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

import java.util.HashMap;

import javax.script.Bindings;

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
    return JsApiUtils.singletonIter(mongoDatabase.getMongoDb().runCommand(JsApiUtils.documentFromMap(cmd)));
  }

  public SimpleTextPresentation getName() {
    return new SimpleTextPresentation(mongoDatabase.getMongoDb().getName());
  }
}
