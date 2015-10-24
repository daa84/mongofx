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
package mongofx.service;

import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import mongofx.settings.ConnectionSettings;

@Singleton
public class MongoService {

  public MongoDbConnection connect(ConnectionSettings connectionSettings) {
    StringBuilder authString = new StringBuilder();

    String user = connectionSettings.getUser();
    if (user != null && !user.isEmpty()) {
      authString.append(user);
      String password = connectionSettings.getPassword();
      if (password != null && !password.isEmpty()) {
        authString.append(":").append(password);
      }
      authString.append("@");
    }
    String uri = String.format("mongodb://%s%s", authString, connectionSettings.getHost());
    MongoClient client = new MongoClient(new MongoClientURI(uri));
    MongoConnection mongoConnection = new MongoConnection(client);
    return new MongoDbConnection(mongoConnection, connectionSettings);
  }

  public void stop() {
  }

  public static class MongoDbConnection {
    private final ConnectionSettings connectionSettings;
    private final MongoConnection mongoConnection;

    public MongoDbConnection(MongoConnection mongoConnection, ConnectionSettings connectionSettings) {
      this.mongoConnection = mongoConnection;
      this.connectionSettings = connectionSettings;
    }

    public ConnectionSettings getConnectionSettings() {
      return connectionSettings;
    }

    public MongoConnection getMongoConnection() {
      return mongoConnection;
    }
  }
}
