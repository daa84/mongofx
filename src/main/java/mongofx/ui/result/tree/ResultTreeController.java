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
package mongofx.ui.result.tree;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import mongofx.service.MongoDatabase;
import mongofx.ui.main.DocumentUtils;
import mongofx.ui.main.UIBuilder;

public class ResultTreeController {
  private static final Logger log = LoggerFactory.getLogger(ResultTreeController.class);

  @Inject
  private UIBuilder uiBuilder;

  private TreeTableView<DocumentTreeValue> queryResultTree;
  private ContextMenu topLevelContextMenu;

  private MongoDatabase mongoDatabase;
  private ContextMenu childContextMenu;
  private String collectionName;

  public ResultTreeController() {
    buildTopLevelContextMenu();
    buildChildContextMenu();
  }

  private void buildRootContextMenu() {
    MenuItem insertDocument = new MenuItem("Insert document...");
    insertDocument.setOnAction(this::insertDocument);
    ContextMenu contextMenu = new ContextMenu(insertDocument);
    queryResultTree.setContextMenu(contextMenu);
  }

  private void buildChildContextMenu() {
    MenuItem insertDocument = new MenuItem("Insert document...");
    insertDocument.setOnAction(this::insertDocument);
    MenuItem editDocument = new MenuItem("Edit document...");
    editDocument.setOnAction(this::editSelected);
    MenuItem copyValue = new MenuItem("Copy value");
    copyValue.setOnAction(this::copyValue);
    MenuItem copyJson = new MenuItem("Copy JSON");
    copyJson.setOnAction(this::copyJson);
    childContextMenu = new ContextMenu(insertDocument, editDocument, new SeparatorMenuItem(), copyJson, copyValue);
  }

  private void copyValue(ActionEvent actionEvent) {
    TreeItem<DocumentTreeValue> selectedItem = queryResultTree.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      return;
    }

    setToClipboard(selectedItem.getValue());
  }

  private void setToClipboard(Object value) {
    ClipboardContent content = new ClipboardContent();
    content.putString(String.valueOf(value));
    Clipboard.getSystemClipboard().setContent(content);
  }

  private void buildTopLevelContextMenu() {
    MenuItem insertDocument = new MenuItem("Insert document...");
    insertDocument.setOnAction(this::insertDocument);
    MenuItem editDocument = new MenuItem("Edit document...");
    editDocument.setOnAction(this::editSelected);
    MenuItem copyJson = new MenuItem("Copy JSON");
    copyJson.setOnAction(this::copyJson);
    MenuItem deleteDocument = new MenuItem("Delete document...");
    deleteDocument.setOnAction(this::deleteDocument);
    topLevelContextMenu = new ContextMenu(insertDocument, editDocument, new SeparatorMenuItem(), copyJson, new SeparatorMenuItem(), deleteDocument);
  }

  private void insertDocument(ActionEvent actionEvent) {
    if (collectionName == null) {
      log.info("Can't find collection name");
      return;
    }

    uiBuilder.editDocument("{\n\n}", 2).ifPresent(s -> mongoDatabase.getMongoDb().getCollection(collectionName).insertOne(Document.parse(s)));
  }

  private void deleteDocument(ActionEvent actionEvent) {
    TreeItem<DocumentTreeValue> selectedItem = queryResultTree.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      return;
    }

    TreeItem<DocumentTreeValue> topLevelItem = getTopLevelItem(selectedItem);
    final Object id = topLevelItem.getValue().getDocument().get("_id");
    if (id == null) {
      log.error("No _id found for updated object");
      return;
    }

    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setHeaderText("Do you want to delete selected document?");
    alert.setContentText(id.toString());
    alert.showAndWait().ifPresent(sb -> {
      if (sb == ButtonType.OK) {
        mongoDatabase.getMongoDb().getCollection(collectionName).deleteOne(new BasicDBObject("_id", id));
        queryResultTree.getRoot().getChildren().remove(topLevelItem);
      }
    });
  }

  private void copyJson(ActionEvent actionEvent) {
    TreeItem<DocumentTreeValue> selectedItem = queryResultTree.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      return;
    }

    DocumentTreeValue value = selectedItem.getValue();
    if (value.getValue() instanceof Document) {
      setToClipboard(DocumentUtils.formatJson((Document) value.getValue()));
    }
    else {
      setToClipboard(String.format("{\"%s\": \"%s\"}", value.getKey(), value.getValue()));
    }
  }

  public void initialize(TreeTableView<DocumentTreeValue> queryResultTree, MongoDatabase mongoDatabase) {
    this.queryResultTree = queryResultTree;
    this.mongoDatabase = mongoDatabase;
    queryResultTree.setRowFactory(ttv -> new ResultRow());
    buildRootContextMenu();
  }

  public void buildTreeFromDocuments(List<Document> documents, String collectionName) {
    this.collectionName = collectionName;

    TreeItem<DocumentTreeValue> root = new TreeItem<>();
    root.getChildren().addAll(documents.stream().map(d -> new TreeItem<>(new DocumentTreeValue(null, d)))
        .peek(this::buildChilds).collect(Collectors.toList()));

    // always exapnd first document
    if (!documents.isEmpty()) {
      root.getChildren().get(0).setExpanded(true);
    }

    queryResultTree.setRoot(root);
  }

  private void buildChilds(TreeItem<DocumentTreeValue> i) {
    Object value = i.getValue().getValue();
    if (value instanceof Document) {
      i.getChildren()
      .addAll(((Document) value).entrySet().stream().map(this::mapFieldToItem).collect(Collectors.toList()));
    }
  }

  @SuppressWarnings("unchecked")
  private TreeItem<DocumentTreeValue> mapFieldToItem(Entry<String, Object> f) {
    Object value = f.getValue();
    if (value instanceof Document) {
      TreeItem<DocumentTreeValue> root = new TreeItem<>(new DocumentTreeValue(f.getKey(), value));
      buildChilds(root);
      return root;
    }
    if (value instanceof List) {
      TreeItem<DocumentTreeValue> root = new TreeItem<>(new DocumentTreeValue(f.getKey(), value));
      buildTreeFromArray(root, (List<Object>)value);
      return root;
    }
    return new TreeItem<>(new DocumentTreeValue(f.getKey(), f.getValue()));
  }

  private void buildTreeFromArray(TreeItem<DocumentTreeValue> root, List<Object> documents) {
    for (int i = 0; i < documents.size(); i++) {
      Object item = documents.get(i);
      TreeItem<DocumentTreeValue> treeItem = new TreeItem<>(new DocumentTreeValue(String.valueOf(i), item));
      root.getChildren().add(treeItem);
      buildChilds(treeItem);
    }
  }

  public void editSelected(ActionEvent ev) {
    TreeItem<DocumentTreeValue> selectedItem = queryResultTree.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      return;
    }
    DocumentTreeValue value = getTopLevelItem(selectedItem).getValue();

    Document oldDoc = value.getDocument();
    final Object id = oldDoc.get("_id");
    if (id == null) {
      log.error("No _id found for updated object");
      return;
    }
    Document toEdit = new Document(oldDoc);
    toEdit.remove("_id");
    uiBuilder.editDocument(DocumentUtils.formatJson(toEdit), 2).ifPresent(newJson -> updateObject(Document.parse(newJson), id));
  }

  private TreeItem<DocumentTreeValue> getTopLevelItem(TreeItem<DocumentTreeValue> selectedItem) {
    while(selectedItem != null) {
      DocumentTreeValue value = selectedItem.getValue();
      if (value.isTopLevel()) {
        return selectedItem;
      }
      selectedItem = selectedItem.getParent();
    }
    return null;
  }

  private void updateObject(Document doc, Object id) {
    try {
      UpdateResult updateResult = mongoDatabase.getMongoDb().getCollection(collectionName)
          .replaceOne(new BasicDBObject("_id", id), doc);

      if (updateResult.getModifiedCount() < 1) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setHeaderText("Document update filed");
        alert.show();
      }
      else {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setHeaderText("Document updated");
        alert.show();
      }
    }
    catch (Exception ex) {
      log.warn("Error update document", ex);
      Alert alert = new Alert(AlertType.ERROR);
      alert.setHeaderText("Document update filed");
      alert.setContentText(ex.getMessage());
      alert.show();
    }
  }

  public void hide() {
    queryResultTree.setVisible(false);
  }

  public void show() {
    queryResultTree.setVisible(true);
  }

  public class ResultRow extends TreeTableRow<DocumentTreeValue> {
    @Override
    protected void updateItem(DocumentTreeValue item, boolean empty) {
      super.updateItem(item, empty);
      if (empty) {
        setContextMenu(null);
      } else {
        if (item.isTopLevel()) {
          setContextMenu(topLevelContextMenu);
        } else {
          setContextMenu(childContextMenu);
        }
      }
    }
  }
}
