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

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import mongofx.service.settings.ConnectionSettings;
import mongofx.service.settings.SettingsService;

public class ConnectionSettingsController {
  @Inject
  private SettingsService settingsService;

  private ConnectionSettings settings = new ConnectionSettings();

  @FXML
  private TextField hostField;

  @FXML
  private PasswordField passField;

  @FXML
  private TextField userField;

  private EditMode editMode;

  private SettingsListController settingsListController;

  @FXML
  public void onBack() throws IOException {
    settingsListController.load();
  }

  @FXML
  public void onSave() throws IOException {
    save();
    settingsListController.load();
  }

  public void save() throws IOException {
    if (editMode == EditMode.CREATE) {
      settingsService.getSettings().getConnections().add(settings);
    }
    settingsService.save();
  }

  public void setSettings(ConnectionSettings settings) {
    if (this.settings != null) {
      hostField.textProperty().unbindBidirectional(this.settings.hostProperty());
      userField.textProperty().unbindBidirectional(this.settings.userProperty());
      passField.textProperty().unbindBidirectional(this.settings.passwordProperty());
    }
    this.settings = settings;
    hostField.textProperty().bindBidirectional(settings.hostProperty());
    userField.textProperty().bindBidirectional(settings.userProperty());
    passField.textProperty().bindBidirectional(settings.passwordProperty());
  }

  public void setSettingsController(SettingsListController settingsListController) {
    this.settingsListController = settingsListController;
  }

  public void setEditMode(EditMode editMode) {
    this.editMode = editMode;
  }

  public ConnectionSettings getSettings() {
    return settings;
  }

}
