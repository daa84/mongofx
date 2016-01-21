// This file is part of MongoFX.
//
// MongoFX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
// MongoFX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with MongoFX.  If not, see <http://www.gnu.org/licenses/>.

//
// Copyright (c) Andrey Dubravin, 2015
//
package mongofx.js.api;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.bson.BsonDocument;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.ExplainVerbosity;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.FindOptions;
import com.mongodb.operation.OperationExecutor;

import mongofx.driver.FindIterable;
import mongofx.service.MongoDatabase;

public class FindResultIterable implements ObjectListPresentation, Iterable<Bindings> {
  private static final Logger log = LoggerFactory.getLogger(FindResultIterable.class);

  private final MongoDatabase mongoDatabase;
  private final BasicDBObject findQuery;
  private final BasicDBObject projection;
  private final String collectionName;
  private final FindOptions findOptions = new FindOptions();
  private Integer skip = null;
  private Integer limit = null;

  public FindResultIterable(MongoDatabase mongoDatabase, String collectionName, BasicDBObject findQuery, BasicDBObject projection) {
    this.mongoDatabase = mongoDatabase;
    this.collectionName = collectionName;
    this.findQuery = findQuery;
    this.projection = projection;
  }

  public FindResultIterable(MongoDatabase mongoDatabase, String collectionName) {
    this.mongoDatabase = mongoDatabase;
    this.collectionName = collectionName;
    findQuery = new BasicDBObject(); // find all
    projection = null;
  }

  @Override
  public Optional<Integer> getSkip() {
    return Optional.ofNullable(skip);
  }

  @Override
  public Optional<Integer> getLimit() {
    return Optional.ofNullable(limit);
  }

  public FindResultIterable skip(int skip) {
    this.skip = skip;
    return this;
  }

  public FindResultIterable limit(int limit) {
    this.limit = limit;
    return this;
  }

  @JsIgnore
  @Override
  public MongoCursor<Document> iterator(int skip, int limit) {
    MongoCollection<Document> collection = getCollection();

    findOptions.skip(skip);
    findOptions.limit(limit);
    if (projection != null) {
      findOptions.projection(projection);
    }
    return new FindIterable(new MongoNamespace(mongoDatabase.getName(), collectionName), collection.getCodecRegistry(), //
        collection.getReadPreference(), getExecutor(), findQuery, findOptions).iterator();
  }

  private OperationExecutor getExecutor() {
    try {
      // this is hack to get executor
      // hope will be removed when 3 series driver have explain function
      com.mongodb.client.MongoDatabase mongoDb = mongoDatabase.getMongoDb();
      Field f = mongoDb.getClass().getDeclaredField("executor");
      f.setAccessible(true);
      return (OperationExecutor) f.get(mongoDb);
    }
    catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      log.error("Exception:",e);
      return null;
    }
  }

  @JsIgnore
  @Override
  public String getCollectionName() {
    return collectionName;
  }

  private MongoCollection<Document> getCollection() {
    return mongoDatabase.getMongoDb().getCollection(collectionName);
  }

  public long count() {
    return getCollection().count(findQuery);
  }

  public ObjectListPresentation explain() {
    MongoCollection<Document> collection = getCollection();

    FindIterable findIterable = new FindIterable(new MongoNamespace(mongoDatabase.getName(), collectionName), collection.getCodecRegistry(), //
        collection.getReadPreference(), getExecutor(), findQuery, findOptions);

    BsonDocument res = findIterable.explainIterator(ExplainVerbosity.QUERY_PLANNER);
    return JsApiUtils.singletonIter(JsApiUtils.convertBsonToDocument(res));
  }

  @Override
  public void forEach(Consumer<? super Bindings> action) {
    Iterable.super.forEach(action);
  }

  @Override
  public Iterator<Bindings> iterator() {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(skip != null ? skip : 0, limit != null ? limit : 0), 0), false)
        .map(v -> (Bindings)new SimpleBindings(v)).iterator();
  }
}
