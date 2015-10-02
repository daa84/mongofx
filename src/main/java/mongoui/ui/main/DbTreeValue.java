package mongoui.ui.main;

import mongoui.service.MongoDatabase;

public class DbTreeValue {
  private MongoDatabase mongoDatabase;
  private final String displayValue;
  private boolean isCollectionValue = false;
  private boolean isCategory = false;

  public DbTreeValue(String displayValue) {
    super();
    this.displayValue = displayValue;
    isCategory = true;
  }

  public DbTreeValue(MongoDatabase mongoDatabase) {
    this.mongoDatabase = mongoDatabase;
    this.displayValue = mongoDatabase.toString();
  }

  public DbTreeValue(MongoDatabase mongoDatabase, String name) {
    this.mongoDatabase = mongoDatabase;
    displayValue = name;
    isCollectionValue = true;
  }

  public MongoDatabase getMongoDatabase() {
    return mongoDatabase;
  }

  public boolean isCollectionValue() {
    return isCollectionValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }

  public boolean isCategory() {
    return isCategory;
  }

  @Override
  public String toString() {
    return displayValue;
  }
}
