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
package mongofx.service;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mongodb.MongoClient;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;

import mongofx.js.api.JsApiUtils;
import mongofx.js.api.ObjectListPresentation;

/**
 * @author daa
 *
 */
public class MongoDatabase {
  private final com.mongodb.client.MongoDatabase mongoDb;
  private List<String> cachedCollectionNames;
  private MongoClient client;

  public MongoDatabase(MongoClient client, String name) {
    this.client = client;
    mongoDb = client.getDatabase(name);
  }

  public MongoDatabase getSiblingDB(String name) {
    return new MongoDatabase(client, name);
  }

  public String getName() {
    return mongoDb.getName();
  }

  public List<String> getCachedCollections() {
    if (cachedCollectionNames != null) {
      return cachedCollectionNames;
    }
    return listCollections();
  }

  /**
   * also reload cache
   */
  public List<String> listCollections() {
    cachedCollectionNames = StreamSupport.stream(mongoDb.listCollectionNames().spliterator(), false).collect(Collectors.toList());
    return cachedCollectionNames;
  }

  /**
   * also reload cache
   */
  public List<CollectionDetails> listCollectionDetails() {
    return listCollections().stream().map(this::getCollectionDetail).collect(Collectors.toList());
  }

  private CollectionDetails getCollectionDetail(String collectionName) {
    BasicDBObject command = new BasicDBObject("collStats", collectionName);
    Document stats = mongoDb.runCommand(command);
    return new CollectionDetails(collectionName,
            stats.getInteger("count"),
            stats.getInteger("storageSize"),
            stats.getInteger("totalIndexSize"),
            stats.containsKey("wiredTiger"));
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

  public ObjectListPresentation runCommand(Bson command) {
    return JsApiUtils.singletonIter(mongoDb.runCommand(command));
  }
}
