package mongofx.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mongodb.BasicDBObject;

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

  public void createCollection(String name) {
    mongoDb.createCollection(name);
  }

  public void dropCollection(String name) {
    mongoDb.getCollection(name).drop();
  }

  public void drop() {
    mongoDb.drop();
  }

  public void removeAllDocuments(String collectionName) {
    mongoDb.getCollection(collectionName).deleteMany(new BasicDBObject());
  }

  public void dropIndex(String collectionName, String indexName) {
    mongoDb.getCollection(collectionName).dropIndex(indexName);
  }
}
