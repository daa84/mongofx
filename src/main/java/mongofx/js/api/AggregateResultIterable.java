package mongofx.js.api;

import java.util.Iterator;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;

import mongofx.service.MongoDatabase;

/**
 * @author daa
 *
 */
public class AggregateResultIterable implements ObjectListPresentation {
  private final MongoDatabase mongoDatabase;
  private final List<BasicDBObject> pipeline;
  private final String collectionName;

  public AggregateResultIterable(MongoDatabase mongoDatabase, String collectionName, List<BasicDBObject> pipeline) {
    super();
    this.mongoDatabase = mongoDatabase;
    this.collectionName = collectionName;
    this.pipeline = pipeline;
  }

  @Override
  public Iterator<Document> iterator() {
    return mongoDatabase.getMongoDb().getCollection(collectionName).aggregate(pipeline).iterator();
  }

}
