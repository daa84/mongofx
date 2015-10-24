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
