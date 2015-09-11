package mongoui.ui.main;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
  TabPane queryTabs;

  public void setConnectionSettings(ConnectionSettings connectionSettings) {
    dbConnect = mongoService.connect(connectionSettings);
    TreeItem<DbTreeValue> root = new TreeItem<>();
    root.getChildren().addAll(buildDbList());
    treeView.setRoot(root);
  }

  private List<TreeItem<DbTreeValue>> buildDbList() {
    return dbConnect.listDbs().stream().map(d -> new TreeItem<>(new DbTreeValue(d))).peek(i -> buildDbChilds(i))
        .collect(Collectors.toList());
  }

  private void buildDbChilds(TreeItem<DbTreeValue> i) {
    MongoDatabase db = i.getValue().getMongoDatabase();
    i.getChildren()
    .addAll(db.listCollectins().stream()
        .map(cn -> new TreeItem<>(new DbTreeValue(db, cn)))
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
  public void createNewDb() throws IOException {
    uiBuilder.loadNewDb();
  }

}
