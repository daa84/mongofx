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
// Copyright (c) Andrey Dubravin, 2015
//
package mongofx.js.api;

import static mongofx.js.api.JsApiUtils.basicDbListFromList;
import static mongofx.js.api.JsApiUtils.dbObjectFromMap;
import static mongofx.js.api.JsApiUtils.documentFromMap;

import java.util.HashMap;
import java.util.List;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.bson.BSONObject;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;

import mongofx.service.MongoDatabase;

@SuppressWarnings("serial")
public class DB extends HashMap<String, Object> {

  private final MongoDatabase mongoDatabase;

  public DB(mongofx.service.MongoDatabase mongoDatabase) {
    super();
    this.mongoDatabase = mongoDatabase;
    mongoDatabase.getCachedCollections().stream().forEach(n -> put(n, new Collection(mongoDatabase, n)));
  }

  public Collection getCollection(String name) {
    return (Collection)get(name);
  }

  public ObjectListPresentation runCommand(Bindings cmd) {
    return mongoDatabase.runCommand(documentFromMap(cmd));
  }

  public String getName() {
    return mongoDatabase.getMongoDb().getName();
  }

  public ObjectListPresentation serverStatus() {
    return serverStatus(new SimpleBindings());
  }

  public ObjectListPresentation serverStatus(Bindings options) {
    BasicDBObject command = dbObjectFromMap(options);
    command.put("serverStatus", 1);
    return mongoDatabase.runCommand(command);
  }

  public ObjectListPresentation stats() {
    return stats(null);
  }

  public ObjectListPresentation stats(Integer scale) {
    BasicDBObject command = new BasicDBObject("dbStats", 1);
    if (scale != null) {
      command.put("scale", scale);
    }
    return mongoDatabase.runCommand(command);
  }

  public ObjectListPresentation serverBuildInfo() {
    return mongoDatabase.runCommand(new BasicDBObject("buildInfo", 1));
  }

  public String version() {
    return mongoDatabase.getMongoDb().runCommand(new BasicDBObject("buildInfo", 1)).getString("version");
  }

  @JsField("Changes an existing user’s password")
  public ObjectListPresentation changeUserPassword(String userName, String newPassword) {
    SimpleBindings simpleBindings = new SimpleBindings();
    simpleBindings.put("pwd", newPassword);
    return updateUser(userName, simpleBindings);
  }

  @JsField("Removes a role from a user")
  public ObjectListPresentation revokeRolesFromUser(String user, List<Bindings> roles) {
    return revokeRolesFromUser(user, roles, null);
  }

  @JsField("Removes a role from a user")
  public ObjectListPresentation revokeRolesFromUser(String user, List<Bindings> roles, Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("revokeRolesFromUser", user);
    command.put("roles", basicDbListFromList(roles));

    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsField("Grants a role and its privileges to a user")
  public ObjectListPresentation grantRolesToUser(String user, List<Bindings> roles) {
    return grantRolesToUser(user, roles, null);
  }

  @JsField("Grants a role and its privileges to a user")
  public ObjectListPresentation grantRolesToUser(String user, List<Bindings> roles, Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("grantRolesToUser", user);
    command.put("roles", basicDbListFromList(roles));

    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsField("Deletes all users associated with a database")
  public ObjectListPresentation dropAllUsers() {
    return dropAllUsers(null);
  }

  @JsField("Deletes all users associated with a database")
  public ObjectListPresentation dropAllUsers(Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("dropAllUsersFromDatabase", 1);
    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsField("Removes a single user")
  public ObjectListPresentation dropUser(String userName) {
    return dropUser(userName, null);
  }

  @JsField("Removes a single user")
  public ObjectListPresentation dropUser(String userName, Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("dropUser", userName);
    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsField("Returns information about all users associated with a database")
  public ObjectListPresentation getUsers() {
    return mongoDatabase.runCommand(new BasicDBObject("usersInfo", 1));
  }

  @JsField("Returns information about the specified user")
  public ObjectListPresentation getUser(String userName) {
    BasicDBObjectBuilder builder = new BasicDBObjectBuilder();
    builder.push("usersInfo").add("user", userName).add("db", mongoDatabase.getName()).pop();
    return mongoDatabase.runCommand((Bson)builder.get());
  }

  @JsField("Changes an existing user’s password")
  public ObjectListPresentation updateUser(String userName, Bindings user) {
    return updateUser(userName, user, null);
  }

  @JsField("Changes an existing user’s password")
  public ObjectListPresentation updateUser(String userName, Bindings user, Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("updateUser", userName);
    command.putAll((BSONObject)dbObjectFromMap(user));
    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsField("Creates a new user")
  public ObjectListPresentation createUser(Bindings user) {
    return createUser(user, null);
  }

  @JsField("Creates a new user")
  public ObjectListPresentation createUser(Bindings user, Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("createUser", user.get("user"));
    command.putAll((BSONObject)dbObjectFromMap(user));
    command.remove("user");
    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  public ObjectListPresentation getCollectionInfos() {
    return JsApiUtils.iter(mongoDatabase.getMongoDb().listCollections());
  }

  public DB getSiblingDB(String name) {
    return new DB(mongoDatabase.getSiblingDB(name));
  }

  @JsField("Removes inherited roles from a role")
  public ObjectListPresentation revokeRolesFromRole(String roleName, List<Bindings> roles) {
    return revokeRolesFromRole(roleName, roles, null);
  }

  @JsField("Removes inherited roles from a role")
  public ObjectListPresentation revokeRolesFromRole(String roleName, List<Bindings> roles, Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("revokeRolesFromRole", roleName);
    command.put("roles", basicDbListFromList(roles));
    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsField("Specifies roles from which a user-defined role inherits privileges")
  public ObjectListPresentation grantRolesToRole(String roleName, List<Bindings> roles) {
    return grantRolesToRole(roleName, roles, null);
  }

  @JsField("Specifies roles from which a user-defined role inherits privileges")
  public ObjectListPresentation grantRolesToRole(String roleName, List<Bindings> roles, Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("grantRolesToRole", roleName);
    command.put("roles", basicDbListFromList(roles));
    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsField("Removes the specified privileges from a user-defined role")
  public ObjectListPresentation revokePrivilegesFromRole(String role, List<Bindings> privileges) {
    return revokeRolesFromUser(role, privileges, null);
  }

  @JsField("Removes the specified privileges from a user-defined role")
  public ObjectListPresentation revokePrivilegesFromRole(String role, List<Bindings> privileges, Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("revokePrivilegesFromRole", role);
    command.put("privileges", basicDbListFromList(privileges));

    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsField("Assigns privileges to a user-defined role")
  public ObjectListPresentation grantPrivilegesToRole(String role, List<Bindings> privileges) {
    return grantRolesToUser(role, privileges, null);
  }

  @JsField("Assigns privileges to a user-defined role")
  public ObjectListPresentation grantPrivilegesToRole(String role, List<Bindings> privileges, Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("grantPrivilegesToRole", role);
    command.put("privileges", basicDbListFromList(privileges));

    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsField("Returns information for the specified role")
  public ObjectListPresentation getRole(String roleName) {
    BasicDBObjectBuilder builder = new BasicDBObjectBuilder();
    builder.push("rolesInfo").add("role", roleName).add("db", mongoDatabase.getName()).pop();
    return mongoDatabase.runCommand((Bson)builder.get());
  }

  @JsField("Returns information for all the user-defined roles in a database")
  public ObjectListPresentation getRoles() {
    return mongoDatabase.runCommand(new BasicDBObject("rolesInfo", 1));
  }

  @JsField("Deletes all user-defined roles associated with a database")
  public ObjectListPresentation dropAllRoles() {
    return dropAllRoles(null);
  }

  @JsField("Deletes all user-defined roles associated with a database")
  public ObjectListPresentation dropAllRoles(Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("dropAllRolesFromDatabase", 1);
    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsField("Deletes a user-defined role")
  public ObjectListPresentation dropRole(String roleName) {
    return dropRole(roleName, null);
  }

  @JsField("Deletes a user-defined role")
  public ObjectListPresentation dropRole(String roleName, Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("dropRole", roleName);
    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsField("Updates a user-defined role")
  public ObjectListPresentation updateRole(String roleName, Bindings role) {
    return updateRole(roleName, role, null);
  }

  @JsField("Updates a user-defined role")
  public ObjectListPresentation updateRole(String roleName, Bindings role, Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("updateRole", roleName);
    command.putAll((BSONObject)dbObjectFromMap(role));
    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsField("Creates a role and specifies its privileges")
  public ObjectListPresentation createRole(Bindings role) {
    return createRole(role, null);
  }

  @JsField("Creates a role and specifies its privileges")
  public ObjectListPresentation createRole(Bindings role, Bindings writeConcern) {
    BasicDBObject command = new BasicDBObject("createRole", role.get("role"));
    command.putAll((BSONObject) dbObjectFromMap(role));
    command.remove("role");
    if (writeConcern != null) {
      command.put("writeConcern", dbObjectFromMap(writeConcern));
    }
    return mongoDatabase.runCommand(command);
  }

  @JsIgnore
  @Override
  public String toString() {
    return mongoDatabase.getName();
  }
}
