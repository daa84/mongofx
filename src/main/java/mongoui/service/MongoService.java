package mongoui.service;

import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import mongoui.settings.ConnectionSettings;

@Singleton
public class MongoService {

  public MongoConnection connect(ConnectionSettings connectionSettings) {
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
    return new MongoConnection(client);
  }

}
