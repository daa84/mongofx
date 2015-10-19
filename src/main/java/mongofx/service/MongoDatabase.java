package mongofx.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import com.mongodb.BasicDBObject;

import mongofx.js.api.DB;
import mongofx.js.api.ObjectListPresentation;
import mongofx.js.api.TextPresentation;

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

  public Optional<Object> eval(String query) throws ScriptException {
    ScriptEngineManager engineManager = new ScriptEngineManager();
    ScriptEngine engine = engineManager.getEngineByName("nashorn");
    SimpleBindings bindings = new SimpleBindings();
    bindings.put("db", new DB(this));
    Object result = engine.eval(query, bindings);
    if (result instanceof ObjectListPresentation || result instanceof TextPresentation) {
      return Optional.of(result);
    }
    return Optional.empty();
  }
}
