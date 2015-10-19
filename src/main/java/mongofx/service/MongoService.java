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
