// This file is part of MongoFX.
//
// MongoFX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
// MongoFX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with MongoFX.  If not, see <http://www.gnu.org/licenses/>.

//
// Copyright (c) Andrey Dubravin, 2015
//
package mongofx.ui.main;

import java.util.Optional;

import mongofx.service.ScriptService;
import org.fxmisc.richtext.CodeArea;
import org.reactfx.EventStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import mongofx.codearea.CodeAreaBuilder;
import mongofx.javafx.FocusFixer;
import mongofx.js.api.ObjectListPresentation;
import mongofx.service.Executor;
import mongofx.service.MongoDatabase;
import mongofx.service.MongoService.MongoDbConnection;
import mongofx.ui.result.tree.DocumentTreeValue;
import mongofx.ui.result.tree.ResultTreeController;

public class QueryTabController {

  private static final Logger log = LoggerFactory.getLogger(QueryTabController.class);

  @Inject
  private Executor executor;

  @FXML
  private CodeArea codeArea;

  @Inject
  private UIBuilder uiBuilder;

  @FXML
  private TreeTableView<DocumentTreeValue> queryResultTree;

  @FXML
  private EditorController queryResultTextController;
  @FXML
  private Node queryResultText;

  private MongoDatabase mongoDatabase;

  @FXML
  private TextField limitResult;

  @FXML
  private TextField skipResult;

  @FXML
  private ToggleButton viewAsTree;

  @FXML
  private ToggleButton viewAsText;

  @FXML
  private ToggleGroup viewToggleGroup;

  @Inject
  private AutocompletionEngine autocompleteService;

  @Inject
  private ResultTreeController resultTreeController;

  @Inject
  private ScriptService scriptService;

  private EditorFileController editorFileController;

  private final SimpleStringProperty connectedServerName = new SimpleStringProperty();
  private final SimpleStringProperty connectedDBName = new SimpleStringProperty();

  private final SimpleBooleanProperty showObjectListControls = new SimpleBooleanProperty();
  private final SimpleBooleanProperty showExecTime = new SimpleBooleanProperty();
  private final SimpleBooleanProperty showProgress = new SimpleBooleanProperty();
  private final SimpleStringProperty lastExecTime = new SimpleStringProperty();

  private QueryResultHolder queryResult;

  private Task<QueryResultHolder> currentTask;

  @FXML
  protected void initialize() {
    editorFileController = new EditorFileController(uiBuilder, codeArea);
    EventStreams.changesOf(viewToggleGroup.selectedToggleProperty()).subscribe(e -> updateResultListView());
  }

  public void setDb(MongoDbConnection mongoDbConnection, MongoDatabase mongoDatabase, String collectionName) {
    this.mongoDatabase = mongoDatabase;
    autocompleteService.setMongoDb(mongoDatabase);
    CodeAreaBuilder builder = new CodeAreaBuilder(codeArea, uiBuilder.getPrimaryStage()).setup()
        .setupAutocomplete(autocompleteService, collectionName);
    if (collectionName != null) {
      builder.setText("db.getCollection('" + collectionName + "').find({})");
    } else {
      builder.setText("db");
    }

    resultTreeController.initialize(queryResultTree, mongoDatabase);

    queryResultTextController.disableEdit();

    setConnectedServerName(mongoDbConnection.getConnectionSettings().getHost());
    setConnectedDBName(mongoDatabase.getName());
  }

  @FXML
  public void codeAreaOnKeyReleased(KeyEvent ev) {
    if (ev.getCode() == KeyCode.F5) {
      executeScript(false);
    } else if (ev.getCode() == KeyCode.ENTER && ev.isControlDown()) {
      executeScript(true);
    }
  }

  public void saveCurrentBuffer() {
    editorFileController.saveCurrentBuffer(mongoDatabase.getName() + ".js");
  }

  public void loadToBuffer() {
    editorFileController.loadToBuffer();
  }

  public void executeScript(boolean selected) {
    if (currentTask != null) {
      log.warn("Task already running");
      return;
    }

    final String script = getScript(selected);
    final int skip = getSkip();
    final int limit = getLimit();

    startProgress();

    currentTask = new Task<QueryResultHolder>() {
      private float scriptEvalTime;
      private float dataLoadTime;
      private long startEvalTime;

      @Override
      protected QueryResultHolder call() throws Exception {
        startEvalTime = System.currentTimeMillis();
        Optional<Object> documents = scriptService.eval(mongoDatabase, script);
        scriptEvalTime = (System.currentTimeMillis() - startEvalTime) / 1000f;

        if (documents.isPresent()) {
          Object result = documents.get();
          if (result instanceof ObjectListPresentation) {
            return processListResult((ObjectListPresentation) result);
          }
          return new QueryResultHolder(String.valueOf(result));
        }
        return new QueryResultHolder();
      }

      private QueryResultHolder processListResult(ObjectListPresentation result) {
        QueryResultHolder queryResultHolder = new QueryResultHolder(result);
        // preload cache in background
        dataLoadTime = System.currentTimeMillis();
        queryResultHolder.getDocuments(queryResultHolder.getSkip().orElse(skip), queryResultHolder.getLimit().orElse(limit));
        dataLoadTime = (System.currentTimeMillis() - dataLoadTime) / 1000f;
        return queryResultHolder;
      }

      @Override
      protected void failed() {
        setLastExecTime(String.format("%.3f sec.", (System.currentTimeMillis() - startEvalTime) / 1000f));
        Throwable e = getException();
        showOnlyText(e.getMessage());
        log.error("Error execute script", e);
      }

      @Override
      protected void succeeded() {
        queryResult = getValue();
        buildResultView();
        setLastExecTime(
            String.format("%.3f sec.", scriptEvalTime + dataLoadTime));
      }

      @Override
      protected void done() {
        stopProgress();
        currentTask = null;
      }
    };

    executor.executeMany(currentTask);
  }

  private String getScript(boolean selected) {
    String script;
    if (selected) {
      script = codeArea.getSelectedText();
    } else {
      script = codeArea.getText();
    }
    return script;
  }

  private void stopProgress() {
    setShowProgress(false);
    showExecTime();
  }

  private void showExecTime() {
    setShowExecTime(true);
  }

  private void startProgress() {
    setShowExecTime(false);
    setShowProgress(true);
  }

  @FXML
  public void stopEval() {
    if (currentTask != null) {
      currentTask.cancel();
    }
  }

  private void buildResultView() {
    if (!queryResult.isEmpty()) {
      if (queryResult.isTextOnlyPresentation()) {
        showOnlyText(queryResult.getTextPresentationString());
      } else {
        setViewModeVisible(true);
        updateResultListView();
      }
    } else {
      showOnlyText("Empty result");
    }
  }

  private void showOnlyText(String text) {
    setViewModeVisible(false);
    if (text == null) {
      text = "Empty result";
    }
    queryResultTextController.replaceText(text);
    showText();
  }

  private void updateResultListView() {
    queryResult.getSkip().ifPresent(skip -> skipResult.setText(String.valueOf(skip)));
    queryResult.getLimit().ifPresent(limit -> limitResult.setText(String.valueOf(limit)));

    if (viewAsTree.isSelected()) {
      resultTreeController.buildTreeFromDocuments(queryResult.getDocuments(getSkip(), getLimit()),
          queryResult.getCollectionName());
      showTree();
    } else {
      queryResultTextController.replaceText(queryResult.getListPresentationString(getSkip(), getLimit()));
      queryResultTextController.selectRange(0);
      showText();
    }
  }

  private void showText() {
    resultTreeController.hide();
    queryResultText.setVisible(true);
  }

  private void showTree() {
    resultTreeController.show();
    queryResultTextController.clear();
    queryResultText.setVisible(false);
  }

  private void setViewModeVisible(boolean b) {
    setShowObjectListControls(b);
  }

  public void startTab() {
    String text = codeArea.getText();
    int startIdx = text.indexOf("{");
    if (startIdx < 0) {
      startIdx = text.length() - 1;
    }
    startIdx++;
    codeArea.selectRange(startIdx, startIdx);
    FocusFixer.requestFocus(codeArea);
    executeScript(false);
  }

  public SimpleStringProperty connectedServerNameProperty() {
    return this.connectedServerName;
  }

  public String getConnectedServerName() {
    return this.connectedServerNameProperty().get();
  }

  public void setConnectedServerName(final String connectedServerName) {
    this.connectedServerNameProperty().set(connectedServerName);
  }

  public SimpleStringProperty connectedDBNameProperty() {
    return this.connectedDBName;
  }

  public String getConnectedDBName() {
    return this.connectedDBNameProperty().get();
  }

  public void setConnectedDBName(final String connectedDBName) {
    this.connectedDBNameProperty().set(connectedDBName);
  }

  @FXML
  public void resultScrollLeft() {
    int offset = getSkip() - getLimit();
    if (offset < 0) {
      offset = 0;
    }
    if (offset == getSkip()) {
      return;
    }
    skipResult.setText(String.valueOf(offset));
    updateResultListView();
  }

  private int getSkip() {
    return Integer.parseInt(skipResult.getText());
  }

  private int getLimit() {
    return Integer.parseInt(limitResult.getText());
  }

  @FXML
  public void resultScrollRight() {
    int offset = getSkip();
    if (offset >= Integer.MAX_VALUE - getLimit()) {
      return;
    }
    skipResult.setText(String.valueOf(offset + getLimit()));
    updateResultListView();
  }

  public void saveCurrentBufferAs() {
    editorFileController.saveCurrentBufferAs(mongoDatabase.getName() + ".js");
  }

  public boolean getShowObjectListControls() {
    return showObjectListControls.get();
  }

  public void setShowObjectListControls(boolean showObjectListControls) {
    this.showObjectListControls.set(showObjectListControls);
  }

  public SimpleBooleanProperty showObjectListControlsProperty() {
    return showObjectListControls;
  }

  public final SimpleBooleanProperty showExecTimeProperty() {
    return this.showExecTime;
  }

  public final boolean isShowExecTime() {
    return this.showExecTimeProperty().get();
  }

  public final void setShowExecTime(final boolean showExecTime) {
    this.showExecTimeProperty().set(showExecTime);
  }

  public final SimpleBooleanProperty showProgressProperty() {
    return this.showProgress;
  }

  public final boolean isShowProgress() {
    return this.showProgressProperty().get();
  }

  public final void setShowProgress(final boolean showProgress) {
    this.showProgressProperty().set(showProgress);
  }

  public final SimpleStringProperty lastExecTimeProperty() {
    return this.lastExecTime;
  }

  public final java.lang.String getLastExecTime() {
    return this.lastExecTimeProperty().get();
  }

  public final void setLastExecTime(final java.lang.String lastExecTime) {
    this.lastExecTimeProperty().set(lastExecTime);
  }
}
