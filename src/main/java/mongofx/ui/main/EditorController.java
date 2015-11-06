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

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.wellbehaved.event.EventHandlerHelper;
import org.fxmisc.wellbehaved.event.EventPattern;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import mongofx.codearea.CodeAreaBuilder;

public class EditorController {

  @FXML
  public CodeArea codeArea;
  @FXML
  public TextField searchField;
  @FXML
  public Label infoLabel;
  @FXML
  public HBox searchBox;

  @Inject
  private UIBuilder uiBuilder;

  @FXML
  public void initialize() {
    new CodeAreaBuilder(codeArea, uiBuilder.getPrimaryStage()).setup();
    EventHandlerHelper.Builder<KeyEvent> keyEvent = EventHandlerHelper//
        .on(EventPattern.keyPressed(KeyCode.F, KeyCombination.CONTROL_DOWN)).act(e -> openSearchBox()) //
        .on(EventPattern.keyPressed(KeyCode.ESCAPE)).act(e -> closeSearchBox());
    EventHandlerHelper.install(codeArea.onKeyPressedProperty(), keyEvent.create());
    infoLabel.setText("");

    keyEvent = EventHandlerHelper.on(EventPattern.keyPressed(KeyCode.ESCAPE)).act(e -> closeSearchBox());
    EventHandlerHelper.install(searchBox.onKeyPressedProperty(), keyEvent.create());

    searchBox.managedProperty().bind(searchBox.visibleProperty());
  }

  @FXML
  private void closeSearchBox() {
    searchBox.setVisible(false);
    if (!codeArea.isFocused()) {
      codeArea.requestFocus();
    }
  }

  private void openSearchBox() {
    searchBox.setVisible(true);
    searchField.requestFocus();
  }

  public void clear() {
    codeArea.clear();
    infoLabel.setText("");
  }

  public void replaceText(String s) {
    codeArea.replaceText(s);
  }

  public void selectRange(int caretPosition) {
    codeArea.selectRange(0, caretPosition);
  }

  public void searchNext() {
    infoLabel.setText("");

    String text = codeArea.getText();
    String searchText = searchField.getText();
    if (searchText == null || searchText.isEmpty()) {
      showNothingFound();
      return;
    }
    int nextIndex = text.indexOf(searchText, codeArea.getSelection().getEnd());
    if (nextIndex < 0) {
      nextIndex = text.indexOf(searchText);
    }

    if (nextIndex < 0) {
      showNothingFound();
      return;
    }

    codeArea.selectRange(nextIndex, nextIndex + searchText.length());
  }

  private void showNothingFound() {
    infoLabel.setText("No matching found");
  }

  public void searchPrevious() {
    infoLabel.setText("");

    String text = codeArea.getText();
    String searchText = searchField.getText();
    if (searchText == null || searchText.isEmpty()) {
      showNothingFound();
      return;
    }
    int previousIndex = text.lastIndexOf(searchText, codeArea.getSelection().getStart() - 1);
    if (previousIndex < 0) {
      previousIndex = text.indexOf(searchText);
    }

    if (previousIndex < 0) {
      showNothingFound();
      return;
    }

    codeArea.selectRange(previousIndex, previousIndex + searchText.length());
  }

  public void onSearchFieldKeyPressed(KeyEvent event) {
    if (event.getCode() == KeyCode.ENTER) {
      searchNext();
      event.consume();
    }
  }

  public CodeArea getCodeArea() {
    return codeArea;
  }

  public void disableEdit() {
    codeArea.setEditable(false);
  }
}
