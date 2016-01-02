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
package mongofx.ui.dbtree;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import mongofx.service.CollectionDetails;

public class DBCollectionTooltipController {
  ObjectProperty<CollectionDetails> details = new SimpleObjectProperty<>();

  public void initialize(CollectionDetails details) {
    setDetails(details);
  }

  public CollectionDetails getDetails() {
    return details.get();
  }

  public ObjectProperty<CollectionDetails> detailsProperty() {
    return details;
  }

  public void setDetails(CollectionDetails details) {
    this.details.set(details);
  }
}
