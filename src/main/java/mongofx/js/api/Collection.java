package mongofx.js.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;

import mongofx.service.MongoDatabase;

public class Collection {
  private final String name;
  private final mongofx.service.MongoDatabase mongoDatabase;

  public Collection(MongoDatabase mongoDatabase, String name) {
    this.mongoDatabase = mongoDatabase;
    this.name = name;
  }

  public FindResultIterable find(Bindings query) {
    return new FindResultIterable(mongoDatabase, name, JsApiUtils.dbObjectFromMap(query));
  }

  public FindResultIterable find() {
    return new FindResultIterable(mongoDatabase, name);
  }

  public void insert(List<Bindings> items) {
    getCollection().insertMany(items.stream().map(JsApiUtils::documentFromMap).collect(Collectors.toList()));
  }

  public void insert(Bindings item) {
    getCollection().insertOne(JsApiUtils.documentFromMap(item));
  }

  public SimpleTextPresentation remove(Bindings item) {
    return new SimpleTextPresentation(getCollection().deleteMany(JsApiUtils.dbObjectFromMap(item)).getDeletedCount());
  }

  public SimpleTextPresentation update(Bindings filter, Bindings update) {
    return update(filter, update, null);
  }

  public SimpleTextPresentation update(Bindings filter, Bindings update, Bindings options) {
    Boolean multi = false;
    if (options != null) {
      options = new SimpleBindings(options);
      multi = (Boolean)options.remove("multi");
      if (multi == null) {
        multi = false;
      }
    }

    if (multi) {
      return new SimpleTextPresentation(
          getCollection().updateMany(JsApiUtils.dbObjectFromMap(filter), JsApiUtils.dbObjectFromMap(update),
              JsApiUtils.buildOptions(new UpdateOptions(), options)).getModifiedCount());
    }
    return new SimpleTextPresentation(
        getCollection().updateOne(JsApiUtils.dbObjectFromMap(filter), JsApiUtils.dbObjectFromMap(update),
            JsApiUtils.buildOptions(new UpdateOptions(), options)).getModifiedCount());
  }

  @JsField("Provides access to the aggregation pipeline.")
  public ObjectListPresentation aggregate(List<Bindings> pipeline) {
    return new AggregateResultIterable(mongoDatabase, name, JsApiUtils.dbObjectFromList(pipeline));
  }

  public SimpleTextPresentation createIndex(Bindings index) {
    return createIndex(index, null);
  }

  public ObjectListPresentation getIndexes() {
    return JsApiUtils.iter(getCollection().listIndexes());
  }

  public SimpleTextPresentation createIndex(Bindings index, Bindings options) {
    return new SimpleTextPresentation(getCollection().createIndex(JsApiUtils.dbObjectFromMap(index),
        JsApiUtils.buildOptions(new IndexOptions(), options)));
  }

  private MongoCollection<Document> getCollection() {
    return mongoDatabase.getMongoDb().getCollection(name);
  }

  @JsField("Wraps count to return a count of the number of documents in a collection or matching a query.")
  public void count() {
    getCollection().count();
  }

  @JsField("Wraps count to return a count of the number of documents in a collection or matching a query.")
  public SimpleTextPresentation count(Bindings find) {
    return new SimpleTextPresentation(getCollection().count(JsApiUtils.dbObjectFromMap(find)));
  }
}
