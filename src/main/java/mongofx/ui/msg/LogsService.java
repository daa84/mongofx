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
// Copyright (c) Andrey Dubravin, 2015
//
package mongofx.ui.msg;

import com.google.inject.Singleton;
import javafx.application.Platform;
import org.apache.logging.log4j.core.LogEvent;

import java.util.function.Consumer;

@Singleton
public class LogsService {

  private Consumer<LogEvent> eventsConsumer;

  public void register(Consumer<LogEvent> eventsConsumer) {
    if (this.eventsConsumer != null) {
      throw new IllegalStateException("Can't register code area two times");
    }
    this.eventsConsumer = eventsConsumer;
  }

  public void log(LogEvent event) {
    Platform.runLater(() -> eventsConsumer.accept(event));
  }
}
