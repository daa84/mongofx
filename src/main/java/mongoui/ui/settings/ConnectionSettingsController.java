package mongoui.ui.settings;

import java.io.IOException;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mongoui.settings.ConnectionSettings;
import mongoui.settings.SettingsService;
import mongoui.ui.main.UIBuilder;

public class ConnectionSettingsController {
  @Inject
  private UIBuilder uiBuilder;

  @Inject
  private SettingsService settingsService;

  private ConnectionSettings settings = new ConnectionSettings();

  @FXML
  TextField hostField;

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
  }

}
