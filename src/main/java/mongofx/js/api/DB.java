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

import static mongofx.js.api.JsApiUtils.dbObjectFromMap;
import static mongofx.js.api.JsApiUtils.documentFromMap;
import static mongofx.js.api.JsApiUtils.singletonIter;

import java.util.HashMap;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import com.mongodb.BasicDBObject;

import mongofx.service.MongoDatabase;

@SuppressWarnings("serial")
public class DB extends HashMap<String, Object> {

  private final MongoDatabase mongoDatabase;

  public DB(mongofx.service.MongoDatabase mongoDatabase) {
    super();
    this.mongoDatabase = mongoDatabase;
    mongoDatabase.listCollections().stream().forEach(n -> put(n, new Collection(mongoDatabase, n)));
  }

  public Collection getCollection(String name) {
    return (Collection)get(name);
  }

  public ObjectListPresentation runCommand(Bindings cmd) {
    return singletonIter(mongoDatabase.getMongoDb().runCommand(documentFromMap(cmd)));
  }

  public SimpleTextPresentation getName() {
    return new SimpleTextPresentation(mongoDatabase.getMongoDb().getName());
  }

  public ObjectListPresentation serverStatus() {
    return serverStatus(new SimpleBindings());
  }

  public ObjectListPresentation serverStatus(Bindings options) {
    BasicDBObject command = dbObjectFromMap(options);
    command.put("serverStatus", 1);
    return singletonIter(mongoDatabase.getMongoDb().runCommand(command));
  }

  public ObjectListPresentation stats() {
    return stats(null);
  }

  public ObjectListPresentation stats(Integer scale) {
    BasicDBObject command = new BasicDBObject("dbStats", 1);
    if (scale != null) {
      command.put("scale", scale);
    }
    return singletonIter(mongoDatabase.getMongoDb().runCommand(command));
  }
}
