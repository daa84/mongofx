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
package mongofx.service.suggest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import com.google.inject.Singleton;

import mongofx.js.api.DB;
import mongofx.js.api.JsIgnore;
import mongofx.service.suggest.Suggest.BackReplaceInsertAction;

@Singleton
public class TypeAutocompleteService {
  private final Map<Class<?>, NavigableMap<String, FieldDescription>> jsInfo = new HashMap<>();
  private final NavigableMap<String, FieldDescription> jsRootFields = new TreeMap<>();
  private boolean initialized = false;

  private void initialize() {
    if (initialized) {
      return;
    }

    jsRootFields.put("db", new FieldDescription("db", DB.class));
    loadJsInfo(DB.class);

    initialized = true;
  }

  public List<Suggest> find(List<String> paths, Map<Class<?>, NavigableMap<String, FieldDescription>> dynamicJsInfo) {
    initialize();

    if (paths.isEmpty()) {
      return Collections.emptyList();
    }

    String firstPathElement = paths.get(0);
    if (firstPathElement.isEmpty()) {
      return getRootFields();
    }
    NavigableMap<String, FieldDescription> root = jsRootFields;

    if (paths.size() > 1) {
      for (String path : paths.subList(0, paths.size() - 1)) {
        FieldDescription fieldDescription = root.get(path);
        if (fieldDescription == null) {
          return Collections.emptyList();
        }
        root = getJoinedTypeInfo(fieldDescription, dynamicJsInfo);
        if (root == null) {
          break;
        }
      }
    }

    if (root == null) {
      return Collections.emptyList();
    }

    return find(root, paths.get(paths.size() - 1));
  }

  private NavigableMap<String, FieldDescription> getJoinedTypeInfo(FieldDescription fieldDescription,
      Map<Class<?>, NavigableMap<String, FieldDescription>> dynamicJsInfo) {
    NavigableMap<String, FieldDescription> staticItemType = jsInfo.get(fieldDescription.fieldType);
    NavigableMap<String, FieldDescription> dynamicItemType = dynamicJsInfo.get(fieldDescription.fieldType);
    return joinTypeInfo(staticItemType, dynamicItemType);
  }

  private NavigableMap<String, FieldDescription> joinTypeInfo(NavigableMap<String, FieldDescription> staticItemType,
      NavigableMap<String, FieldDescription> dynamicItemType) {
    if (dynamicItemType == null) {
      return staticItemType;
    }
    if (staticItemType == null) {
      return dynamicItemType;
    }

    NavigableMap<String, FieldDescription> joinedMap = new TreeMap<>();
    joinedMap.putAll(staticItemType);
    joinedMap.putAll(dynamicItemType);
    return joinedMap;
  }

  private List<Suggest> getRootFields() {
    return jsRootFields.values().stream().map(Suggest::new).collect(Collectors.toList());
  }

  private List<Suggest> find(NavigableMap<String, FieldDescription> root, String path) {
    NavigableMap<String, FieldDescription> tailMap = root.tailMap(path, true);
    return tailMap.entrySet().stream().filter(e -> e.getKey().startsWith(path)) //
        .map(e -> new Suggest(e.getValue().name, new FunctionSuggestAction(path.length(), e.getValue().methods)))
        .collect(Collectors.toList());
  }

  private void loadJsInfo(Class<?> clazz) {
    if (jsInfo.containsKey(clazz)) {
      return;
    }

    NavigableMap<String, FieldDescription> fieldsInfo = new TreeMap<>();
    jsInfo.put(clazz, fieldsInfo);

    for (Method method : clazz.getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers()) || method.getAnnotation(JsIgnore.class) != null) {
        continue;
      }

      Class<?> returnType = method.getReturnType();
      fieldsInfo.computeIfAbsent(method.getName(), k -> new FieldDescription(method.getName(), returnType)).join(method);
      Package package1 = returnType.getPackage();
      if (package1 != null && "mongofx.js.api".equals(package1.getName())) {
        loadJsInfo(returnType);
      }
    }
  }

  public static class FieldDescription {
    private final Class<?> fieldType;
    private final String name;
    private List<Method> methods;

    public FieldDescription(String name, Class<?> returnType) {
      this.name = name;
      this.fieldType = returnType;
    }

    private void join(Method method) {
      if (method == null) {
        return;
      }

      if (methods == null) {
        methods = new ArrayList<>(1);
      }
      methods.add(method);
    }

    public Class<?> getFieldType() {
      return fieldType;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
