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

import java.util.List;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;

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
  public String getCollectionName() {
    return collectionName;
  }

  @Override
  public Optional<Integer> getSkip() {
    return Optional.empty();
  }

  @Override
  public Optional<Integer> getLimit() {
    return Optional.empty();
  }

  @Override
  public MongoCursor<Document> iterator(int skip, int limit) {
    try (MongoCursor<Document> iterator = mongoDatabase.getMongoDb().getCollection(collectionName).aggregate(pipeline).iterator()) {
      return new JsApiUtils.SimpleIteratorMongoCursor(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false).skip(skip).limit(limit).iterator());
    }
  }

}
