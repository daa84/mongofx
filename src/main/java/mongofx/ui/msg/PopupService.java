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

import com.google.inject.Singleton;

import javafx.scene.Node;

@Singleton
public class PopupService {
  private PopupMessageController popupMessageController;
  private Node origin;

  public void register(PopupMessageController popupMessageController, Node origin) {
    this.popupMessageController = popupMessageController;
    this.origin = origin;
  }

  public void showInfo(String message) {
    showInfo("", message);
  }

  public void showInfo(String headerMessage, String message) {
    popupMessageController.setHeaderMessage(headerMessage);
    popupMessageController.setMessage(message);
    popupMessageController.showInfoAt(origin);
  }

  public void showError(String message) {
    showError("", message);
  }

  public void showError(String headerMessage, String message) {
    popupMessageController.setHeaderMessage(headerMessage);
    popupMessageController.setMessage(message);
    popupMessageController.showErrorAt(origin);
  }
}
