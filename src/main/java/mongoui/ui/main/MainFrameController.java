package mongoui.ui.main;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import mongoui.service.MongoConnection;
import mongoui.service.MongoDatabase;
import mongoui.service.MongoService;
import mongoui.settings.ConnectionSettings;

public class MainFrameController {

  @Inject
  private UIBuilder uiBuilder;

  @Inject
  private MongoService mongoService;

  private MongoConnection dbConnect;

  @FXML
  private TreeView<DbTreeValue> treeView;

  @FXML
  private TabPane queryTabs;

  private ContextMenu dbContextMenu;
  private ContextMenu collectionContextMenu;

  @FXML
  protected void initialize() {
    treeView.setRoot(new TreeItem<>());
    treeView.setCellFactory(tv -> new TreeDbCell());
    buildDbContextMenu();
    buildCollectionContextMenu();
  }

  private void buildCollectionContextMenu() {
    MenuItem dropCollection = new MenuItem("Drop collection");
    dropCollection.setOnAction(this::onDropCollection);
    collectionContextMenu = new ContextMenu(dropCollection);
  }

  private void buildDbContextMenu() {
    MenuItem createCollection = new MenuItem("Create collection");
    createCollection.setOnAction(this::onCreateNewCollection);
    MenuItem dropCollection = new MenuItem("Drop db");
    dropCollection.setOnAction(this::onDropDB);
    dbContextMenu = new ContextMenu(createCollection, dropCollection);
  }

  public void setConnectionSettings(ConnectionSettings connectionSettings) {
    dbConnect = mongoService.connect(connectionSettings);
    reloadDbList();
  }

  private void reloadDbList() {
    ObservableList<TreeItem<DbTreeValue>> children = treeView.getRoot().getChildren();
    children.clear();
    children.addAll(buildDbList());
  }

  private List<TreeItem<DbTreeValue>> buildDbList() {
    return dbConnect.listDbs().stream().map(d -> createDbItem(d)).peek(i -> buildDbChilds(i))
        .collect(Collectors.toList());
  }

  private TreeItem<DbTreeValue> createDbItem(MongoDatabase d) {
    return new TreeItem<>(new DbTreeValue(d), new FontAwesomeIconView(FontAwesomeIcon.DATABASE));
  }

  private void buildDbChilds(TreeItem<DbTreeValue> i) {
    MongoDatabase db = i.getValue().getMongoDatabase();
    i.getChildren()
        .addAll(db.listCollectins().stream()
            .map(cn -> new TreeItem<>(new DbTreeValue(db, cn), new FontAwesomeIconView(FontAwesomeIcon.TABLE)))
            .collect(Collectors.toList()));
  }

  @FXML
  public void treeViewClicked(MouseEvent ev) throws IOException {
    if (ev.getClickCount() == 2) {
      openTab();
    }
  }

  private void openTab() throws IOException {
    TreeItem<DbTreeValue> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      DbTreeValue value = selectedItem.getValue();
      if (value.isCollectionValue()) {
        queryTabs.getTabs().add(new Tab(value.getDisplayValue(), uiBuilder.buildQueryNode(dbConnect, value)));
        queryTabs.getSelectionModel().selectLast();
      }
    }
  }

  @FXML
  public void onCreateNewDb() throws IOException {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setContentText("Enter Name:");
    dialog.setHeaderText("Create new db");
    dialog.showAndWait().ifPresent(r -> createDB(dialog.getResult()));
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

  private void createDB(String dbName) {
    treeView.getRoot().getChildren().add(createDbItem(dbConnect.createMongoDB(dbName)));
  }

  private class TreeDbCell extends TreeCell<DbTreeValue> {
    @Override
    protected void updateItem(DbTreeValue item, boolean empty) {
      super.updateItem(item, empty);

      setGraphic();

      if (!empty) {
        setText(item.toString());
        if (item.isCollectionValue()) {
          setContextMenu(collectionContextMenu);
        }
        else {
          setContextMenu(dbContextMenu);
        }
      }
      else {
        setText(null);
        setContextMenu(null);
      }
    }

    private void setGraphic() {
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
