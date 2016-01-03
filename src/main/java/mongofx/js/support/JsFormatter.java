// This file is part of MongoFX.
//
// MongoFX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
// MongoFX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with MongoFX.  If not, see <http://www.gnu.org/licenses/>.

//
// Copyright (c) Andrey Dubravin, 2016
//
package mongofx.js.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStreamReader;

public class JsFormatter {

  private static final Logger log = LoggerFactory.getLogger(JsFormatter.class);

  private Invocable invocable;

  private void initialize() {
    if (invocable != null) {
      return;
    }

    try {
      log.debug("Initialize beautify library");
      ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
      engine.eval(new InputStreamReader(getClass().getResourceAsStream("/javascript/beautify.js")));
      invocable = (Invocable) engine;
    } catch (ScriptException e) {
      log.error("Can't load javascript beautify.js", e);
    }
  }

  public String beautify(String in) {
    if (in == null || in.isEmpty()) {
      return in;
    }

    initialize();

    try {
      log.debug("Format code");
      return (String) invocable.invokeFunction("js_beautify", in);
    } catch (ScriptException | NoSuchMethodException e) {
      log.error("Can't execute beautify.js", e);
    }
    return in;
  }
}
