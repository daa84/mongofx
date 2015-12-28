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
package mongofx.ui.main;


import com.google.inject.Inject;
import mongofx.ui.msg.LogsService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.ReadOnlyStyledDocument;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ConsoleController {
  private CodeArea codeArea;

  @Inject
  private LogsService logsService;

  public void initialize(CodeArea codeArea) {
    this.codeArea = codeArea;
    logsService.register(this::accept);
  }

  private void accept(LogEvent logEvent) {
    Level level = logEvent.getLevel();

    List<String> style = Collections.EMPTY_LIST;
    if (level == Level.ERROR || level == Level.WARN) {
      style = Collections.singletonList("console-error");
    }

    ReadOnlyStyledDocument<Collection<String>> document =
        ReadOnlyStyledDocument.fromString(logEvent.getMessage().getFormattedMessage() + "\n", style);
    codeArea.insert(codeArea.getLength(), document);
  }
}
