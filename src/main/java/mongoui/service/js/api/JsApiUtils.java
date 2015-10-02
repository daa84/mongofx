package mongoui.service.js.api;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.Bindings;

import org.bson.Document;

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
}
