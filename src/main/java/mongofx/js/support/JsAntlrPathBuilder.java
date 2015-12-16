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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import mongofx.js.antlr4.parser.ECMAScriptBaseVisitor;
import mongofx.js.antlr4.parser.ECMAScriptLexer;
import mongofx.js.antlr4.parser.ECMAScriptParser;
import org.antlr.v4.runtime.tree.TerminalNode;

public class JsAntlrPathBuilder {
  public static Optional<List<String>> buildPath(String jsCode, int position) {
    if (position <= 0) {
      return Optional.of(Collections.singletonList(""));
    }
    position--;

    ECMAScriptLexer lexer = new ECMAScriptLexer(new ANTLRInputStream(jsCode));
    ECMAScriptParser parser = new ECMAScriptParser(new CommonTokenStream(lexer));

    List<String> path = new ArrayList<>();

    new ScriptVisitor(path, position).visit(parser.program());

    if (path.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(path);
  }

  private static class ScriptVisitor extends ECMAScriptBaseVisitor<Void> {
    private final List<String> path;
    private final int position;
    int dotExpression;
    boolean foundPath;

    public ScriptVisitor(List<String> path, int position) {
      this.path = path;
      this.position = position;
      dotExpression = 0;
      foundPath = false;
    }

    @Override
    public Void visitMemberDotExpression(ECMAScriptParser.MemberDotExpressionContext ctx) {
      dotExpression++;

      super.visitMemberDotExpression(ctx);

      dotExpression--;
      if (dotExpression == 0 && !foundPath) {
        path.clear();
      }

      return null;
    }

    @Override
    public Void visitIdentifierName(ECMAScriptParser.IdentifierNameContext ctx) {
      if (foundPath || dotExpression == 0) {
        return null;
      }
      markFound(ctx.getStart());
      addPath(ctx);

      return super.visitIdentifierName(ctx);
    }

    @Override
    public Void visitArguments(ECMAScriptParser.ArgumentsContext ctx) {
      // TODO: here stack needed
      if (dotExpression > 0) {
        return null;
      }
      return super.visitArguments(ctx);
    }

    @Override
    public Void visitIdentifierExpression(ECMAScriptParser.IdentifierExpressionContext ctx) {
      if (foundPath) {
        return null;
      }

      if (markFound(ctx.getStart())) {
        addPath(ctx);
        return null;
      }

      if (dotExpression == 0) {
        return null;
      }

      addPath(ctx);
      return super.visitIdentifierExpression(ctx);
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
      if (foundPath || dotExpression == 0) {
        return null;
      }

      if (dotExpression > 0 && ".".equals(node.getText())) {
        if (markFound(node.getSymbol())) {
          path.add("");
          return null;
        }
      }
      return super.visitTerminal(node);
    }

    private void addPath(ParserRuleContext ctx) {
      if (ctx.exception == null) {
        if (foundPath) {
          Token start = ctx.getStart();
          int partLength = position + 1 - Math.min(start.getStartIndex(), start.getStopIndex());
          if (partLength > 0) {
            path.add(ctx.getText().substring(0, partLength));
          } else {
            path.add("");
          }
        } else {
          path.add(ctx.getText());
        }
      }
      else {
        path.add(ctx.getText());
      }
    }

    private boolean markFound(Token token) {
      int start = Math.min(token.getStartIndex(), token.getStopIndex());
      int stop = Math.max(token.getStartIndex(), token.getStopIndex());
      if (position >= start && position <= stop) {
        foundPath = true;
      }
      return foundPath;
    }
  }
}
