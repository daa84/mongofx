package mongoui.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author daa
 *
 */
public class MongoDatabase {
  private final com.mongodb.client.MongoDatabase mongoDb;

  public MongoDatabase(com.mongodb.client.MongoDatabase mongoDb) {
    this.mongoDb = mongoDb;
  }

  public String getName() {
    return mongoDb.getName();
  }

  public List<String> listCollectins() {
    return StreamSupport.stream(mongoDb.listCollectionNames().spliterator(), false).collect(Collectors.toList());
  }

  public com.mongodb.client.MongoDatabase getMongoDb() {
    return mongoDb;
  }

  @Override
  public String toString() {
    return getName();
  }
}
