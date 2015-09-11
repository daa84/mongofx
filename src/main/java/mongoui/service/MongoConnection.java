package mongoui.service;

import java.util.Optional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoIterable;

import mongoui.service.js.api.DB;
import mongoui.service.js.api.ObjectListPresentationIterables;

public class MongoConnection {

  private MongoClient client;

  public MongoConnection(MongoClient client) {
    this.client = client;
  }

  public MongoIterable<String> listDbs() {
    return client.listDatabaseNames();
  }

  public MongoClient getClient() {
    return client;
  }

  public Optional<ObjectListPresentationIterables> eval(String dbName, String query) throws ScriptException {
    ScriptEngineManager engineManager = new ScriptEngineManager();
    ScriptEngine engine = engineManager.getEngineByName("nashorn");
    SimpleBindings bindings = new SimpleBindings();
    bindings.put("db", new DB(this, dbName));
    Object result = engine.eval(query, bindings);
    if (result instanceof ObjectListPresentationIterables) {
      return Optional.of((ObjectListPresentationIterables)result);
    }
    return Optional.empty();
  }
}
