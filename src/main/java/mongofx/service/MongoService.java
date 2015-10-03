package mongofx.service;

import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import mongofx.settings.ConnectionSettings;

@Singleton
public class MongoService {

  private MongoConnection mongoConnection;

  public MongoConnection connect(ConnectionSettings connectionSettings) {
    closeConnection();

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
    mongoConnection = new MongoConnection(client);
    return mongoConnection;
  }

  private void closeConnection() {
    if (mongoConnection == null) {
      return;
    }

    mongoConnection.getClient().close();
  }

  public void stop() {
    closeConnection();
  }

}
