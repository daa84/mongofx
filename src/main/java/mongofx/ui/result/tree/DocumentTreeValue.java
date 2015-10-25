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
package mongofx.ui.result.tree;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * @author daa
 *
 */
public class DocumentTreeValue {
  private final Object value;
  private final String key;
  private final String collectionName;

  public DocumentTreeValue(String key, Object value) {
    this.key = key;
    this.value = value;
    this.collectionName = null;
  }

  public DocumentTreeValue(String key, Object value, String collectionName) {
    this.key = key;
    this.value = value;
    this.collectionName = collectionName;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public Document getDocument() {
    return (Document)value;
  }

  @Override
  public String toString() {
    if (value instanceof Document) {
      return "";
    }
    if (value instanceof List) {
      return "[]";
    }
    return String.valueOf(value);
  }

  public String getDisplayValue() {
    return toString();
  }

  public String getKey() {
    if (key == null && value instanceof Document) {
      Object id = ((Document)value).get("_id");
      if (id != null) {
        if (id instanceof ObjectId) {
          return "ObjectId(" + ((ObjectId)id).toHexString() + ")";
        }
        return id.toString();
      }
      return "id not found";
    }
    return key;
  }

  public Object getValue() {
    return value;
  }
}
