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

import java.util.*;

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

    ScriptVisitor scriptVisitor = new ScriptVisitor(position);
    scriptVisitor.visit(parser.program());
    return scriptVisitor.getPath();
  }

  private static class ScriptVisitor extends ECMAScriptBaseVisitor<Void> {
    private Stack<DotPath> stack = new Stack<>();

    private final int position;
    boolean foundPath;

    public ScriptVisitor(int position) {
      this.position = position;
      foundPath = false;
    }

    public Optional<List<String>> getPath() {
      if (foundPath) {
        return Optional.of(stack.lastElement().path);
      }
      return Optional.empty();
    }

    @Override
    public Void visitMemberDotExpression(ECMAScriptParser.MemberDotExpressionContext ctx) {
      if (stack.isEmpty()) {
        stack.add(new DotPath());
      }
      stack.lastElement().levelDown();

      super.visitMemberDotExpression(ctx);

      DotPath dotPath = stack.lastElement();
      dotPath.levelUp();
      if (dotPath.isTopLevel() && !foundPath) {
        stack.pop();
      }

      return null;
    }

    @Override
    public Void visitIdentifierName(ECMAScriptParser.IdentifierNameContext ctx) {
      if (foundPath || stack.isEmpty()) {
        return null;
      }
      markFound(ctx.getStart());
      addPath(ctx);

      return super.visitIdentifierName(ctx);
    }

    @Override
    public Void visitArguments(ECMAScriptParser.ArgumentsContext ctx) {
      // function context must be processed here
      if (!stack.isEmpty()) {
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
        if (stack.isEmpty()) {
          stack.add(new DotPath());
        }
      }

      if (stack.isEmpty()) {
        return null;
      }

      addPath(ctx);
      return super.visitIdentifierExpression(ctx);
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
      if (foundPath || stack.isEmpty()) {
        return null;
      }

      if (".".equals(node.getText())) {
        if (markFound(node.getSymbol())) {
          stack.lastElement().addToPath("");
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
            stack.lastElement().addToPath(ctx.getText().substring(0, partLength));
          } else {
            stack.lastElement().addToPath("");
          }
        } else {
          stack.lastElement().addToPath(ctx.getText());
        }
      }
      else {
        stack.lastElement().addToPath(ctx.getText());
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

  private static class DotPath {
    private final List<String> path = new ArrayList<>(3);
    int level = 0;

    public void levelDown() {
      level++;
    }

    public void levelUp() {
      level--;
    }

    public boolean isTopLevel() {
      return level <= 0;
    }

    public void addToPath(String value) {
      path.add(value);
    }
  }
}
