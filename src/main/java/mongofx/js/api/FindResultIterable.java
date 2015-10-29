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

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import mongofx.service.MongoDatabase;
import org.bson.Document;

import java.util.Iterator;
import java.util.function.Consumer;

public class FindResultIterable implements ObjectListPresentation {

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

  @JsIgnore
  @Override
  public Iterator<Document> iterator() {
    MongoCollection<Document> collection = getCollection();
    if (findQuery != null) {
      return collection.find(findQuery).iterator();
    }
    return collection.find().iterator();
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

  @Override
  public void forEach(Consumer<? super Document> action) {
    iterator().forEachRemaining(action);
  }
}
