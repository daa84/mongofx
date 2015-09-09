package mongoui.ui.main;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
  private MongoService mongoService;

  private MongoConnection dbConnect;

  @FXML
  private TreeView<String> treeView;

  @FXML
  TabPane queryTabs;

  public void setConnectionSettings(ConnectionSettings connectionSettings) {
    dbConnect = mongoService.connect(connectionSettings);
    TreeItem<String> root = new TreeItem<>();
    root.getChildren().addAll(StreamSupport.stream(dbConnect.listDbs().spliterator(), false)
        .map(d -> new TreeItem<>(d)).collect(Collectors.toList()));
    treeView.setRoot(root);
  }

  @FXML
  public void treeViewClicked(MouseEvent ev) throws IOException {
    if (ev.getClickCount() == 2) {
      TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        queryTabs.getTabs().add(new Tab(selectedItem.getValue(), uiBuilder.buildQueryNode(selectedItem.getValue())));
        queryTabs.getSelectionModel().selectLast();
      }
    }
  }

}
