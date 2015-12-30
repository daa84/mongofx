package mongofx.service;

import com.google.inject.Singleton;
import mongofx.js.api.DB;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Function;

@Singleton
public class ScriptService {
  private static final Logger log = LoggerFactory.getLogger(ScriptService.class);

  private ScriptEngineManager engineManager = new ScriptEngineManager();
  private ScriptEngine engine = engineManager.getEngineByName("nashorn");

  private final Function<String, ObjectId> toObjectId = id -> new ObjectId(id);

  public Optional<Object> eval(MongoDatabase db, String query) throws ScriptException {
    ScriptContext scriptContext = new SimpleScriptContext();
    Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
    bindings.put("db", new DB(db));
    bindings.put("ObjectId", toObjectId);

    StringWriter writer = new StringWriter();
    scriptContext.setWriter(writer);

    Object result = engine.eval(query, scriptContext);
    StringBuffer out = writer.getBuffer();

    if (result != null) {
      if (out.length() > 0) {
        log.info("Script output:\n{}", out.toString().trim());
      }
      return Optional.of(result);
    }

    if (out.length() > 0) {
      return Optional.of(out.toString().trim());
    }
    return Optional.empty();
  }
}
