package mongofx.ui.settings;

import java.io.IOException;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import mongofx.settings.ConnectionSettings;
import mongofx.settings.SettingsService;

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

}
