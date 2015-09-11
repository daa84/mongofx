package mongoui.service.js.api;

import com.mongodb.client.MongoDatabase;

import mongoui.service.MongoConnection;

public class DB {

  private MongoDatabase database;

  public DB(MongoConnection connection, String dbName) {
    super();
    database = connection.getClient().getDatabase(dbName);
  }

  public Collection getCollection(String name) {
    return new Collection(database, name);
  }
}
