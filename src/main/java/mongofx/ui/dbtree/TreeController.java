package mongofx.ui.dbtree;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import mongofx.service.Executor;
import mongofx.service.MongoConnection;
import mongofx.service.MongoDatabase;
import mongofx.ui.dbtree.DbTreeValue.TreeValueType;

public class TreeController {
  private static final Logger log = LoggerFactory.getLogger(TreeController.class);

  @Inject
  private Executor executor;

  private TreeView<DbTreeValue> treeView;

  private ContextMenu dbContextMenu;
  private ContextMenu collectionContextMenu;
  private ContextMenu indexContextMenu;

  private MongoConnection dbConnect;

  public void reloadDbList() {
    ObservableList<TreeItem<DbTreeValue>> children = treeView.getRoot().getChildren();

    Task<List<TreeItem<DbTreeValue>>> loadTask = new Task<List<TreeItem<DbTreeValue>>>() {

      @Override
      protected List<TreeItem<DbTreeValue>> call() throws Exception {
        return buildDbList();
      }

      @Override
      protected void succeeded() {
        children.setAll(getValue());
      }

      @Override
      protected void failed() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setHeaderText("Can't connect to MongoDB");
        Throwable exception = getException();
        if (exception != null) {
          log.error("Error", exception);
          alert.setContentText(exception.getMessage());
        }
        alert.showAndWait();
      }
    };

    executor.execute(loadTask);
  }

  private List<TreeItem<DbTreeValue>> buildDbList() {
    return dbConnect.listDbs().stream().map(d -> createDbItem(d)).collect(Collectors.toList());
  }

  private TreeItem<DbTreeValue> createDbItem(MongoDatabase d) {
    return new DynamicTreeItem(new DbTreeValue(d, d.getName(), TreeValueType.DATABASE),
        new FontAwesomeIconView(FontAwesomeIcon.DATABASE), executor, this::buildDbChilds);
  }

  private List<TreeItem<DbTreeValue>> buildDbChilds(DbTreeValue value) {
    MongoDatabase db = value.getMongoDatabase();
    return db.listCollectins().stream()
        .map(cn -> new TreeItem<>(new DbTreeValue(db, cn, TreeValueType.COLLECTION),
            new FontAwesomeIconView(FontAwesomeIcon.TABLE)))
        .peek(ti -> buildCollectionDetail(db, ti)).collect(Collectors.toList());
  }

  private void buildCollectionDetail(MongoDatabase db, TreeItem<DbTreeValue> ti) {
    DbTreeValue indexCategory = new DbTreeValue(db, "Indexes", TreeValueType.CATEGORY);
    indexCategory.setCollectionName(ti.getValue().getDisplayValue());
    ti.getChildren().add(new DynamicTreeItem(indexCategory, new FontAwesomeIconView(FontAwesomeIcon.FOLDER), executor,
        this::buildIndexes));
  }

  private List<TreeItem<DbTreeValue>> buildIndexes(DbTreeValue value) {
    MongoCollection<Document> collection =
        value.getMongoDatabase().getMongoDb().getCollection(value.getCollectionName());

    return StreamSupport.stream(collection.listIndexes().spliterator(), false).map(d -> {
      DbTreeValue val = new DbTreeValue(value.getMongoDatabase(), (String)d.get("name"), TreeValueType.INDEX);
      val.setCollectionName(value.getDisplayValue());
      return new TreeItem<DbTreeValue>(val, new FontAwesomeIconView(FontAwesomeIcon.ASTERISK));
    }).collect(Collectors.toList());
  }

  public void initialize(TreeView<DbTreeValue> treeView) {
    this.treeView = treeView;
    treeView.setRoot(new TreeItem<>());
    treeView.setCellFactory(tv -> new TreeDbCell());
    buildDbContextMenu();
    buildCollectionContextMenu();
    buildIndexContextMenu();
  }

  private void buildIndexContextMenu() {
    MenuItem dropIndex = new MenuItem("Drop Index...");
    dropIndex.setOnAction(this::onDropIndex);
    indexContextMenu = new ContextMenu(dropIndex);
  }

  private void buildCollectionContextMenu() {
    MenuItem removeAllDocs = new MenuItem("Remove All Documents...");
    removeAllDocs.setOnAction(this::onRemoveAllDocuments);
    MenuItem dropCollection = new MenuItem("Drop Collection...");
    dropCollection.setOnAction(this::onDropCollection);
    collectionContextMenu = new ContextMenu(removeAllDocs, dropCollection);
  }

  private void buildDbContextMenu() {
    MenuItem createCollection = new MenuItem("Create collection");
    createCollection.setOnAction(this::onCreateNewCollection);
    MenuItem dropCollection = new MenuItem("Drop db");
    dropCollection.setOnAction(this::onDropDB);
    dbContextMenu = new ContextMenu(createCollection, dropCollection);
  }

  public void createDB(String dbName) {
    treeView.getRoot().getChildren().add(createDbItem(dbConnect.createMongoDB(dbName)));
  }

  private void onCreateNewCollection(ActionEvent ev) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setContentText("Enter Name:");
    dialog.setHeaderText("Create new collection");
    dialog.showAndWait().ifPresent(r -> {
      DbTreeValue value = treeView.getSelectionModel().getSelectedItem().getValue();
      value.getMongoDatabase().createCollection(dialog.getResult());
      reloadDbList();
    });
  }

  private void onDropIndex(ActionEvent ev) {
    DbTreeValue value = treeView.getSelectionModel().getSelectedItem().getValue();
    String indexName = value.getDisplayValue();

    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setHeaderText("Drop index " + indexName);
    alert.setContentText("Are you sure?");
    alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
      value.getMongoDatabase().dropIndex(value.getCollectionName(), indexName);
      reloadDbList();
    });
  }

  private void onDropCollection(ActionEvent ev) {
    DbTreeValue value = treeView.getSelectionModel().getSelectedItem().getValue();
    String collectionName = value.getDisplayValue();

    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setHeaderText("Drop collection " + collectionName);
    alert.setContentText("Are you sure?");
    alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
      value.getMongoDatabase().dropCollection(collectionName);
      reloadDbList();
    });
  }

  private void onRemoveAllDocuments(ActionEvent ev) {
    DbTreeValue value = treeView.getSelectionModel().getSelectedItem().getValue();
    String collectionName = value.getDisplayValue();

    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setHeaderText("Remove all documents form " + collectionName);
    alert.setContentText("Are you sure?");
    alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
      value.getMongoDatabase().removeAllDocuments(collectionName);
    });
  }

  private void onDropDB(ActionEvent ev) {
    DbTreeValue value = treeView.getSelectionModel().getSelectedItem().getValue();
    String dbName = value.getDisplayValue();

    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setHeaderText("Drop database " + dbName);
    alert.setContentText("Are you sure?");
    alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
      value.getMongoDatabase().drop();
      reloadDbList();
    });
  }

  public void setDbConnect(MongoConnection dbConnect) {
    this.dbConnect = dbConnect;
  }

  private class TreeDbCell extends TreeCell<DbTreeValue> {
    @Override
    protected void updateItem(DbTreeValue item, boolean empty) {
      super.updateItem(item, empty);

      setupGraphic();

      if (!empty) {
        setText(item.toString());
        setupContextMenu(item);
      }
      else {
        setText(null);
        setContextMenu(null);
      }
    }

    private void setupContextMenu(DbTreeValue item) {
      TreeValueType valueType = item.getValueType();
      if (valueType == TreeValueType.DATABASE) {
        setContextMenu(dbContextMenu);
      }
      else if (valueType == TreeValueType.COLLECTION) {
        setContextMenu(collectionContextMenu);
      }
      else if (valueType == TreeValueType.INDEX) {
        setContextMenu(indexContextMenu);
      }
      else {
        setContextMenu(null);
      }
    }

    private void setupGraphic() {
      TreeItem<DbTreeValue> treeItem = getTreeItem();
      if (treeItem != null && treeItem.getGraphic() != null) {
        setGraphic(treeItem.getGraphic());
      }
      else {
        setGraphic(null);
      }
    }
  }
}
