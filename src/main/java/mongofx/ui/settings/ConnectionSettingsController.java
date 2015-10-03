package mongofx.ui.settings;

import java.io.IOException;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import mongofx.settings.ConnectionSettings;
import mongofx.settings.SettingsService;
import mongofx.ui.main.UIBuilder;

public class ConnectionSettingsController {
  @Inject
  private UIBuilder uiBuilder;

  @Inject
  private SettingsService settingsService;

  private ConnectionSettings settings = new ConnectionSettings();

  @FXML
  TextField hostField;

  @FXML
  PasswordField passField;

  @FXML
  TextField userField;

  @FXML
  public void back() throws IOException {
    uiBuilder.loadSettingsWindow();
  }

  @FXML
  public void save() throws IOException {
    settingsService.getSettings().getConnections().add(settings);
    settingsService.save();
    uiBuilder.loadSettingsWindow();
  }

  public void setSettings(ConnectionSettings settings) {
    if (this.settings != null) {
      hostField.textProperty().unbindBidirectional(this.settings.hostProperty());
    }
    this.settings = settings;
    hostField.textProperty().bindBidirectional(settings.hostProperty());
    userField.textProperty().bindBidirectional(settings.userProperty());
    passField.textProperty().bindBidirectional(settings.passwordProperty());
  }

}
