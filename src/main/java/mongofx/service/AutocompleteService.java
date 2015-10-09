package mongofx.service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.google.inject.Singleton;

import mongofx.js.api.DB;

@Singleton
public class AutocompleteService {
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

  public List<Suggest> find(List<String> paths) {
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
        root = jsInfo.get(fieldDescription.fieldType);
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

  private List<Suggest> getRootFields() {
    return jsRootFields.values().stream().map(e -> new Suggest(e)).collect(Collectors.toList());
  }

  private List<Suggest> find(NavigableMap<String, FieldDescription> root, String path) {
    NavigableMap<String, FieldDescription> tailMap = root.tailMap(path, true);
    return tailMap.entrySet().stream().filter(e -> e.getKey().startsWith(path)) //
        .map(e -> new Suggest(e.getValue().name, e.getValue().name.substring(path.length())))
        .collect(Collectors.toList());
  }

  private void loadJsInfo(Class<?> clazz) {
    if (jsInfo.containsKey(clazz)) {
      return;
    }

    NavigableMap<String, FieldDescription> fieldsInfo = new TreeMap<>();
    jsInfo.put(clazz, fieldsInfo);

    for (Method method : clazz.getDeclaredMethods()) {
      Class<?> returnType = method.getReturnType();
      if (!Modifier.isPublic(method.getModifiers())) {
        continue;
      }
      fieldsInfo.put(method.getName(), new FieldDescription(method.getName(), returnType));
      Package package1 = returnType.getPackage();
      if (package1 != null && "mongofx.js.api".equals(package1.getName())) {
        loadJsInfo(returnType);
      }
    }
  }

  public static class Suggest {
    private final String name;
    private final String inserPart;

    public Suggest(String name, String inserPart) {
      super();
      this.name = name;
      this.inserPart = inserPart;
    }

    public Suggest(FieldDescription e) {
      name = e.name;
      inserPart = name;
    }

    public String getName() {
      return name;
    }

    public String getInserPart() {
      return inserPart;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public static class FieldDescription {
    private final Class<?> fieldType;
    private final String name;

    public FieldDescription(String name, Class<?> returnType) {
      this.name = name;
      fieldType = returnType;
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
