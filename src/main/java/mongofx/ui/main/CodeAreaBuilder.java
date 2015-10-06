package mongofx.ui.main;

import java.util.Collection;
import java.util.Collections;
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
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class CodeAreaBuilder {
  private static final String[] KEYWORDS = new String[]{
    "db"
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

  public static void setup(Stage primaryStage, CodeArea codeArea) {
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

    Builder<KeyEvent> onKeyPressed = setupAutocomplete(primaryStage, codeArea);

    EventHandlerHelper.install(codeArea.onKeyTypedProperty(), onKeyTyped.create());
    EventHandlerHelper.install(codeArea.onKeyPressedProperty(), onKeyPressed.create());
  }

  private static Builder<KeyEvent> setupAutocomplete(Stage primaryStage, CodeArea codeArea) {
    Popup popup = new Popup();
    popup.setAutoHide(true);
    popup.getContent().add(new Button("Autocomplete here"));
    codeArea.setPopupWindow(popup);
    codeArea.setPopupAlignment(PopupAlignment.CARET_BOTTOM);
    codeArea.setPopupAnchorOffset(new Point2D(1, 1));

    return EventHandlerHelper.on(EventPattern.keyPressed(KeyCode.SPACE, KeyCombination.CONTROL_DOWN)).act(ae -> {
      if (popup.isShowing()) {
        popup.hide();
      }
      else {
        popup.show(primaryStage);
      }
    });
  }

  private static void charRight(CodeArea codeArea, String ch) {
    IndexRange selection = codeArea.getSelection();
    codeArea.insertText(selection.getStart(), ch);
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
}
