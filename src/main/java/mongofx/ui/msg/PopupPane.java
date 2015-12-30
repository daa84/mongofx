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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.List;

public class PopupPane extends Pane {
  private ObjectProperty<Node> anchor = new SimpleObjectProperty<Node>(this, "anchor", null) {
    @Override
    protected void invalidated() {
      requestLayout();
    }
  };

  @Override
  protected void layoutChildren() {
    List<Node> children = getManagedChildren();
    if (children.size() > 1) {
      throw new IllegalStateException("Can position only single child");
    }

    Node anchor = getAnchor();
    for (final Node node : children) {
        if (anchor != null) {
          Bounds originBounds = anchor.localToScene(anchor.getBoundsInLocal());
          Point2D target = sceneToLocal(originBounds.getMinX(), originBounds.getMinY());

          double nodeWidth = Math.min(node.prefWidth(Region.USE_COMPUTED_SIZE), getWidth() - originBounds.getMinX() - 5 - 5);

          node.resize(nodeWidth, node.prefHeight(nodeWidth));
          node.relocate(target.getX() + 5, target.getY() - node.getLayoutBounds().getHeight() - 5);
        } else {
          node.autosize();
        }
    }
  }

  public Node getAnchor() {
    return anchor.get();
  }

  public ObjectProperty<Node> anchorProperty() {
    return anchor;
  }

  public void setAnchor(Node anchor) {
    this.anchor.set(anchor);
  }
}
