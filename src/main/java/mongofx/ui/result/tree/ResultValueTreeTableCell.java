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
package mongofx.ui.result.tree;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import mongofx.ui.main.DocumentUtils;

public class ResultValueTreeTableCell extends TreeTableCell<DocumentTreeValue, Object> {
  private static final Pattern NEW_LINE = Pattern.compile("[\n\r]");

  @Override
  protected void updateItem(Object item, boolean empty) {
    if (item == getItem()) {
      return;
    }

    super.updateItem(item, empty);

    if (item == null) {
      super.setText(null);
      super.setGraphic(null);
      super.setTooltip(null);
    } else if (item instanceof Node) {
      super.setText(null);
      super.setGraphic((Node) item);
      super.setTooltip(null);
    } else {
      setupDisplayValue(item);
    }
  }

  private void setupDisplayValue(Object value) {
    if (value instanceof Document) {
    	final Document document = (Document) value;
    	
      super.setGraphic(null);
			super.setText(String.format("{%d fields}", document.size()));
      super.setTooltip(new Tooltip(DocumentUtils.formatJson(document)));
    }
    else if (value instanceof List) {
      super.setGraphic(null);
      super.setText(String.format("[%d]", ((List<?>) value).size()));
      super.setTooltip(null);
    }
    else {
      setupStringDisplayValue(value);
    }
  }

  private void setupStringDisplayValue(Object value) {
    String displayValue = String.valueOf(value);

    super.setText(limitTo(displayValue, 200));
    super.setTooltip(new Tooltip(displayValue));
    super.setGraphic(null);
  }

  private String limitTo(String displayValue, int limit) {
    if (displayValue == null || displayValue.isEmpty()) {
      return displayValue;
    }
    Matcher newLineMatcher = NEW_LINE.matcher(displayValue);
    if (newLineMatcher.find()) {
      displayValue = newLineMatcher.replaceAll("");
    }
    if (displayValue.length() > limit) {
      return displayValue.substring(0, limit);
    }
    return displayValue;
  }
}
