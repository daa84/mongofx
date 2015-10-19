package mongofx.ui.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Singleton;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import mongofx.settings.ConnectionSettings;
import mongofx.ui.dbtree.DbTreeValue;
import mongofx.ui.settings.ConnectionSettingsController;
import mongofx.ui.settings.EditMode;
import mongofx.ui.settings.SettingsListController;

@Singleton
public class UIBuilder {
  private static final Logger log = LoggerFactory.getLogger(UIBuilder.class);

  private Injector injector;
  private Stage primaryStage;
  private Scene previousScene;

  public void setInjector(Injector injector) {
    this.injector = injector;
  }

  public void setStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  public Entry<ConnectionSettingsController, BorderPane> loadConnectionSetupWindow(ConnectionSettings settings,
      SettingsListController settingsListController, EditMode editMode) throws IOException {
    URL url = getClass().getResource("/ui/ConnectionSettings.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    ConnectionSettingsController controller = (ConnectionSettingsController)loader.getController();
    controller.setSettings(settings);
    controller.setSettingsController(settingsListController);
    controller.setEditMode(editMode);

    return new SimpleEntry<>(controller, root);
  }

  public void showSettingsWindow(MainFrameController controller) throws IOException {
    URL url = getClass().getResource("/ui/SettingsList.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    SettingsListController dialogController = loader.getController();

    ButtonType connectButtonType = new ButtonType("Connect", ButtonData.OK_DONE);
    Dialog<ConnectionSettings> dialog = new Dialog<>();
    dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);
    dialog.setTitle("MongoFX - settings");
    dialog.getDialogPane().setContent(root);
    dialog.setResultConverter(bt -> {
      if (ButtonData.OK_DONE == bt.getButtonData()) {
        return dialogController.getSelected();
      }
      return null;
    });
    dialogController.setDialog(dialog);
    dialog.showAndWait().ifPresent(connectionSettings -> {
      try {
        dialogController.saveIfSettingsChanged();
        controller.addConnectionSettings(connectionSettings);
      }
      catch (Exception e) {
        log.error("Can't save changes", e);
      }
    });
  }

  public void back() {
    if (previousScene != null) {
      primaryStage.setScene(previousScene);
      previousScene = null;
    }
  }

  public MainFrameController loadMainWindow() throws IOException {
    URL url = getClass().getResource("/ui/MainFrame.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    MainFrameController mainFrameController = (MainFrameController)loader.getController();

    primaryStage.setTitle("MongoFX");
    primaryStage.setScene(createScene(root, 600, 400));
    primaryStage.show();
    return mainFrameController;
  }

  private Scene createScene(BorderPane root, double width, double height) {
    Scene scene = new Scene(root, width, height);
    scene.getStylesheets().add(getClass().getResource("/ui/application.css").toExternalForm());
    scene.getStylesheets().add(getClass().getResource("/ui/editor.css").toExternalForm());
    return scene;
  }

  private BorderPane load(URL url, final FXMLLoader loader) throws IOException {
    BorderPane root;
    try (InputStream in = url.openStream()) {
      root = loader.load(in);
    }
    return root;
  }

  private FXMLLoader createLoader(URL url) {
    final FXMLLoader loader = new FXMLLoader();
    loader.setLocation(url);
    loader.setControllerFactory((param) -> injector.getInstance(param));
    return loader;
  }

  public Entry<Node, QueryTabController> buildQueryNode(DbTreeValue dbTreeValue) throws IOException {
    URL url = getClass().getResource("/ui/QueryTab.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    QueryTabController controller = (QueryTabController)loader.getController();
    controller.setDb(dbTreeValue.getMongoDatabase(), dbTreeValue.getDisplayValue());
    return new SimpleEntry<>(root, controller);
  }

  public Stage getPrimaryStage() {
    return primaryStage;
  }
}
