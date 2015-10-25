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
package mongofx.service;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

@Singleton
public class PropertiesService {
  private static final Logger log = LoggerFactory.getLogger(PropertiesService.class);

  private static Properties properties = new Properties();

  static {
    try {
      properties.load(PropertiesService.class.getResourceAsStream("/app.properties"));
    }
    catch (IOException e) {
      log.error("IOException:",e);
    }
  }

  public String getVersion() {
    return properties.getProperty("mongofx.version");
  }
}
