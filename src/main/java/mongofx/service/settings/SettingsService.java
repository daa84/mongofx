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
