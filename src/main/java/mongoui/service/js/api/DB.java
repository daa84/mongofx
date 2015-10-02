package mongoui.service.js.api;

import java.util.HashMap;

import javax.script.Bindings;

import mongoui.service.MongoDatabase;

@SuppressWarnings("serial")
public class DB extends HashMap<String, Object> {

  private MongoDatabase mongoDatabase;

  public DB(mongoui.service.MongoDatabase mongoDatabase) {
    super();
    this.mongoDatabase = mongoDatabase;
    mongoDatabase.listCollectins().stream().forEach(n -> put(n, new Collection(mongoDatabase, n)));
  }

  public Collection getCollection(String name) {
    return (Collection)get(name);
  }

  public ObjectListPresentationIterables runCommand(Bindings cmd) {
    return JsApiUtils.singletonIter(mongoDatabase.getMongoDb().runCommand(JsApiUtils.documentFromMap(cmd)));
  }
}
