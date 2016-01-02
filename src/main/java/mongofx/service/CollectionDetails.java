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
// Copyright (c) Andrey Dubravin, 2016
//
package mongofx.service;

import mongofx.ui.main.DocumentUtils;

public class CollectionDetails {
  private final String name;
  private final Integer count;
  private final Integer storageSize;
  private final Integer totalIndexSize;
  private final boolean wiredTiger;

  public CollectionDetails(String name, Integer count, Integer storageSize, Integer totalIndexSize, boolean wiredTiger) {
    this.name = name;
    this.count = count;
    this.storageSize = storageSize;
    this.totalIndexSize = totalIndexSize;
    this.wiredTiger = wiredTiger;
  }

  public Integer getCount() {
    return count;
  }

  public Integer getStorageSize() {
    return storageSize;
  }

  public String getStorageSizeHR() {
    return DocumentUtils.bytesIntoHumanReadable(storageSize);
  }

  public Integer getTotalIndexSize() {
    return totalIndexSize;
  }

  public String getTotalIndexSizeHR() {
    return DocumentUtils.bytesIntoHumanReadable(totalIndexSize);
  }

  public boolean isWiredTiger() {
    return wiredTiger;
  }

  public String getName() {
    return name;
  }
}
