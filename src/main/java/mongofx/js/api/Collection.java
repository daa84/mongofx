// This file is part of MongoFX.
//
// MongoFX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
// Foobar is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

//
// Copyright (c) Andrey Dubravin, 2015
//
package mongofx.js.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
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

  public ObjectListPresentation reIndex() {
    return JsApiUtils.singletonIter(mongoDatabase.getMongoDb().runCommand(new BasicDBObject("reIndex", name)));
  }

  public void dropIndex(String indexName) {
    getCollection().dropIndex(indexName);
  }

  public void dropIndex(Bindings index) {
    getCollection().dropIndex(JsApiUtils.dbObjectFromMap(index));
  }

  public void dropIndexes() {
    getCollection().dropIndexes();
  }

  private MongoCollection<Document> getCollection() {
    return mongoDatabase.getMongoDb().getCollection(name);
  }

  @JsField("Wraps count to return a count of the number of documents in a collection or matching a query.")
  public SimpleTextPresentation count() {
    return new SimpleTextPresentation(getCollection().count());
  }

  @JsField("Wraps count to return a count of the number of documents in a collection or matching a query.")
  public SimpleTextPresentation count(Bindings find) {
    return new SimpleTextPresentation(getCollection().count(JsApiUtils.dbObjectFromMap(find)));
  }

  public ObjectListPresentation distinct(String key) {
    return distinct(key, null);
  }

  public ObjectListPresentation distinct(String key, Bindings query) {
    BasicDBObjectBuilder command = new BasicDBObjectBuilder() //
        .add("distinct", name) //
        .add("key", key); //
    if (query != null) {
      command.add("query", query);
    }

    return JsApiUtils.singletonIter(mongoDatabase.getMongoDb().runCommand((Bson)command.get()));
  }

  @JsField("Updates an existing document or inserts a new document, depending on its document parameter.")
  public SimpleTextPresentation save(Bindings document) {
    Object id = document.get("_id");

    if (id != null) {
      Document toUpdate = new Document(document);
      return new SimpleTextPresentation(
          getCollection().replaceOne(new BasicDBObject("_id", id), toUpdate, new UpdateOptions().upsert(true)).getModifiedCount());
    }

    getCollection().insertOne(JsApiUtils.documentFromMap(document));
    return new SimpleTextPresentation(1);
  }

  public ObjectListPresentation mapReduce(String map, String reduce, Bindings options) {
    BasicDBObjectBuilder command = new BasicDBObjectBuilder();
    command.add("mapReduce", name);
    command.add("map", map);
    command.add("reduce", reduce);

    putObject("query", options, command);
    putObject("out", options, command);
    putObject("scope", options, command);
    putSimpleField("field", options, command);
    putSimpleField("jsMode", options, command);
    putSimpleField("finilize", options, command);
    putSimpleField("verbose", options, command);

    return JsApiUtils.singletonIter(mongoDatabase.getMongoDb().runCommand((Bson)command.get()));
  }

  private void putSimpleField(String field, Bindings options, BasicDBObjectBuilder command) {
    Object val = options.get(field);
    if (val != null) {
      command.add(field, val);
    }
  }

  private void putObject(String field, Bindings options, BasicDBObjectBuilder command) {
    Bindings obj = (Bindings)options.get(field);
    if (obj != null) {
      command.add(field, JsApiUtils.dbObjectFromMap(obj));
    }
  }
}
