package mongofx.js.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mongofx.js.support.JsCallPathTokenizer.Token;

public class JsPathBuilder {
  public static Optional<List<String>> buildPath(String jsCode) {
    JsCallPathTokenizer tokenizer = new JsCallPathTokenizer(jsCode);

    List<String> path = new ArrayList<>(4);
    while (true) {
      Token token = tokenizer.next();
      if (token.isError()) {
        return Optional.empty();
      }

      boolean lastItem = path.isEmpty();
      if (lastItem || !token.isEmpty()) {
        path.add(0, token.getFieldName());
      }

      if ((!lastItem && token.isEmpty()) || token.isLast()) {
        break;
      }
    }

    return Optional.of(path);
  }
}
