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

import mongofx.service.suggest.TypeAutocompleteService.FieldDescription;

public class Suggest {
  private final String name;
  private final FieldDescription fieldDescription;
  private final SuggestAction action;

  public static final SuggestAction SIMPLE_INSERT_ACTION = (c, s) -> c.insert(s.getName());

  public boolean isFunction() {
    return fieldDescription != null && fieldDescription.isFunction();
  }

  public static class BackReplaceInsertAction implements SuggestAction {
    protected int back;
    protected String text;

    public BackReplaceInsertAction(int back, String text) {
      super();
      this.back = back;
      this.text = text;
    }

    @Override
    public void insert(SuggestContext c, Suggest s) {
      if (text != null) {
        c.replace(back, text);
      } else {
        c.replace(back, s.getName());
      }
    }
  }

  public Suggest(String name, SuggestAction action) {
    super();
    this.name = name;
    this.action = action;
    this.fieldDescription = null;
  }

  public Suggest(FieldDescription fieldDescription, SuggestAction action) {
    this.name = fieldDescription.getName();
    this.fieldDescription = fieldDescription;
    this.action = action;
  }

  public Suggest(FieldDescription e) {
    this(e, SIMPLE_INSERT_ACTION);
  }

  public String getName() {
    return name;
  }

  public String getParametersHintOptional() {
    if (fieldDescription == null) {
      return "";
    }
    return fieldDescription.getParametersHintOptional();
  }

  public String getParametersHintRequired() {
    if (fieldDescription == null) {
      return "";
    }
    return fieldDescription.getParametersHintRequired();
  }

  @Override
  public String toString() {
    return name;
  }

  public void apply(SuggestContext suggestContext) {
    action.insert(suggestContext, this);
  }
}