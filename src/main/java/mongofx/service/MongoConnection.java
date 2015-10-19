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
    return new MongoDatabase(client.getDatabase(name));
  }

  public MongoClient getClient() {
    return client;
  }
}
