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
package mongofx.codearea;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BracketsMatcher {

  private final Map<Character, Character> openBrackets;
  private final Map<Character, Character> closeBrackets;

  public BracketsMatcher(Map<Character, Character> brackets) {
    openBrackets = new HashMap<>(brackets.size());
    closeBrackets = new HashMap<>(brackets.size());

    for (Entry<Character, Character> entry : brackets.entrySet()) {
      openBrackets.put(entry.getKey(), entry.getValue());
      closeBrackets.put(entry.getValue(), entry.getKey());
    }
  }

  public int findPair(String text, int position) {
    if (text == null || text.isEmpty() || text.length() <= position) {
      return -1;
    }
    Character bracket = text.charAt(position);
    Character closeBracket = openBrackets.get(bracket);
    if (closeBracket != null) {
      return findRight(text, position, bracket, closeBracket);
    }
    Character openBracket = closeBrackets.get(bracket);
    if (openBracket != null) {
      return findLeft(text, position, bracket, openBracket);
    }
    return -1;
  }

  private int findLeft(String text, int position, char closeBracket, char openBracket) {
    int closeBrackets = 0;
    for(int i = position - 1; i >= 0; i--) {
      char ch = text.charAt(i);
      if (ch == openBracket) {
        if (closeBrackets == 0) {
          return i;
        }
        closeBrackets--;
      }
      else if (ch == closeBracket) {
        closeBrackets++;
      }
    }
    return -1;
  }

  private int findRight(String text, int position, char openBracket, char closeBracket) {
    int openBrackets = 0;
    for(int i = position + 1; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (ch == closeBracket) {
        if (openBrackets == 0) {
          return i;
        }
        openBrackets--;
      }
      else if (ch == openBracket) {
        openBrackets++;
      }
    }
    return -1;
  }
}
