package mongoui.service;

import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import mongoui.settings.ConnectionSettings;

@Singleton
public class MongoService {

  public MongoConnection connect(ConnectionSettings connectionSettings) {
    String uri = String.format("mongodb://%s", connectionSettings.getHost());
    MongoClient client = new MongoClient(new MongoClientURI(uri));
    return new MongoConnection(client);
  }

}
