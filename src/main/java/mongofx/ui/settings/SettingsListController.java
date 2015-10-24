// This file is part of MongoFX.
//
// MongoFX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
// Foobar is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

//
// Copyright (c) Andrey Dubravin, 2015
//
package mongofx.ui.settings;

import java.io.IOException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import mongofx.settings.ConnectionSettings;
import mongofx.settings.SettingsService;
import mongofx.ui.main.UIBuilder;

public class SettingsListController {
  private static final Logger log = LoggerFactory.getLogger(SettingsListController.class);
  @Inject
  private SettingsService settingsService;

  @Inject
  private UIBuilder uiBuilder;

  private SimpleListProperty<ConnectionSettings> settingsList = new SimpleListProperty<>();

  @FXML
  private TableView<ConnectionSettings> settingsListTable;

  private Dialog<ConnectionSettings> dialog;

  private Node settingsListContent;
  private ConnectionSettingsController connectionSettingsController;

  public final SimpleListProperty<ConnectionSettings> settingsListProperty() {
    return this.settingsList;
  }

  public final javafx.collections.ObservableList<mongofx.settings.ConnectionSettings> getSettingsList() {
    return this.settingsListProperty().get();
  }

  public final void setSettingsList(
      final javafx.collections.ObservableList<mongofx.settings.ConnectionSettings> settingsList) {
    this.settingsListProperty().set(settingsList);
  }

  @FXML
  protected void initialize() {
    if (settingsService.isEmpty()) {
      settingsList.clear();
    }
    else {
      setSettingsList(FXCollections.observableArrayList(settingsService.getSettings().getConnections()));
      if (!getSettingsList().isEmpty()) {
        settingsListTable.getSelectionModel().select(0);
      }
    }
  }

  @FXML
  public void createConnection() throws IOException {
    Entry<ConnectionSettingsController, BorderPane> entry =
        uiBuilder.loadConnectionSetupWindow(new ConnectionSettings(), this, EditMode.CREATE);
    dialog.getDialogPane().setContent(entry.getValue());
    connectionSettingsController = entry.getKey();
    changeConnectButton("Save and connect");
  }

  @FXML
  public void editConnection() throws IOException {
    ConnectionSettings selected = getSelected();
    if (selected != null) {
      Entry<ConnectionSettingsController, BorderPane> entry =
          uiBuilder.loadConnectionSetupWindow(selected, this, EditMode.EDIT);
      dialog.getDialogPane().setContent(entry.getValue());
      connectionSettingsController = entry.getKey();
      changeConnectButton("Save and connect");
    }
  }

  @FXML
  public void deleteConnection() {
    ConnectionSettings selected = getSelected();
    if (selected != null) {
      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setTitle("Remove connection");
      alert.setHeaderText("You want to remove connection to '" + selected.getHost() + "'?");
      alert.showAndWait().ifPresent(b -> {
        if (b == ButtonType.OK) {
          settingsService.getSettings().getConnections().remove(selected);
          try {
            settingsService.save();
            initialize();
          }
          catch (Exception e) {
            log.error("Error save settings", e);
          }
        }
      });
    }
  }

  private void changeConnectButton(String text) {
    dialog.getDialogPane().getButtonTypes().set(0, new ButtonType(text, ButtonData.OK_DONE));
  }

  @FXML
  public void tableClicked(MouseEvent event) throws IOException {
    if (event.getClickCount() == 2) {
      dialog.setResult(getSelected());
      dialog.close();
    }
  }

  public ConnectionSettings getSelected() {
    return settingsListTable.getSelectionModel().getSelectedItem();
  }

  public void setDialog(Dialog<ConnectionSettings> dialog) {
    this.dialog = dialog;
    settingsListContent = dialog.getDialogPane().getContent();
  }

  public void load() {
    initialize();
    dialog.getDialogPane().setContent(settingsListContent);
    changeConnectButton("Save");
  }

  public void saveIfSettingsChanged() throws IOException {
    if (dialog.getDialogPane().getContent() != settingsListContent) {
      connectionSettingsController.save();
    }
  }
}
