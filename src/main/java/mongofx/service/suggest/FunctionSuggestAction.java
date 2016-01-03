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
package mongofx.service.suggest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class FunctionSuggestAction implements SuggestAction {

  private final int back;
  private final List<Method> methods;

  public FunctionSuggestAction(int back, List<Method> methods) {
    this.back = back;
    this.methods = methods;
  }

  @Override
  public void insert(SuggestContext c, Suggest s) {
    if (methods == null || methods.isEmpty()) {
      c.replace(back, s.getName());
    } else {
      Map.Entry<String, Integer> minimumArguments = buildMinimumArguments(methods);
      String text = String.format("%s(%s)", s.getName(), minimumArguments.getKey());
      c.replaceAndSelect(back, text, s.getName().length() + 1 + minimumArguments.getValue());
    }
  }

  public Map.Entry<String, Integer> buildMinimumArguments(List<Method> methods) {
    Method method = getMinArgsMethod(methods);

    StringBuilder args = new StringBuilder();
    Parameter[] parameters = method.getParameters();
    int cursorPosition = 0;
    for (int i = 0; i < parameters.length; i++) {
      Parameter parameter = parameters[i];
      if (i > 0) {
        args.append(",");
      }
      Class<?> type = parameter.getType();
      if (Map.class.isAssignableFrom(type)) {
        args.append("{}");
        cursorPosition = 1;
      } else if (List.class.isAssignableFrom(type)) {
        args.append("[]");
        cursorPosition = 1;
      } else if (String.class.isAssignableFrom(type)) {
        args.append("''");
        cursorPosition = 1;
      }
    }

    return new AbstractMap.SimpleEntry<>(args.toString(), cursorPosition);
  }

  private Method getMinArgsMethod(List<Method> methods) {
    Method minArgsMethod = null;
    for (Method method : methods) {
      if (minArgsMethod == null) {
        minArgsMethod = method;
      } else {
        if (minArgsMethod.getParameterCount() > method.getParameterCount()) {
          minArgsMethod = method;
        }
      }
    }
    return minArgsMethod;
  }
}
