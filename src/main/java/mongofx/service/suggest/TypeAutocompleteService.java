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
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import com.google.inject.Singleton;

import com.sun.org.apache.xpath.internal.operations.Bool;
import mongofx.js.api.DB;
import mongofx.js.api.JsIgnore;
import mongofx.js.api.RS;

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
    jsRootFields.put("rs", new FieldDescription("rs", RS.class));
    loadJsInfo(DB.class);
    loadJsInfo(RS.class);

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
        .map(e -> new Suggest(e.getValue(), new FunctionSuggestAction(path.length(), e.getValue().methods)))
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

    private String parametersHintRequired;
    private String parametersHintOptional;

    public FieldDescription(String name, Class<?> returnType) {
      this.name = name;
      this.fieldType = returnType;
    }

    private void buildParametersHint() {
      if (parametersHintRequired != null) {
        return;
      }

      if (methods == null || methods.isEmpty()) {
        parametersHintOptional = "";
        parametersHintRequired = "";
      }

      int minArgs = Integer.MAX_VALUE;
      List<String> args = new LinkedList<>();
      for (Method method : methods) {
        minArgs = Math.min(minArgs, method.getParameterCount());
        Parameter[] parameters = method.getParameters();
        for(int i = args.size(); i < method.getParameterCount(); i++) {
          Parameter parameter = parameters[i];
          args.add(convertTypeToName(parameter));
        }
      }

      parametersHintRequired = args.subList(0, minArgs).stream().collect(Collectors.joining(", "));
      if (args.size() > minArgs) {
        if (minArgs != 0) {
          parametersHintOptional = args.stream().skip(minArgs).collect(Collectors.joining(", ", ", ", ""));
        } else {
          parametersHintOptional = args.stream().skip(minArgs).collect(Collectors.joining(", "));
        }
      } else {
        parametersHintOptional = "";
      }
    }

    private String convertTypeToName(Parameter parameter) {
      Class<?> type = parameter.getType();
      if (String.class.isAssignableFrom(type)) {
        return "str";
      }
      if (Map.class.isAssignableFrom(type)) {
        return "obj";
      }
      if (List.class.isAssignableFrom(type)) {
        return "arr";
      }
      if (Boolean.class.isAssignableFrom(type)) {
        return "bool";
      }
      if (Number.class.isAssignableFrom(type)) {
        return "num";
      }
      return type.getSimpleName();
    }

    public String getParametersHintRequired() {
      buildParametersHint();
      return parametersHintRequired;
    }

    public String getParametersHintOptional() {
      buildParametersHint();
      return parametersHintOptional;
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

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }

    public boolean isFunction() {
      return methods != null && !methods.isEmpty();
    }
  }
}
