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
package mongofx.ui.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.reactfx.EventStreams;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import mongofx.service.MongoService;
import mongofx.settings.ConnectionSettings;
import mongofx.ui.dbtree.DbTreeValue;
import mongofx.ui.dbtree.DbTreeValue.TreeValueType;
import mongofx.ui.dbtree.TreeController;

public class MainFrameController {
  @Inject
  private UIBuilder uiBuilder;

  @Inject
  private TreeController treeController;

  @Inject
  private MongoService mongoService;

  @FXML
  private TreeView<DbTreeValue> treeView;

  @FXML
  private TabPane queryTabs;

  private final Map<Node, QueryTabController> tabData = new HashMap<>();

  @FXML
  protected void initialize() {
    treeController.initialize(treeView);
    EventStreams.simpleChangesOf(queryTabs.getTabs())
        .subscribe(e -> e.getRemoved().stream().forEach(t -> tabData.remove(t.getContent())));
  }

  public void addConnectionSettings(ConnectionSettings connectionSettings) {
    treeController.addDbConnect(mongoService.connect(connectionSettings));
  }

  @FXML
  public void treeViewClicked(MouseEvent ev) throws IOException {
    if (ev.getClickCount() == 2) {
      openTab();
    }
  }

  private void openTab() throws IOException {
    TreeItem<DbTreeValue> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      DbTreeValue value = selectedItem.getValue();
      if (value.getValueType() == TreeValueType.COLLECTION) {
        Entry<Node, QueryTabController> tabEntry = uiBuilder.buildQueryNode(value);
        tabData.put(tabEntry.getKey(), tabEntry.getValue());
        queryTabs.getTabs().add(new Tab(value.getDisplayValue(), tabEntry.getKey()));
        queryTabs.getSelectionModel().selectLast();
        tabEntry.getValue().startTab();
      }
    }
  }

  @FXML
  public void runCommand() {
    Tab selectedTab = queryTabs.getSelectionModel().getSelectedItem();
    if (selectedTab != null) {
      tabData.get(selectedTab.getContent()).executeScript();
    }
  }

  @FXML
  public void showConnectionSettings() throws IOException {
    uiBuilder.showSettingsWindow(this);
  }
}
