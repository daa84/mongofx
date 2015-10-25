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
package mongofx.service.settings;

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
