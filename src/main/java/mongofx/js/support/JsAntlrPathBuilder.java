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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import mongofx.js.antlr4.parser.ECMAScriptBaseVisitor;
import mongofx.js.antlr4.parser.ECMAScriptLexer;
import mongofx.js.antlr4.parser.ECMAScriptParser;

public class JsAntlrPathBuilder {
  public static Optional<List<String>> buildPath(String jsCode, int position) {
    ECMAScriptLexer lexer = new ECMAScriptLexer(new ANTLRInputStream(jsCode));
    ECMAScriptParser parser = new ECMAScriptParser(new CommonTokenStream(lexer));

    List<String> path = new ArrayList<>();

    new ECMAScriptBaseVisitor<Void>() {
      int dotExpression = 0;
      boolean findedPath = false;

      @Override
      public Void visitMemberDotExpression(mongofx.js.antlr4.parser.ECMAScriptParser.MemberDotExpressionContext ctx) {
        dotExpression++;

        super.visitMemberDotExpression(ctx);

        dotExpression--;
        if (dotExpression == 0 && !findedPath) {
          path.clear();
        }

        return null;
      }

      @Override
      public Void visitIdentifierName(mongofx.js.antlr4.parser.ECMAScriptParser.IdentifierNameContext ctx) {
        if (findedPath || dotExpression == 0) {
          return null;
        }
        markFinded(ctx.getStart());
        path.add(ctx.getText());

        return super.visitIdentifierName(ctx);
      }

      @Override
      public Void visitArguments(mongofx.js.antlr4.parser.ECMAScriptParser.ArgumentsContext ctx) {
        // TODO: here stack needed
        if (dotExpression > 0) {
          return null;
        }
        return super.visitArguments(ctx);
      };

      @Override
      public Void visitIdentifierExpression(mongofx.js.antlr4.parser.ECMAScriptParser.IdentifierExpressionContext ctx) {
        if (findedPath || dotExpression == 0) {
          return null;
        }
        markFinded(ctx.getStart());
        path.add(ctx.getText());
        return super.visitIdentifierExpression(ctx);
      }

      private void markFinded(Token start) {
        int startIndex = start.getStartIndex();
        int stopIndex = start.getStopIndex();
        if (position >= startIndex && position <= stopIndex) {
          findedPath = true;
        }
      }
    }.visit(parser.program());

    if (path.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(path);
  }
}
