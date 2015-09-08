package mongoui.settings;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;

@Singleton
public class SettingsService {
  private static final Logger log = LoggerFactory.getLogger(SettingsService.class);
  private static final String SETTINGS_FILE = "settings.json";
  private Settings settings = new Settings();

  public void load() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      File settingsFile = new File(SETTINGS_FILE);
      if (settingsFile.exists()) {
        settings = mapper.readValue(settingsFile, Settings.class);
      }
    }
    catch (IOException e) {
      log.error("Error", e);
      settings = new Settings();
    }
  }

  public boolean isEmpty() {
    return settings.isEmpty();
  }

  public Settings getSettings() {
    return settings;

  }

  public void save() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(new File(SETTINGS_FILE), settings);
  }

}
