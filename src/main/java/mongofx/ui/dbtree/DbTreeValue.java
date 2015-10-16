package mongofx.ui.dbtree;

import mongofx.service.MongoConnection;
import mongofx.service.MongoDatabase;

public class DbTreeValue {
  private MongoConnection dbConnect;
  private MongoDatabase mongoDatabase;
  private final String displayValue;
  private Integer count;
  private String collectionName;
  private final TreeValueType valueType;

  public static enum TreeValueType {
    COLLECTION,
    CATEGORY,
    INDEX,
    DATABASE,
    CONNECTION
  };

  public DbTreeValue(String displayValue) {
    this.displayValue = displayValue;
    valueType = TreeValueType.CATEGORY;
  }

  public DbTreeValue(MongoDatabase mongoDatabase, String displayValue, TreeValueType type) {
    super();
    this.mongoDatabase = mongoDatabase;
    this.displayValue = displayValue;
    valueType = type;
  }

  public DbTreeValue(MongoConnection dbConnect, String displayValue) {
    super();
    this.dbConnect = dbConnect;
    this.displayValue = displayValue;
    valueType = TreeValueType.CONNECTION;
  }

  public MongoDatabase getMongoDatabase() {
    return mongoDatabase;
  }

  public MongoConnection getDbConnect() {
    return dbConnect;
  }

  public String getDisplayValue() {
    return displayValue;
  }

  public TreeValueType getValueType() {
    return valueType;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public void setCollectionName(String collectionName) {
    this.collectionName = collectionName;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public Integer getCount() {
    return count;
  }

  @Override
  public String toString() {
    if (count != null) {
      return String.format("%s(%d)", displayValue, count);
    }
    return displayValue;
  }
}
