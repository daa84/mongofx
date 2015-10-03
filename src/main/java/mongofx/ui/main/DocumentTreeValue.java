package mongofx.ui.main;

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

  public DocumentTreeValue(String key, Object value) {
    this.key = key;
    this.value = value;
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
