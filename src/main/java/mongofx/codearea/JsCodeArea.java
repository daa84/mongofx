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
// Copyright (c) Andrey Dubravin, 2016
//
package mongofx.codearea;

import javafx.collections.ObservableList;
import mongofx.js.support.JsFormatter;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.Paragraph;

import java.util.Collection;
import java.util.Objects;

public class JsCodeArea extends CodeArea {
  private static final JsFormatter FORMATTER = new JsFormatter();

  public void formatCode() {
    if (isEditable()) {
      String unformatted = getText();
      String formatted = FORMATTER.beautify(unformatted);
      if (!Objects.equals(unformatted, formatted)) {
        int currentParagraph = getCurrentParagraph();

        replaceText(formatted);

        if (currentParagraph < getParagraphs().size()) {
          selectParagraphEnd(currentParagraph);
        }
      }
    }
  }

  private void selectParagraphEnd(int currentParagraph) {
    int paragraphStartOffset = 0;
    ObservableList<Paragraph<Collection<String>>> paragraphs = getParagraphs();
    for (int i = 0; i < paragraphs.size(); i++) {
      Paragraph<Collection<String>> paragraph = paragraphs.get(i);
      if (i == currentParagraph) {
        int pos = paragraphStartOffset + paragraph.length();
        selectRange(pos, pos);
        break;
      }
      paragraphStartOffset += paragraph.length() + 1 /*\n*/;
    }
  }
}
