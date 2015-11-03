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
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.script.ScriptException;

import org.bson.Document;
import org.fxmisc.richtext.CodeArea;
import org.reactfx.EventStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.mongodb.client.MongoCursor;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
  private TextField offsetResult;

  @FXML
  private ToggleButton viewAsTree;

  @FXML
  private ToggleButton viewAsText;

  private ObjectListPresentation objectListResult;

  @FXML
  private ToggleGroup viewToogleGroup;

  @Inject
  private AutocompleteService autocompleteService;

  @Inject
  private ResultTreeController resultTreeController;

  private final SimpleStringProperty connectedServerName = new SimpleStringProperty();
  private final SimpleStringProperty connectedDBName = new SimpleStringProperty();

  @FXML
  protected void initialize() {
    EventStreams.changesOf(viewToogleGroup.selectedToggleProperty()).subscribe(e -> updateResultListView());
  }

  public void setDb(MongoDbConnection mongoDbConnection, MongoDatabase mongoDatabase, String collectionName) {
    this.mongoDatabase = mongoDatabase;
    new CodeAreaBuilder(codeArea, uiBuilder.getPrimaryStage()).setup().setupAutocomplete(autocompleteService)
    .setText("db.getCollection('" + collectionName + "').find({})");

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
      buildResultView(documents);
    }
    catch (ScriptException e) {
      showOnlyText(e.getMessage());
      log.error("Error execute script", e);
    }
  }

  private void buildResultView(Optional<Object> documents) {
    if (documents.isPresent()) {
      Object result = documents.get();

      objectListResult = null;

      if (result instanceof TextPresentation) {
        showOnlyText(String.valueOf(result));
      }
      else {
        setViewModeVisible(true);
        objectListResult = (ObjectListPresentation)result;
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
    //TODO: cache results
    try(MongoCursor<Document> iterator = objectListResult.iterator()) {
      Stream<Document> resultStream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false)//
          .limit(Integer.parseInt(limitResult.getText()));

      if (viewAsTree.isSelected()) {
        resultTreeController.buildTreeFromDocuments(resultStream, objectListResult.getCollectionName());
        showTree();
      }
      else {
        queryResultTextController.replaceText(String.valueOf(buildTextFromList(resultStream)));
        queryResultTextController.selectRange(0);
        showText();
      }
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

  private String buildTextFromList(Stream<Document> resultStream) {
    return resultStream.map(DocumentUtils::formatJson).collect(Collectors.joining(",\n", "[", "]"));
  }

  private void setViewModeVisible(boolean b) {
    viewAsText.setVisible(b);
    viewAsTree.setVisible(b);
  }

  public void startTab() {
    int startIdx = codeArea.getText().indexOf("{");
    if (startIdx < 0) {
      startIdx = 0;
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
    int offset = getOffset() - getLimit();
    if (offset < 0) {
      offset = 0;
    }
    if (offset == getOffset()) {
      return;
    }
    offsetResult.setText(String.valueOf(offset));
    executeScript();
  }

  private int getOffset() {
    return Integer.parseInt(offsetResult.getText());
  }

  private int getLimit() {
    return Integer.parseInt(limitResult.getText());
  }

  @FXML
  public void resultScrollRight() {
    int offset = getOffset();
    if (offset >= Integer.MAX_VALUE - getLimit()) {
      return;
    }
    offsetResult.setText(String.valueOf(offset + getLimit()));
    executeScript();
  }
}
