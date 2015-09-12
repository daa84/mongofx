package mongoui.service.js.api;

import java.util.HashMap;

public class DB extends HashMap<String, Object> {

  public DB(mongoui.service.MongoDatabase mongoDatabase) {
    super();
    mongoDatabase.listCollectins().stream().forEach(n -> put(n, new Collection(mongoDatabase, n)));
  }

  public Collection getCollection(String name) {
    return (Collection)get(name);
  }
}
