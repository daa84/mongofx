package mongoui.service.js.api;

import mongoui.service.MongoConnection;
import mongoui.service.MongoDatabase;

public class DB {

  private final MongoDatabase mongoDatabase;

  public DB(MongoConnection connection, mongoui.service.MongoDatabase mongoDatabase) {
    super();
    this.mongoDatabase = mongoDatabase;
  }

  public Collection getCollection(String name) {
    return new Collection(mongoDatabase, name);
  }
}
