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
package mongofx.javafx;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;

public class FocusFixer {
  public static void requestFocus(Node node) {
    requestFocus(node, 0);
  }

  public static void requestFocus(Node node, int level) {
    if (level > 10) {
      return;
    }
    Scene scene = node.getScene();
    if (scene != null) {
      node.requestFocus();
    } else {
      Platform.runLater(()-> requestFocus(node, level + 1));
    }
  }
}
