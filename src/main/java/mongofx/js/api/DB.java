package mongofx.js.api;

import java.util.HashMap;

import javax.script.Bindings;

import mongofx.service.MongoDatabase;

@SuppressWarnings("serial")
public class DB extends HashMap<String, Object> {

  private final MongoDatabase mongoDatabase;

  public DB(mongofx.service.MongoDatabase mongoDatabase) {
    super();
    this.mongoDatabase = mongoDatabase;
    mongoDatabase.listCollectins().stream().forEach(n -> put(n, new Collection(mongoDatabase, n)));
  }

  public Collection getCollection(String name) {
    return (Collection)get(name);
  }

  public ObjectListPresentation runCommand(Bindings cmd) {
    return JsApiUtils.singletonIter(mongoDatabase.getMongoDb().runCommand(JsApiUtils.documentFromMap(cmd)));
  }

  public SimpleTextPresentation getName() {
    return new SimpleTextPresentation(mongoDatabase.getMongoDb().getName());
  }
}
