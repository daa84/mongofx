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

import java.lang.reflect.Field;
import java.util.function.Consumer;

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

public class FindResultIterable implements ObjectListPresentation {
  private static final Logger log = LoggerFactory.getLogger(FindResultIterable.class);

  private final MongoDatabase mongoDatabase;
  private final BasicDBObject findQuery;
  private final String collectionName;
  private final FindOptions findOptions = new FindOptions();

  public FindResultIterable(MongoDatabase mongoDatabase, String collectionName, BasicDBObject findQuery) {
    this.mongoDatabase = mongoDatabase;
    this.collectionName = collectionName;
    this.findQuery = findQuery;
  }

  public FindResultIterable(MongoDatabase mongoDatabase, String collectionName) {
    this.mongoDatabase = mongoDatabase;
    this.collectionName = collectionName;
    findQuery = new BasicDBObject(); // find all
  }

  @JsIgnore
  @Override
  public MongoCursor<Document> iterator() {
    MongoCollection<Document> collection = getCollection();

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
      OperationExecutor executor = (OperationExecutor) f.get(mongoDb);
      return executor;
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

  public TextPresentation count() {
    return new SimpleTextPresentation(getCollection().count(findQuery));
  }

  public ObjectListPresentation explain() {
    MongoCollection<Document> collection = getCollection();

    FindIterable findIterable = new FindIterable(new MongoNamespace(mongoDatabase.getName(), collectionName), collection.getCodecRegistry(), //
        collection.getReadPreference(), getExecutor(), findQuery, findOptions);

    BsonDocument res = findIterable.explainIterator(ExplainVerbosity.QUERY_PLANNER);
    return JsApiUtils.singletonIter(JsApiUtils.convertBsonToDocument(res));
  }

  public void forEach(Consumer<? super Document> action) {
    iterator().forEachRemaining(action);
  }
}
