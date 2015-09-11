package mongoui.service.js.api;

import javax.script.Bindings;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;

public class Collection {
  private String name;
  private MongoDatabase database;

  public Collection(MongoDatabase database, String name) {
    this.database = database;
    this.name = name;
  }

  public FindResultIterable find(Bindings query) {
    return new FindResultIterable(database.getCollection(name).find(new BasicDBObject(query)));
  }

  public FindResultIterable find() {
    return new FindResultIterable(database.getCollection(name).find());
  }
}
