package mongofx.js.api;

import java.util.Iterator;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

import mongofx.service.MongoDatabase;

public class FindResultIterable implements ObjectListPresentationIterables {

  private final MongoDatabase mongoDatabase;
  private final BasicDBObject findQuery;
  private final String collectionName;

  public FindResultIterable(MongoDatabase mongoDatabase, String collectionName, BasicDBObject findQuery) {
    this.mongoDatabase = mongoDatabase;
    this.collectionName = collectionName;
    this.findQuery = findQuery;
  }

  public FindResultIterable(MongoDatabase mongoDatabase, String collectionName) {
    this.mongoDatabase = mongoDatabase;
    this.collectionName = collectionName;
    findQuery = null; // find all
  }

  @Override
  public Iterator<Document> iterator() {
    MongoCollection<Document> collection = getCollection();
    if (findQuery != null) {
      return collection.find(findQuery).iterator();
    }
    return collection.find().iterator();
  }

  private MongoCollection<Document> getCollection() {
    return mongoDatabase.getMongoDb().getCollection(collectionName);
  }

  public TextPresentation count() {
    return new SimpleTextPresentation(getCollection().count(findQuery));
  }
}
