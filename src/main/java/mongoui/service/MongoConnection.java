package mongoui.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import com.mongodb.MongoClient;

import mongoui.service.js.api.DB;
import mongoui.service.js.api.ObjectListPresentationIterables;

public class MongoConnection {

  private final MongoClient client;

  public MongoConnection(MongoClient client) {
    this.client = client;
  }

  public List<MongoDatabase> listDbs() {
    return StreamSupport.stream(client.listDatabaseNames().spliterator(), false).map(n -> client.getDatabase(n)).map(d -> new MongoDatabase(d)).collect(Collectors.toList());
  }

  public MongoClient getClient() {
    return client;
  }

  public Optional<ObjectListPresentationIterables> eval(MongoDatabase mongoDatabase, String query) throws ScriptException {
    ScriptEngineManager engineManager = new ScriptEngineManager();
    ScriptEngine engine = engineManager.getEngineByName("nashorn");
    SimpleBindings bindings = new SimpleBindings();
    bindings.put("db", new DB(this, mongoDatabase));
    Object result = engine.eval(query, bindings);
    if (result instanceof ObjectListPresentationIterables) {
      return Optional.of((ObjectListPresentationIterables)result);
    }
    return Optional.empty();
  }
}
