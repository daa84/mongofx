package mongofx.ui.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import com.google.inject.Injector;
import com.google.inject.Singleton;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import mongofx.service.MongoConnection;
import mongofx.settings.ConnectionSettings;
import mongofx.ui.settings.ConnectionSettingsController;

@Singleton
public class UIBuilder {

  private Injector injector;
  private Stage primaryStage;
  private Scene previousScene;

  public void setInjector(Injector injector) {
    this.injector = injector;
  }

  public void setStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  public void loadConnectionSetupWindow(ConnectionSettings settings) throws IOException {
    URL url = getClass().getResource("/ui/ConnectionSettings.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    ((ConnectionSettingsController)loader.getController()).setSettings(settings);

    primaryStage.setTitle("MongoFX - connection");
    primaryStage.setScene(createScene(root, 400, 400));
    primaryStage.show();
  }

  public void loadSettingsWindow() throws IOException {
    Scene scene = loadScene("/ui/SettingsList.fxml", 400, 400);
    primaryStage.setTitle("MongoFX - settings");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public void back() {
    if (previousScene != null) {
      primaryStage.setScene(previousScene);
      previousScene = null;
    }
  }

  public void loadMainWindow(ConnectionSettings selectedItem) throws IOException {
    URL url = getClass().getResource("/ui/MainFrame.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    ((MainFrameController)loader.getController()).setConnectionSettings(selectedItem);

    primaryStage.setTitle("MongoFX");
    primaryStage.setScene(createScene(root, 600, 400));
    primaryStage.show();
  }

  private Scene loadScene(String uiPath, double width, double height) throws IOException {
    URL url = getClass().getResource(uiPath);
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    return createScene(root, width, height);
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

  public Entry<Node, QueryTabController> buildQueryNode(MongoConnection dbConnect, DbTreeValue dbTreeValue) throws IOException {
    URL url = getClass().getResource("/ui/QueryTab.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    QueryTabController controller = (QueryTabController)loader.getController();
    controller.setDb(dbTreeValue.getMongoDatabase(), dbTreeValue.getDisplayValue());
    controller.setConnection(dbConnect);
    return new SimpleEntry<>(root, controller);
  }
}
