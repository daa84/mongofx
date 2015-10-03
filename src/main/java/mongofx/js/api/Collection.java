package mongofx.js.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.script.Bindings;

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
    if (options == null || options.isEmpty()) {
      return new SimpleTextPresentation(getCollection()
          .updateOne(JsApiUtils.dbObjectFromMap(filter), JsApiUtils.dbObjectFromMap(update)).getModifiedCount());
    }

    return new SimpleTextPresentation(
        getCollection().updateOne(JsApiUtils.dbObjectFromMap(filter), JsApiUtils.dbObjectFromMap(update),
            JsApiUtils.buildOptions(new UpdateOptions(), options)).getModifiedCount());
  }

  public ObjectListPresentationIterables aggregate(List<Bindings> pipeline) {
    return new AggregateResultIterable(mongoDatabase, name, JsApiUtils.dbObjectFromList(pipeline));
  }

  public SimpleTextPresentation createIndex(Bindings index) {
    return createIndex(index, null);
  }

  public SimpleTextPresentation createIndex(Bindings index, Bindings options) {
    if (options == null || options.isEmpty()) {
      return new SimpleTextPresentation(getCollection().createIndex(JsApiUtils.dbObjectFromMap(index)));
    }
    return new SimpleTextPresentation(getCollection().createIndex(JsApiUtils.dbObjectFromMap(index),
        JsApiUtils.buildOptions(new IndexOptions(), options)));
  }

  private MongoCollection<Document> getCollection() {
    return mongoDatabase.getMongoDb().getCollection(name);
  }
}
