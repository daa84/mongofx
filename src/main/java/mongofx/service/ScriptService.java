package mongofx.service;

import com.google.inject.Singleton;
import mongofx.js.api.DB;
import org.bson.types.ObjectId;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.Optional;
import java.util.function.Function;

@Singleton
public class ScriptService {

  private ScriptEngineManager engineManager = new ScriptEngineManager();
  private ScriptEngine engine = engineManager.getEngineByName("nashorn");

  private final Function<String, ObjectId> toObjectId = id -> new ObjectId(id);

  public Optional<Object> eval(MongoDatabase db, String query) throws ScriptException {
    SimpleBindings bindings = new SimpleBindings();
    bindings.put("db", new DB(db));
    bindings.put("ObjectId", toObjectId);
    Object result = engine.eval(query, bindings);
    if (result != null) {
      return Optional.of(result);
    }
    return Optional.empty();
  }
}
