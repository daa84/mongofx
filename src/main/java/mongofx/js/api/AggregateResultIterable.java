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

import java.util.Iterator;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;

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
  public Iterator<Document> iterator() {
    return mongoDatabase.getMongoDb().getCollection(collectionName).aggregate(pipeline).iterator();
  }

}
