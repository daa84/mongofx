package mongoui.service.js.api;

import javax.script.Bindings;

import com.mongodb.BasicDBObject;

import mongoui.service.MongoDatabase;

public class Collection {
  private final String name;
  private final mongoui.service.MongoDatabase mongoDatabase;

  public Collection(MongoDatabase mongoDatabase, String name) {
    this.mongoDatabase = mongoDatabase;
    this.name = name;
  }

  public FindResultIterable find(Bindings query) {
    return new FindResultIterable(mongoDatabase.getMongoDb().getCollection(name).find(new BasicDBObject(query)));
  }

  public FindResultIterable find() {
    return new FindResultIterable(mongoDatabase.getMongoDb().getCollection(name).find());
  }
}
