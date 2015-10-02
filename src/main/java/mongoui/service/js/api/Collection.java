package mongoui.service.js.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.script.Bindings;

import mongoui.service.MongoDatabase;

public class Collection {
  private final String name;
  private final mongoui.service.MongoDatabase mongoDatabase;

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
    mongoDatabase.getMongoDb().getCollection(name)
        .insertMany(items.stream().map(JsApiUtils::documentFromMap).collect(Collectors.toList()));
  }

  public void insert(Bindings item) {
    mongoDatabase.getMongoDb().getCollection(name).insertOne(JsApiUtils.documentFromMap(item));
  }

  public SimpleTextPresentation remove(Bindings item) {
    return new SimpleTextPresentation(mongoDatabase.getMongoDb().getCollection(name)
        .deleteMany(JsApiUtils.dbObjectFromMap(item)).getDeletedCount());
  }

  public SimpleTextPresentation update(Bindings filter, Bindings update) {
    return new SimpleTextPresentation(mongoDatabase.getMongoDb().getCollection(name)
        .updateOne(JsApiUtils.dbObjectFromMap(filter), JsApiUtils.dbObjectFromMap(update)).getModifiedCount());
  }

  public ObjectListPresentationIterables aggregate(List<Bindings> pipeline) {
    return new AggregateResultIterable(mongoDatabase, name, JsApiUtils.dbObjectFromList(pipeline));
  }
}
