package mongoui.ui.settings;

import java.io.IOException;

import com.google.inject.Inject;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import mongoui.settings.ConnectionSettings;
import mongoui.settings.SettingsService;
import mongoui.ui.main.UIBuilder;

public class SettingsListController {
  @Inject
  private SettingsService settingsService;

  @Inject
  private UIBuilder uiBuilder;

  private SimpleListProperty<ConnectionSettings> settingsList = new SimpleListProperty<>();

  @FXML
  private TableView<ConnectionSettings> settingsListTable;

  public final SimpleListProperty<ConnectionSettings> settingsListProperty() {
    return this.settingsList;
  }

  public final javafx.collections.ObservableList<mongoui.settings.ConnectionSettings> getSettingsList() {
    return this.settingsListProperty().get();
  }

  public final void setSettingsList(
      final javafx.collections.ObservableList<mongoui.settings.ConnectionSettings> settingsList) {
    this.settingsListProperty().set(settingsList);
  }

  @FXML
  protected void initialize() {
    if (settingsService.isEmpty()) {
      settingsList.clear();
    }
    else {
      setSettingsList(FXCollections.observableArrayList(settingsService.getSettings().getConnections()));
    }
  }

  @FXML
  public void createConnection() throws IOException {
    uiBuilder.loadConnectionSetupWindow(new ConnectionSettings());
  }

  @FXML
  public void tableClicked(MouseEvent event) throws IOException {
    if (event.getClickCount() == 2) {
      ConnectionSettings selectedItem = settingsListTable.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        uiBuilder.loadMainWindow(selectedItem);
      }
    }
  }
}
