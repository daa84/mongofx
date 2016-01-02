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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mongodb.MongoClient;

public class MongoConnection {

  private final MongoClient client;

  public MongoConnection(MongoClient client) {
    this.client = client;
  }

  public List<MongoDatabase> listDbs() {
    return StreamSupport.stream(client.listDatabaseNames().spliterator(), false).map(n -> createMongoDB(n))
        .collect(Collectors.toList());
  }

  public MongoDatabase createMongoDB(String name) {
    return new MongoDatabase(client, name);
  }

  public MongoClient getClient() {
    return client;
  }

  public void close() {
    client.close();
  }
}
