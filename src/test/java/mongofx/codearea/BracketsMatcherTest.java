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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

public class BracketsMatcherTest {

  @Test
  public void testFindPair() {
    HashMap<Character, Character> brackets = new HashMap<>();
    brackets.put('[', ']');
    brackets.put('(', ')');
    BracketsMatcher matcher = new BracketsMatcher(brackets);
    String inString = "[Hello[[[]]] world]";
    int rightBracket = matcher.findPair(inString, 0);
    assertEquals(inString.length() - 1, rightBracket);

    int leftBracket = matcher.findPair(inString, inString.length() - 1);
    assertEquals(0, leftBracket);
  }

  @Test
  public void testFindPairNotFound() {
    HashMap<Character, Character> brackets = new HashMap<>();
    brackets.put('[', ']');
    BracketsMatcher matcher = new BracketsMatcher(brackets);
    String inString = "[Hello[[[]]] world";
    int rightBracket = matcher.findPair(inString, 0);

    assertEquals(-1, rightBracket);
  }

}
