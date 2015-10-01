package mongoui.ui.main;

import java.io.IOException;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import mongoui.service.MongoConnection;
import mongoui.service.MongoService;
import mongoui.settings.ConnectionSettings;

public class MainFrameController {
  @Inject
  private UIBuilder uiBuilder;

  @Inject
  private TreeController treeController;

  @Inject
  private MongoService mongoService;

  private MongoConnection dbConnect;

  @FXML
  private TreeView<DbTreeValue> treeView;

  @FXML
  private TabPane queryTabs;

  @FXML
  protected void initialize() {
    treeController.initialize(treeView);
  }

  public void setConnectionSettings(ConnectionSettings connectionSettings) {
    dbConnect = mongoService.connect(connectionSettings);
    treeController.setDbConnect(dbConnect);
    treeController.reloadDbList();
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
    dialog.showAndWait().ifPresent(r -> treeController.createDB(dialog.getResult()));
  }
}
