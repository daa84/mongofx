package mongoui.service.js.api;

import javax.script.Bindings;

import org.bson.Document;

import com.mongodb.BasicDBObject;

public class JsApiUtils {

  public static BasicDBObject dbObjectFromMap(Bindings from) {
    return new BasicDBObject(from);
  }

  public static Document documentFromMap(Bindings from) {
    return new Document(from);
  }
}
