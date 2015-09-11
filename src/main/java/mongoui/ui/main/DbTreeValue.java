package mongoui.ui.main;

import mongoui.service.MongoDatabase;

public class DbTreeValue {
  private final MongoDatabase mongoDatabase;
  private final String displayValue;
  private boolean isCollectionValue = false;

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

  @Override
  public String toString() {
    return displayValue;
  }
}
