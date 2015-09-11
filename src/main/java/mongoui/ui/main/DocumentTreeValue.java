package mongoui.ui.main;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * @author daa
 *
 */
public class DocumentTreeValue {
  private Object value;
  private String key;

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
      ObjectId id = ((Document)value).getObjectId("_id");
      if (id != null) {
        return "ObjectId(" + id.toHexString() + ")";
      }
      else {
        return "id not found";
      }
    }
    return key;
  }

  public Object getValue() {
    return value;
  }
}
