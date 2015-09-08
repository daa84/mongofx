package mongoui.ui.main;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import mongoui.service.MongoConnection;
import mongoui.service.MongoService;
import mongoui.settings.ConnectionSettings;

public class MainFrameController {

  @Inject
  private MongoService mongoService;

  private ConnectionSettings connectionSettings;

  private MongoConnection dbConnect;

  @FXML
  private TreeView<String> treeView;

  public void setConnectionSettings(ConnectionSettings connectionSettings) {
    this.connectionSettings = connectionSettings;
    dbConnect = mongoService.connect(connectionSettings);
    TreeItem<String> root = new TreeItem<>();
    root.getChildren().addAll(StreamSupport.stream(dbConnect.listDbs().spliterator(), false)
        .map(d -> new TreeItem<>(d)).collect(Collectors.toList()));
    treeView.setRoot(root);
  }

}
