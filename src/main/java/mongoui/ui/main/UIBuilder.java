package mongoui.ui.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.google.inject.Injector;
import com.google.inject.Singleton;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import mongoui.settings.ConnectionSettings;
import mongoui.ui.settings.ConnectionSettingsController;

@Singleton
public class UIBuilder {

  private Injector injector;
  private Stage primaryStage;

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

    primaryStage.setScene(createScene(root));
    primaryStage.show();
  }

  public void loadSettingsWindow() throws IOException {
    Scene scene = loadScene("/ui/SettingsList.fxml");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public void loadMainWindow(ConnectionSettings selectedItem) throws IOException {
    URL url = getClass().getResource("/ui/MainFrame.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    ((MainFrameController)loader.getController()).setConnectionSettings(selectedItem);

    primaryStage.setScene(createScene(root));
    primaryStage.show();
  }

  private Scene loadScene(String uiPath) throws IOException {
    URL url = getClass().getResource(uiPath);
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    return createScene(root);
  }

  private Scene createScene(BorderPane root) {
    Scene scene = new Scene(root, 400, 400);
    scene.getStylesheets().add(getClass().getResource("/ui/application.css").toExternalForm());
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
}
