package mongofx.settings;

import javafx.beans.property.SimpleStringProperty;

public class ConnectionSettings {
  private SimpleStringProperty host = new SimpleStringProperty();
  private SimpleStringProperty user = new SimpleStringProperty();
  private SimpleStringProperty password = new SimpleStringProperty();

  public ConnectionSettings() {
    setHost("localhost");
  }

  public final SimpleStringProperty hostProperty() {
    return this.host;
  }

  public final java.lang.String getHost() {
    return this.hostProperty().get();
  }

  public final void setHost(final java.lang.String host) {
    this.hostProperty().set(host);
  }

  public final SimpleStringProperty userProperty() {
    return this.user;
  }

  public final java.lang.String getUser() {
    return this.userProperty().get();
  }

  public final void setUser(final java.lang.String user) {
    this.userProperty().set(user);
  }

  public final SimpleStringProperty passwordProperty() {
    return this.password;
  }

  public final java.lang.String getPassword() {
    return this.passwordProperty().get();
  }

  public final void setPassword(final java.lang.String password) {
    this.passwordProperty().set(password);
  }
}
