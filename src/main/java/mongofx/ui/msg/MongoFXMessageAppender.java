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

import mongofx.Main;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;

@Plugin(name = "MongoFXMessage", category = "Core", elementType = "appender")
public class MongoFXMessageAppender extends AbstractAppender {

  private LogsService logsService;

  protected MongoFXMessageAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
    super(name, filter, layout, ignoreExceptions);
  }

  @PluginFactory
  public static MongoFXMessageAppender createAppender(@PluginAttribute("name") String name,
                                            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
                                            @PluginElement("Layout") Layout layout,
                                            @PluginElement("Filters") Filter filter) {

    if (name == null) {
      LOGGER.error("No name provided for StubAppender");
      return null;
    }

    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }
    return new MongoFXMessageAppender(name, filter, layout, ignoreExceptions);
  }

  private LogsService getLogsService() {
    if (logsService == null) {
      logsService = Main.getInstance(LogsService.class);
    }
    return logsService;
  }

  @Override
  public void append(LogEvent event) {
    LogsService logsService = getLogsService();
    if (logsService != null) {
      logsService.log(event);
    }
  }
}
