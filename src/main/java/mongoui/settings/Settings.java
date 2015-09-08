package mongoui.settings;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Settings {
  private List<ConnectionSettings> connections;

  public List<ConnectionSettings> getConnections() {
    if (connections == null) {
      connections = new ArrayList<>();
    }
    return connections;
  }

  public void setConnections(List<ConnectionSettings> connections) {
    this.connections = connections;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return connections == null || connections.isEmpty();
  }
}
