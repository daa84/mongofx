// This file is part of MongoFX.
//
// MongoFX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
// MongoFX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with MongoFX.  If not, see <http://www.gnu.org/licenses/>.

//
// Copyright (c) Andrey Dubravin, 2015
//
package mongofx.ui.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Optional;

import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import mongofx.service.MongoService.MongoDbConnection;
import mongofx.service.PropertiesService;
import mongofx.service.settings.ConnectionSettings;
import mongofx.ui.dbtree.DbTreeValue;
import mongofx.ui.settings.ConnectionSettingsController;
import mongofx.ui.settings.EditMode;
import mongofx.ui.settings.SettingsListController;

@Singleton
public class UIBuilder {
  private static final Logger log = LoggerFactory.getLogger(UIBuilder.class);

  @Inject
  private PropertiesService properteisService;

  private Injector injector;
  private Stage primaryStage;

  public void setInjector(Injector injector) {
    this.injector = injector;
  }

  public void setStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  public Entry<ConnectionSettingsController, BorderPane> loadConnectionSetupWindow(ConnectionSettings settings,
      SettingsListController settingsListController, EditMode editMode) {
    URL url = getClass().getResource("/ui/ConnectionSettings.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    ConnectionSettingsController controller = loader.getController();
    controller.setSettings(settings);
    controller.setSettingsController(settingsListController);
    controller.setEditMode(editMode);

    return new SimpleEntry<>(controller, root);
  }

  public void showSettingsWindow(MainFrameController controller) {
    URL url = getClass().getResource("/ui/SettingsList.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    SettingsListController dialogController = loader.getController();

    ButtonType connectButtonType = new ButtonType("Connect", ButtonData.OK_DONE);
    Dialog<ConnectionSettings> dialog = new Dialog<>();
    dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);
    dialog.setTitle("MongoFX - settings");
    dialog.setResizable(true);

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

  public MainFrameController loadMainWindow() {
    URL url = getClass().getResource("/ui/MainFrame.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    MainFrameController mainFrameController = loader.getController();

    primaryStage.setTitle("MongoFX " + properteisService.getVersion());
    primaryStage.setScene(createScene(root, 800, 600));
    primaryStage.show();
    return mainFrameController;
  }

  private Scene createScene(BorderPane root, double width, double height) {
    Scene scene = new Scene(root, width, height);
    scene.getStylesheets().add(getClass().getResource("/ui/application.css").toExternalForm());
    scene.getStylesheets().add(getClass().getResource("/ui/editor.css").toExternalForm());
    return scene;
  }

  private BorderPane load(URL url, final FXMLLoader loader) {
    BorderPane root;
    try (InputStream in = url.openStream()) {
      root = loader.load(in);
    }
    catch (IOException e) {
      log.error("IOException:",e);
      return null;
    }
    return root;
  }

  private FXMLLoader createLoader(URL url) {
    final FXMLLoader loader = new FXMLLoader();
    loader.setLocation(url);
    loader.setControllerFactory((param) -> injector.getInstance(param));
    return loader;
  }

  public Entry<Node, QueryTabController> buildQueryNode(MongoDbConnection mongoConnection, DbTreeValue dbTreeValue) {
    URL url = getClass().getResource("/ui/QueryTab.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    QueryTabController controller = loader.getController();
    if (dbTreeValue.getValueType() == DbTreeValue.TreeValueType.COLLECTION) {
      controller.setDb(mongoConnection, dbTreeValue.getMongoDatabase(), dbTreeValue.getDisplayValue());
    } else {
      controller.setDb(mongoConnection, dbTreeValue.getMongoDatabase(), null);
    }
    return new SimpleEntry<>(root, controller);
  }

  public Stage getPrimaryStage() {
    return primaryStage;
  }

  public Optional<String> editDocument(String formattedJson, int cursorPosition) {
    Dialog<String> dialog = new Dialog<>();
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    dialog.setTitle("MongoFX - edit document");
    dialog.setResizable(true);
    dialog.getDialogPane().getStylesheets().add(getClass().getResource("/ui/editor.css").toExternalForm());

    CodeArea codeArea = setupEditorArea(formattedJson, dialog, cursorPosition);

    dialog.setResultConverter(bt -> {
      if (ButtonData.OK_DONE == bt.getButtonData()) {
        return codeArea.getText();
      }
      return null;
    });
    return dialog.showAndWait();
  }

  private CodeArea setupEditorArea(String formattedJson, Dialog<String> dialog, int cursorPosition) {
    URL url = getClass().getResource("/ui/Editor.fxml");
    final FXMLLoader loader = createLoader(url);
    BorderPane root = load(url, loader);
    EditorController editorController = loader.getController();
    CodeArea codeArea = editorController.getCodeArea();
    codeArea.setPrefSize(500, 400);
    codeArea.replaceText(formattedJson);
    codeArea.getUndoManager().forgetHistory();

    // stackpane is workaround https://github.com/TomasMikula/RichTextFX/issues/196
    dialog.getDialogPane().setContent(new StackPane(root));
    Platform.runLater(() -> {
      codeArea.selectRange(cursorPosition, cursorPosition);
      codeArea.requestFocus();
    });
    return codeArea;
  }
}
