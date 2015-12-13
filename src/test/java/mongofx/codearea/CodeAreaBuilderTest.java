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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CodeAreaBuilderTest {

  @Test
  public void openLeftCheckTest() {
    assertFalse(CodeAreaBuilder.isLeftCharOpen("", 0, '\''));
    assertFalse(CodeAreaBuilder.isLeftCharOpen("'", 0, '\''));
    assertTrue(CodeAreaBuilder.isLeftCharOpen("'", 1, '\''));
    assertTrue(CodeAreaBuilder.isLeftCharOpen("''", 1, '\''));
    assertFalse(CodeAreaBuilder.isLeftCharOpen("''", 2, '\''));
    String someText = "'some text here'";
    assertTrue(CodeAreaBuilder.isLeftCharOpen(someText, someText.length() - 1, '\''));
  }

  @Test
  public void openRightCheckTest() {
    assertFalse(CodeAreaBuilder.isRightCharOpen("", 0, '\''));
    assertTrue(CodeAreaBuilder.isRightCharOpen("'", 0, '\''));
    assertFalse(CodeAreaBuilder.isRightCharOpen("'", 1, '\''));
    assertTrue(CodeAreaBuilder.isRightCharOpen("''", 1, '\''));
    assertFalse(CodeAreaBuilder.isRightCharOpen("''", 2, '\''));
    String someText = "'some text here'asdf'";
    assertTrue(CodeAreaBuilder.isRightCharOpen(someText, someText.length() - 1, '\''));
  }

}
