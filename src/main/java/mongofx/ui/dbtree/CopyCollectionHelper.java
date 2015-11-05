// This file is part of MongoFX.
//
// MongoFX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
// Foobar is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

//
// Copyright (c) Andrey Dubravin, 2015
//
package mongofx.ui.dbtree;

import com.mongodb.BasicDBObject;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import mongofx.service.MongoDatabase;

import java.util.Arrays;
import java.util.Optional;

public class CopyCollectionHelper {
  private final String fromCollection;
  private final String toCollection;
  private final MongoDatabase fromDb;

  public CopyCollectionHelper(MongoDatabase fromDb, String fromCollection, String toCollection) {
    this.fromCollection = fromCollection;
    this.toCollection = toCollection;
    this.fromDb = fromDb;
  }

  public boolean validate() {
    return checkTargetExists();
  }

  private boolean checkTargetExists() {
    if (fromDb.listCollections().stream().anyMatch(v -> v.equalsIgnoreCase(toCollection))) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setHeaderText("Target collection '" + toCollection + "' already exists, overwrite?");
      Optional<ButtonType> buttonType = alert.showAndWait();
      if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
        return true;
      }
      return false;
    }

    return true;
  }

  public void copy() {
    if (validate()) {
      fromDb.getMongoDb().getCollection(toCollection).drop();
      fromDb.getMongoDb().getCollection(fromCollection).aggregate(Arrays.asList(new BasicDBObject("$out", toCollection)))
          .batchSize(10).first();
    }
  }
}
