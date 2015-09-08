package mongoui.service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoIterable;

public class MongoConnection {

  private MongoClient client;

  public MongoConnection(MongoClient client) {
    this.client = client;
  }

  public MongoIterable<String> listDbs() {
    return client.listDatabaseNames();
  }

}
