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

  public static ObjectListPresentationIterables singletonIter(final Document doc) {
    final List<Document> list = Collections.singletonList(doc);
    return new ObjectListPresentationIterables() {

      @Override
      public Iterator<Document> iterator() {
        return list.iterator();
      }
    };
  }

  public static ObjectListPresentationIterables iter(Iterable<Document> iterable) {
    return new ObjectListPresentationIterables() {

      @Override
      public Iterator<Document> iterator() {
        return iterable.iterator();
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
