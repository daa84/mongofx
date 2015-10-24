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
package mongofx.ui.result.tree;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;

import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import mongofx.service.MongoDatabase;
import mongofx.ui.main.DocumentUtils;
import mongofx.ui.main.UIBuilder;

public class ResultTreeController {

  @Inject
  private UIBuilder uiBuilder;

  private TreeTableView<DocumentTreeValue> queryResultTree;
  private final ContextMenu editItemContextMenu;

  private MongoDatabase mongoDatabase;

  public ResultTreeController() {
    MenuItem editDocument = new MenuItem("Edit document");
    editDocument.setOnAction(this::editSelected);
    editItemContextMenu = new ContextMenu(editDocument);
  }

  public void initialize(TreeTableView<DocumentTreeValue> queryResultTree, MongoDatabase mongoDatabase) {
    this.queryResultTree = queryResultTree;
    this.mongoDatabase = mongoDatabase;
    queryResultTree.setRowFactory(ttv -> new ResultRow());
  }

  public void buildTreeFromDocuments(Stream<Document> resultStream) {
    TreeItem<DocumentTreeValue> root = new TreeItem<>();
    buildTreeFromDocuments(root, resultStream);
    queryResultTree.setRoot(root);
  }

  private void buildTreeFromDocuments(TreeItem<DocumentTreeValue> root, Stream<? extends Object> documents) {
    root.getChildren().addAll(documents.map(d -> new TreeItem<>(new DocumentTreeValue(null, d)))
        .peek(i -> buildChilds(i)).collect(Collectors.toList()));
  }

  private void buildChilds(TreeItem<DocumentTreeValue> i) {
    Object value = i.getValue().getValue();
    if (value instanceof Document) {
      i.getChildren()
      .addAll(((Document)value).entrySet().stream().map(f -> mapFieldToItem(f)).collect(Collectors.toList()));
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

    Document oldDoc = selectedItem.getValue().getDocument();
    uiBuilder.editDocument(DocumentUtils.formatJson(oldDoc)).ifPresent(newJson -> {
      Document doc = Document.parse(newJson);
      //TODO: assign document collection name after execution
      //      mongoDatabase.getMongoDb().getCollection("test").updateOne(filter, update)
    });
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
        setContextMenu(editItemContextMenu);
      }
    }
  }
}
