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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.google.inject.Inject;

import mongofx.js.api.Collection;
import mongofx.js.api.DB;
import mongofx.service.MongoDatabase;
import mongofx.service.Suggest;
import mongofx.service.TemplateAutocompleteService;
import mongofx.service.TypeAutocompleteService;
import mongofx.service.TypeAutocompleteService.FieldDescription;

public class AutocompletionEngine {

  @Inject
  private TypeAutocompleteService jsService;

  @Inject
  private TemplateAutocompleteService templateService;

  private MongoDatabase mongoDb;

  public List<Suggest> find(List<String> paths) {
    List<Suggest> result = new LinkedList<>();
    result.addAll(jsService.find(paths, buildDynamicInfo()));
    if (paths != null && !paths.isEmpty()) {
      result.addAll(templateService.find(paths.get(paths.size() - 1)));
    }
    return result;
  }


  private Map<Class<?>, NavigableMap<String, FieldDescription>> buildDynamicInfo() {
    if (mongoDb == null) {
      return Collections.emptyMap();
    }

    List<String> collections = mongoDb.getCachedCollections();
    Map<Class<?>, NavigableMap<String, FieldDescription>> dynamicInfo = new HashMap<>(1);
    NavigableMap<String, FieldDescription> dbDynamicFields = new TreeMap<>();
    for (String colletionName : collections) {
      dbDynamicFields.put(colletionName, new FieldDescription(colletionName, Collection.class));
    }
    dynamicInfo.put(DB.class, dbDynamicFields);
    return dynamicInfo;
  }


  public void setMongoDb(MongoDatabase mongoDb) {
    this.mongoDb = mongoDb;
  }
}
