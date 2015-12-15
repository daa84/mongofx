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
package mongofx.js.support;

import mongofx.js.antlr4.parser.ECMAScriptBaseListener;
import mongofx.js.antlr4.parser.ECMAScriptLexer;
import mongofx.js.antlr4.parser.ECMAScriptParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsAntlrPathBuilder {
  private static final Logger log = LoggerFactory.getLogger(JsAntlrPathBuilder.class);

  public static Optional<List<String>> buildPath(String jsCode, int position) {
    ECMAScriptLexer lexer = new ECMAScriptLexer(new ANTLRInputStream(jsCode));
    ECMAScriptParser parser = new ECMAScriptParser(new CommonTokenStream(lexer));

    List<String> path = new ArrayList<>();

    parser.addParseListener(new ECMAScriptBaseListener() {
      @Override
      public void exitMemberDotExpression(ECMAScriptParser.MemberDotExpressionContext ctx) {
      }

      @Override
      public void enterMemberDotExpression(ECMAScriptParser.MemberDotExpressionContext ctx) {
      }

      @Override
      public void exitIdentifierName(ECMAScriptParser.IdentifierNameContext ctx) {
      }

      @Override
      public void exitIdentifierExpression(ECMAScriptParser.IdentifierExpressionContext ctx) {
        int startIndex = ctx.getStart().getStartIndex();
        int stopIndex = ctx.getStart().getStopIndex();
        if (position >= startIndex && position <= stopIndex) {

        }
      }
    });
    parser.program();

    if (path.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(path);
  }
}
