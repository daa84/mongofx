package mongofx.js.support;

import java.util.regex.Pattern;

public class JsCallPathTokenizer {
  private static Pattern jsIdPattern = Pattern.compile("[$A-Z_][0-9A-Z_$]*", Pattern.CASE_INSENSITIVE);

  private char[] input;
  private int pos;

  public JsCallPathTokenizer(String input) {
    super();
    this.input = input.toCharArray();
    pos = input.length() - 1;
  }

  public Token next() {
    StringBuilder tokenString = new StringBuilder();

    skipSpace();
    if (pos < 0) {
      return new Token(true, false);
    }
    if (input[pos] == '.') {
      pos--;
      return new Token(true, false);
    }

    // function parameters
    if (input[pos] == ')') {
      pos--;
      while (pos >= 0 && input[pos] != '(') {
        pos--;
      }
      pos--;
      if (pos < 0) {
        return new Token(true, true);
      }

      skipSpace();
      if (pos < 0) {
        return new Token(true, true);
      }
    }

    boolean last = false;
    while (true) {
      if (isSeparatorChar()) {
        skipSpace();
        if (pos < 0 || input[pos] != '.') {
          last = true;
        }
        pos--;
        break;
      }
      tokenString.append(input[pos]);
      pos--;
      if (pos < 0) {
        break;
      }
    }

    // check js id is valid
    tokenString.reverse();
    if (!jsIdPattern.matcher(tokenString).matches()) {
      return new Token(true, true);
    }
    return new Token(tokenString.toString(), last);
  }

  private boolean isSeparatorChar() {
    char ch = input[pos];
    return !Character.isAlphabetic(ch) && !Character.isDigit(ch) && ch != '_' && ch != '$';
  }

  private void skipSpace() {
    while (pos >= 0 && Character.isWhitespace(input[pos])) {
      pos--;
    }
  }

  public static class Token {
    private boolean error = false;
    private boolean empty = false;
    private boolean last = false;
    private String fieldName;

    public Token(String fieldName, boolean last) {
      super();
      this.fieldName = fieldName;
      this.last = last;
    }

    public Token(boolean empty, boolean error) {
      super();
      fieldName = "";
      this.error = error;
      this.empty = empty;
    }

    public boolean isError() {
      return error;
    }

    public boolean isEmpty() {
      return empty;
    }

    public String getFieldName() {
      return fieldName;
    }

    public boolean isLast() {
      return last;
    }

    @Override
    public String toString() {
      return "Token [error=" + error + ", empty=" + empty + ", last=" + last + ", fieldName=" + fieldName + "]";
    }
  }
}
