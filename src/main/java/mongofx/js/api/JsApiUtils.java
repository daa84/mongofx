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
package mongofx.js.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.script.Bindings;

import org.bson.Document;

import com.google.common.base.Preconditions;
import com.mongodb.BasicDBObject;

public class JsApiUtils {

  public static BasicDBObject dbObjectFromMap(Bindings from) {
    return new BasicDBObject(from);
  }

  public static List<BasicDBObject> dbObjectFromList(List<Bindings> from) {
    return from.stream().map(JsApiUtils::dbObjectFromMap).collect(Collectors.toList());
  }

  public static Document documentFromMap(Bindings from) {
    return new Document(from);
  }

  public static ObjectListPresentation singletonIter(final Document doc) {
    final List<Document> list = Collections.singletonList(doc);
    return new ObjectListPresentation() {

      @Override
      public Iterator<Document> iterator() {
        return list.iterator();
      }

      @Override
      public String getCollectionName() {
        return null;
      }
    };
  }

  public static ObjectListPresentation iter(Iterable<Document> iterable) {
    return new ObjectListPresentation() {

      @Override
      public Iterator<Document> iterator() {
        return iterable.iterator();
      }

      @Override
      public String getCollectionName() {
        return null;
      }
    };
  }

  public static <T> T buildOptions(T buildedOptions, Bindings options) {
    Preconditions.checkNotNull(buildedOptions);
    if (options == null || options.isEmpty()) {
      return buildedOptions;
    }

    Map<String, Method> methods =
        Arrays.stream(buildedOptions.getClass().getMethods()).filter(m -> !Object.class.equals(m.getDeclaringClass()))
        .collect(Collectors.toMap(m -> m.getName(), Function.identity()));

    for (Entry<String, Object> entry : options.entrySet()) {
      String key = entry.getKey();
      Method keyMethod = methods.get(key);
      if (keyMethod == null) {
        throw new IllegalArgumentException("Wrong options argument " + key);
      }
      Object value = entry.getValue();
      if (value instanceof Bindings) {
        value = dbObjectFromMap((Bindings)value);
      }

      try {
        keyMethod.invoke(buildedOptions, value);
      }
      catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
    return buildedOptions;
  }
}
