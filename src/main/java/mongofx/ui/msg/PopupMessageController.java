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
package mongofx.ui.msg;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class PopupMessageController {

  @FXML
  public BorderPane messagePane;

  @FXML
  private Pane pane;

  private final StringProperty headerMessage = new SimpleStringProperty();
  private final StringProperty message = new SimpleStringProperty();

  public final StringProperty headerMessageProperty() {
    return this.headerMessage;
  }

  public final java.lang.String getHeaderMessage() {
    return this.headerMessageProperty().get();
  }

  public final void setHeaderMessage(final java.lang.String headerMessage) {
    this.headerMessageProperty().set(headerMessage);
  }

  public final StringProperty messageProperty() {
    return this.message;
  }

  public final java.lang.String getMessage() {
    return this.messageProperty().get();
  }

  public final void setMessage(final java.lang.String message) {
    this.messageProperty().set(message);
  }


  public void showAt(Node origin) {
    // TODO: close
    // TODO: relayout
    pane.setVisible(true);
    Bounds originBounds = origin.localToScene(origin.getBoundsInLocal());
    Point2D target = pane.sceneToLocal(originBounds.getMinX(), originBounds.getMinY());
    messagePane.setMaxWidth(pane.getWidth() - originBounds.getMinX() - 5);
    messagePane.layout();
    messagePane.relocate(target.getX(), target.getY() - messagePane.getLayoutBounds().getHeight() - 5);
  }
}
