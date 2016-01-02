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
package mongofx.ui.main;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

public class DocumentUtils {

  public static String formatJson(Document doc) {
    return doc.toJson(new JsonWriterSettings(JsonMode.SHELL, true));
  }

  public static String bytesIntoHumanReadable(long bytes) {
    final long kilobyte = 1024;
    final long megabyte = kilobyte * 1024;
    final long gigabyte = megabyte * 1024;
    final long terabyte = gigabyte * 1024;

    if ((bytes >= 0) && (bytes < kilobyte)) {
      return bytes + " B";
    }
    if ((bytes >= kilobyte) && (bytes < megabyte)) {
      return (bytes / kilobyte) + " KB";
    }
    if ((bytes >= megabyte) && (bytes < gigabyte)) {
      return (bytes / megabyte) + " MB";
    }
    if ((bytes >= gigabyte) && (bytes < terabyte)) {
      return (bytes / gigabyte) + " GB";
    }
    if (bytes >= terabyte) {
      return (bytes / terabyte) + " TB";
    }

    return bytes + " Bytes";
  }


}
