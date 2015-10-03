package mongofx.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import com.mongodb.MongoClient;

import mongofx.js.api.DB;
import mongofx.js.api.ObjectListPresentationIterables;
import mongofx.js.api.TextPresentation;

public class MongoConnection {

  private final MongoClient client;

  public MongoConnection(MongoClient client) {
    this.client = client;
  }

  public List<MongoDatabase> listDbs() {
    return StreamSupport.stream(client.listDatabaseNames().spliterator(), false).map(n -> createMongoDB(n)).collect(Collectors.toList());
  }

  public MongoDatabase createMongoDB(String name) {
    return new MongoDatabase(client.getDatabase(name));
  }

  public MongoClient getClient() {
    return client;
  }

  public Optional<Object> eval(MongoDatabase mongoDatabase, String query) throws ScriptException {
    ScriptEngineManager engineManager = new ScriptEngineManager();
    ScriptEngine engine = engineManager.getEngineByName("nashorn");
    SimpleBindings bindings = new SimpleBindings();
    bindings.put("db", new DB(mongoDatabase));
    Object result = engine.eval(query, bindings);
    if (result instanceof ObjectListPresentationIterables || result instanceof TextPresentation) {
      return Optional.of(result);
    }
    return Optional.empty();
  }
}
