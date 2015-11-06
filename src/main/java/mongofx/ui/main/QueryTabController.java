// This file is part of MongoFX.
//
// MongoFX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
// Foobar is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

//
// Copyright (c) Andrey Dubravin, 2015
//
package mongofx.ui.main;

import java.util.Optional;

import javax.script.ScriptException;

import javafx.beans.property.SimpleBooleanProperty;
import org.fxmisc.richtext.CodeArea;
import org.reactfx.EventStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import mongofx.codearea.CodeAreaBuilder;
import mongofx.js.api.ObjectListPresentation;
import mongofx.js.api.TextPresentation;
import mongofx.service.AutocompleteService;
import mongofx.service.MongoDatabase;
import mongofx.service.MongoService.MongoDbConnection;
import mongofx.ui.result.tree.DocumentTreeValue;
import mongofx.ui.result.tree.ResultTreeController;

public class QueryTabController {

  private static final Logger log = LoggerFactory.getLogger(QueryTabController.class);

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
  private AutocompleteService autocompleteService;

  @Inject
  private ResultTreeController resultTreeController;

  private final SimpleStringProperty connectedServerName = new SimpleStringProperty();
  private final SimpleStringProperty connectedDBName = new SimpleStringProperty();

  private final SimpleBooleanProperty showObjectListControls = new SimpleBooleanProperty();

  private QueryResultHolder queryResult;

  @FXML
  protected void initialize() {
    EventStreams.changesOf(viewToggleGroup.selectedToggleProperty()).subscribe(e -> updateResultListView());
  }

  public void setDb(MongoDbConnection mongoDbConnection, MongoDatabase mongoDatabase, String collectionName) {
    this.mongoDatabase = mongoDatabase;
    CodeAreaBuilder builder = new CodeAreaBuilder(codeArea, uiBuilder.getPrimaryStage()).setup().setupAutocomplete(autocompleteService);
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
      executeScript();
    }
  }

  public void executeScript() {
    try {
      Optional<Object> documents = mongoDatabase.eval(codeArea.getText());

      if (documents.isPresent()) {
        Object result = documents.get();
        if (result instanceof TextPresentation) {
          queryResult = new QueryResultHolder((TextPresentation) result);
        } else {
          queryResult = new QueryResultHolder((ObjectListPresentation) result);
        }
      } else {
        queryResult = new QueryResultHolder();
      }

      buildResultView();
    }
    catch (ScriptException e) {
      showOnlyText(e.getMessage());
      log.error("Error execute script", e);
    }
  }

  private void buildResultView() {
    if (!queryResult.isEmpty()) {
      if (queryResult.isTextOnlyPresentation()) {
        showOnlyText(queryResult.getTextPresentationString());
      }
      else {
        setViewModeVisible(true);
        updateResultListView();
      }
    }
    else {
      showOnlyText("Empty result");
    }
  }

  private void showOnlyText(String text) {
    setViewModeVisible(false);
    queryResultTextController.replaceText(text);
    showText();
  }

  private void updateResultListView() {
    queryResult.getSkip().ifPresent(skip -> skipResult.setText(String.valueOf(skip)));
    queryResult.getLimit().ifPresent(limit -> limitResult.setText(String.valueOf(limit)));

    if (viewAsTree.isSelected()) {
      resultTreeController.buildTreeFromDocuments(queryResult.getDocuments(getSkip(), getLimit()), queryResult.getCollectionName());
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
    codeArea.requestFocus();
    executeScript();
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

  public boolean getShowObjectListControls() {
    return showObjectListControls.get();
  }

  public void setShowObjectListControls(boolean showObjectListControls) {
    this.showObjectListControls.set(showObjectListControls);
  }

  public SimpleBooleanProperty showObjectListControlsProperty() {
    return showObjectListControls;
  }
}
