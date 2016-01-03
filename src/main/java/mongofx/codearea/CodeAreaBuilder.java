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
package mongofx.codearea;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mongofx.js.support.JsAntlrPathBuilder;
import mongofx.js.support.JsFormatter;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.PopupAlignment;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.EventHandlerHelper;
import org.fxmisc.wellbehaved.event.EventHandlerHelper.Builder;
import org.fxmisc.wellbehaved.event.EventPattern;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;
import mongofx.service.suggest.Suggest;
import mongofx.service.suggest.SuggestContext;
import mongofx.ui.main.AutocompletionEngine;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

public class CodeAreaBuilder {
  private static final String[] KEYWORDS = new String[]{
    "db", "function", "var", "for", "if", "else", "return", "while", "this", "true", "false"
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

  private static final BracketsMatcher BRACKETS_MATCHER;
  private static final JsFormatter FORMATTER = new JsFormatter();

  static {
    HashMap<Character, Character> brackets = new HashMap<>();
    brackets.put('[', ']');
    brackets.put('{', '}');
    brackets.put('(', ')');
    BRACKETS_MATCHER = new BracketsMatcher(brackets);
  }

  private final CodeArea codeArea;
  private final Stage primaryStage;

  public CodeAreaBuilder(CodeArea codeArea, Stage primaryStage) {
    this.codeArea = codeArea;
    this.primaryStage = primaryStage;
  }

  public CodeAreaBuilder setup() {
    codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

    codeArea.textProperty().addListener((obs, oldText, newText) -> {
      updateStyles(newText);
    });

    codeArea.caretPositionProperty().addListener(c -> {
      updateStyles(codeArea.getText());
    });

    Builder<KeyEvent> onKeyTyped = EventHandlerHelper.startWith(e -> {
      final String character = e.getCharacter();
      if ("{".equals(character)) {
        charRight(e, codeArea, "}");
      }
      else if ("[".equals(character)) {
        charRight(e, codeArea, "]");
      }
      else if ("(".equals(character)) {
        charRight(e, codeArea, ")");
      }
      else if ("\"".equals(character)) {
        final int caretPosition = codeArea.getCaretPosition();
        final String text = codeArea.getText();
        if (!isLeftCharOpen(text, caretPosition, '"') && !isRightCharOpen(text, caretPosition, '"')) {
          charRight(e, codeArea, "\"");
        }
      }
      else if ("'".equals(character)) {
        final int caretPosition = codeArea.getCaretPosition();
        final String text = codeArea.getText();
        if (!isLeftCharOpen(text, caretPosition, '\'') && !isRightCharOpen(text, caretPosition, '\'')) {
          charRight(e, codeArea, "'");
        }
      }
    });

    Builder<KeyEvent> onKeyPressed = EventHandlerHelper.on(keyPressed(KeyCode.TAB)).act((e) -> {
      codeArea.replaceSelection("    ");
    }).on(keyPressed(KeyCode.ENTER)).act(e -> {
      if (leftCharIs('{') && rightCharIs('}') ||
          leftCharIs('[') && rightCharIs(']')) {
        codeArea.replaceSelection("\n\n");
        int caretPosition = codeArea.getCaretPosition() - 1;
        if (caretPosition >= 0) {
          codeArea.selectRange(caretPosition, caretPosition);
        }
      } else {
        codeArea.replaceSelection("\n");
      }
    }).on(keyPressed(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN)).act(e -> formatCode());

    EventHandlerHelper.install(codeArea.onKeyTypedProperty(), onKeyTyped.create());
    EventHandlerHelper.install(codeArea.onKeyPressedProperty(), onKeyPressed.create());
    return this;
  }

  private void formatCode() {
    if (codeArea.isEditable()) {
      codeArea.replaceText(FORMATTER.beautify(codeArea.getText()));
    }
  }

  private boolean rightCharIs(char c) {
    String text = codeArea.getText();
    for (int i = codeArea.getCaretPosition(); i < text.length(); i++) {
      char testC = text.charAt(i);
      if (testC == c) {
        return true;
      }
      if (testC != ' ') {
        return false;
      }
    }
    return false;
  }

  private boolean leftCharIs(char c) {
    String text = codeArea.getText();
    for (int i = codeArea.getCaretPosition() - 1; i >= 0; i--) {
      char testC = text.charAt(i);
      if (testC == c) {
        return true;
      }
      if (testC != ' ') {
        return false;
      }
    }
    return false;
  }

  int oldCaretPosition = 0;
  String oldText = null;

  private SuggestContext suggestContext;

  private void updateStyles(String newText) {
    int caretPosition = codeArea.getCaretPosition();
    if (newText != oldText // compare by object link not by text
        || oldCaretPosition != caretPosition) {
      oldText = newText;
      oldCaretPosition = caretPosition;
      codeArea.setStyleSpans(0, computeHighlighting(newText, caretPosition));
    }
  }

  public CodeAreaBuilder setupAutocomplete(AutocompletionEngine service, String collectionName) {
    this.suggestContext = new SuggestContext(collectionName, codeArea);
    Popup popup = new Popup();
    popup.setAutoHide(true);
    popup.setHideOnEscape(true);

    ListView<Suggest> listView = createAutocompleteListView(popup);
    popup.getContent().add(listView);
    codeArea.setPopupWindow(popup);
    codeArea.setPopupAlignment(PopupAlignment.CARET_BOTTOM);
    codeArea.setPopupAnchorOffset(new Point2D(1, 1));

    Builder<KeyEvent> onKey =
        EventHandlerHelper.on(keyPressed(KeyCode.SPACE, KeyCombination.CONTROL_DOWN)).act(ae -> {
          if (popup.isShowing()) {
            popup.hide();
          }
          else {
            showPopup(service, popup, listView);
          }
        }) //
        .on(EventPattern.keyReleased(KeyCode.PERIOD)).act(ae -> showPopup(service, popup, listView));

    codeArea.textProperty().addListener(text -> {
      if (popup.isShowing()) {
        updateSuggestion(service, popup, listView);
      }
    });
    EventHandler<KeyEvent> onKeyHandler = onKey.create();
    EventHandlerHelper.install(codeArea.onKeyPressedProperty(), onKeyHandler);
    EventHandlerHelper.install(codeArea.onKeyReleasedProperty(), onKeyHandler);
    return this;
  }

  private void showPopup(AutocompletionEngine service, Popup popup, ListView<Suggest> listView) {
    if (!popup.isShowing() && updateSuggestion(service, popup, listView)) {
      popup.show(primaryStage);
    }
  }

  private boolean updateSuggestion(AutocompletionEngine service, Popup popup, ListView<Suggest> listView) {
    List<Suggest> autocompleteList = buildAutocompleteFromPosition(service);
    if (!autocompleteList.isEmpty()) {
      listView.getItems().setAll(autocompleteList);
      listView.getSelectionModel().select(0);
      return true;
    }
    popup.hide();
    return false;
  }

  private List<Suggest> buildAutocompleteFromPosition(AutocompletionEngine service) {
    int cursorPos = codeArea.getSelection().getStart();
    Optional<List<String>> paths = JsAntlrPathBuilder.buildPath(codeArea.getText(), cursorPos);
    if (!paths.isPresent()) {
      return Collections.emptyList();
    }

    return service.find(paths.get());
  }

  private ListView<Suggest> createAutocompleteListView(Popup popup) {
    ListView<Suggest> listView = new ListView<>();

    Builder<KeyEvent> popupKeyEvents =
        EventHandlerHelper.on(keyPressed(KeyCode.ESCAPE)).act(e -> popup.hide())//
        .on(keyPressed(KeyCode.ENTER)).act(e -> applySelected(popup, listView));
    EventHandlerHelper.install(listView.onKeyPressedProperty(), popupKeyEvents.create());

    listView.setOnMouseClicked(e -> {
      if (e.getClickCount() == 2) applySelected(popup, listView);
    });

    return listView;
  }

  private void applySelected(Popup popup, ListView<Suggest> listView) {
    Suggest selectedItem = listView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      selectedItem.apply(suggestContext);
    }
    popup.hide();
  }

  static boolean isLeftCharOpen(String in, int pos, char ch) {
    if (in.length() == 0) {
      return false;
    }

    if (pos > in.length()) {
      throw new IllegalStateException("pos must be > in.length");
    }

    boolean open = false;
    for (int i = 0; i < pos; i++) {
      if (in.charAt(i) == ch) {
        if (open) {
          open = !(i == 0 || in.charAt(i - 1) != '\\');
        }
        else {
          open = true;
        }
      }
    }
    return open;
  }

  static boolean isRightCharOpen(String in, int pos, char ch) {
    if (in.length() == 0 || pos == in.length()) {
      return false;
    }

    if (pos > in.length()) {
      throw new IllegalStateException("pos must be > in.length");
    }

    boolean open = false;
    for (int i = pos; i < in.length(); i++) {
      if (in.charAt(i) == ch) {
        if (open) {
          open = !(i == 0 || in.charAt(i - 1) != '\\');
        }
        else {
          open = true;
        }
      }
    }
    return open;
  }

  private static void charRight(KeyEvent e, CodeArea codeArea, String ch) {
    IndexRange selection = codeArea.getSelection();
    codeArea.replaceSelection(e.getCharacter() + ch);
    int cursorPosition = selection.getStart() + 1;
    codeArea.selectRange(cursorPosition, cursorPosition);
    e.consume();
  }

  private StyleSpans<Collection<String>> computeHighlighting(String text, int caretPosition) {
    int bracketMatchPosition = BRACKETS_MATCHER.findPair(text, caretPosition);

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
      int matchEnd = matcher.end();
      int matchLength = matchEnd - matcher.start();

      if (bracketMatchPosition >= 0 && //
          (matchEnd - 1 == caretPosition ||
          matchEnd - 1 == bracketMatchPosition)) {
        spansBuilder.add(Arrays.asList(styleClass, "breacket-highlight"), matchLength);
      } else {
        spansBuilder.add(Collections.singleton(styleClass), matchLength);
      }

      lastKwEnd = matchEnd;
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
