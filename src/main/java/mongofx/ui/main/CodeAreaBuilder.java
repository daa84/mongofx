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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.PopupAlignment;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.EventHandlerHelper;
import org.fxmisc.wellbehaved.event.EventHandlerHelper.Builder;
import org.fxmisc.wellbehaved.event.EventPattern;

import javafx.geometry.Point2D;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;
import mongofx.js.support.JsPathBuilder;
import mongofx.service.AutocompleteService;
import mongofx.service.AutocompleteService.Suggest;

public class CodeAreaBuilder {
  private static final String[] KEYWORDS = new String[]{
    "db", "function", "var", "for", "if", "else"
  };

  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String PAREN_PATTERN = "\\(|\\)";
  private static final String BRACE_PATTERN = "\\{|\\}";
  private static final String BRACKET_PATTERN = "\\[|\\]";
  private static final String SEMICOLON_PATTERN = "\\;";
  private static final String STRING_PATTERN_DOUBLE = "\"([^\"\\\\]|\\\\.)*\"";
  private static final String STRING_PATTERN_SINGLE = "'([^'\\\\]|\\\\.)*'";
  private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

  private static final Pattern PATTERN = Pattern.compile("(?<KEYWORD>" + KEYWORD_PATTERN + ")" //
      + "|(?<PAREN>" + PAREN_PATTERN + ")" //
      + "|(?<BRACE>" + BRACE_PATTERN + ")" //
      + "|(?<BRACKET>" + BRACKET_PATTERN + ")" //
      + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")" //
      + "|(?<STRINGDOUBLE>" + STRING_PATTERN_DOUBLE + ")" //
      + "|(?<STRINGSINGLE>" + STRING_PATTERN_SINGLE + ")" //
      + "|(?<COMMENT>" + COMMENT_PATTERN + ")" //
      );

  private final CodeArea codeArea;
  private final Stage primaryStage;

  public CodeAreaBuilder(CodeArea codeArea, Stage primaryStage) {
    this.codeArea = codeArea;
    this.primaryStage = primaryStage;
  }

  public CodeAreaBuilder setup() {
    codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

    codeArea.textProperty().addListener((obs, oldText, newText) -> {
      codeArea.setStyleSpans(0, computeHighlighting(newText));
    });

    Builder<KeyEvent> onKeyTyped = EventHandlerHelper.startWith(e -> {
      String character = e.getCharacter();
      if ("{".equals(character)) {
        charRight(codeArea, "}");
      }
      else if ("[".equals(character)) {
        charRight(codeArea, "]");
      }
      else if ("(".equals(character)) {
        charRight(codeArea, ")");
      }
      else if ("\"".equals(character)) {
        charRight(codeArea, "\"");
      }
      else if ("'".equals(character)) {
        charRight(codeArea, "'");
      }
    });

    EventHandlerHelper.install(codeArea.onKeyTypedProperty(), onKeyTyped.create());
    return this;
  }

  public CodeAreaBuilder setupAutocomplete(AutocompleteService service) {
    Popup popup = new Popup();
    popup.setAutoHide(true);
    popup.setHideOnEscape(true);

    ListView<Suggest> listView = createAutocompleteListView(service, popup);
    popup.getContent().add(listView);
    codeArea.setPopupWindow(popup);
    codeArea.setPopupAlignment(PopupAlignment.CARET_BOTTOM);
    codeArea.setPopupAnchorOffset(new Point2D(1, 1));

    Builder<KeyEvent> onKeyPressed =
        EventHandlerHelper.on(EventPattern.keyPressed(KeyCode.SPACE, KeyCombination.CONTROL_DOWN)).act(ae -> {
          if (popup.isShowing()) {
            popup.hide();
          }
          else {
            showPopup(service, popup, listView);
          }
        }) //
        .on(EventPattern.keyPressed(KeyCode.PERIOD)).act(ae -> showPopup(service, popup, listView));

    codeArea.textProperty().addListener(text -> {
      if (popup.isShowing()) {
        updateSuggestion(service, popup, listView);
      }
    });
    EventHandlerHelper.install(codeArea.onKeyPressedProperty(), onKeyPressed.create());
    return this;
  }

  private void showPopup(AutocompleteService service, Popup popup, ListView<Suggest> listView) {
    if (!popup.isShowing() && updateSuggestion(service, popup, listView)) {
      popup.show(primaryStage);
    }
  }

  private boolean updateSuggestion(AutocompleteService service, Popup popup, ListView<Suggest> listView) {
    List<Suggest> autocompleteList = buildAutocompleteFromPosition(service);
    if (!autocompleteList.isEmpty()) {
      listView.getItems().setAll(autocompleteList);
      listView.getSelectionModel().select(0);
      return true;
    }
    popup.hide();
    return false;
  }

  private List<Suggest> buildAutocompleteFromPosition(AutocompleteService service) {
    int cursorPos = codeArea.getSelection().getStart();
    String headText = codeArea.getText(0, cursorPos);
    Optional<List<String>> paths = JsPathBuilder.buildPath(headText);
    if (!paths.isPresent()) {
      return Collections.emptyList();
    }

    return service.find(paths.get());
  }

  private ListView<Suggest> createAutocompleteListView(AutocompleteService service, Popup popup) {
    ListView<Suggest> listView = new ListView<>();

    Builder<KeyEvent> popupKeyEvents =
        EventHandlerHelper.on(EventPattern.keyPressed(KeyCode.ESCAPE)).act(e -> popup.hide())//
        .on(EventPattern.keyPressed(KeyCode.ENTER)).act(e -> {
          Suggest selectedItem = listView.getSelectionModel().getSelectedItem();
          if (selectedItem != null) {
            codeArea.replaceText(codeArea.getSelection(), selectedItem.getInserPart());
          }
          popup.hide();
        });
    EventHandlerHelper.install(listView.onKeyPressedProperty(), popupKeyEvents.create());

    return listView;
  }

  private static void charRight(CodeArea codeArea, String ch) {
    IndexRange selection = codeArea.getSelection();
    codeArea.replaceSelection(ch);
    codeArea.selectRange(selection.getStart(), selection.getStart());
  }

  private static StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass = //
          matcher.group("KEYWORD") != null ? "keyword" : //
            matcher.group("PAREN") != null ? "paren" : //
              matcher.group("BRACE") != null ? "brace" : //
                matcher.group("BRACKET") != null ? "bracket" : //
                  matcher.group("SEMICOLON") != null ? "semicolon" : //
                    matcher.group("STRINGSINGLE") != null ? "string" : //
                      matcher.group("STRINGDOUBLE") != null ? "string" : //
                        matcher.group("COMMENT") != null ? "comment" : //
                          null;
      /* never happens */ assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  public CodeAreaBuilder setText(String formatedJson) {
    codeArea.replaceText(formatedJson);
    codeArea.getUndoManager().forgetHistory();
    return this;
  }
}
