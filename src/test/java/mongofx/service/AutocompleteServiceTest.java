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
package mongofx.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import mongofx.js.api.Collection;
import mongofx.js.api.DB;
import mongofx.service.suggest.Suggest;
import mongofx.service.suggest.SuggestContext;
import mongofx.service.suggest.TypeAutocompleteService;
import mongofx.service.suggest.TypeAutocompleteService.FieldDescription;

public class AutocompleteServiceTest {

  @Test
  public void testSmallPathDynamicMap() {
    TypeAutocompleteService service = new TypeAutocompleteService();
    Map<Class<?>, NavigableMap<String, FieldDescription>> dynamicJsInfo = new HashMap<>();
    NavigableMap<String, FieldDescription> collectionDynamic = new TreeMap<>();
    collectionDynamic.put("find2", new FieldDescription("find2", String.class));
    collectionDynamic.put("find", new FieldDescription("find", String.class));
    dynamicJsInfo.put(DB.class, collectionDynamic);
    List<Suggest> result = service.find(Arrays.asList("db", "find"), dynamicJsInfo);

    Assert.assertEquals(2, result.size());
  }

  @Test
  public void testDynamicMap() {
    TypeAutocompleteService service = new TypeAutocompleteService();
    Map<Class<?>, NavigableMap<String, FieldDescription>> dynamicJsInfo = new HashMap<>();
    NavigableMap<String, FieldDescription> collectionDynamic = new TreeMap<>();
    collectionDynamic.put("find2", new FieldDescription("find2", String.class));
    dynamicJsInfo.put(Collection.class, collectionDynamic);
    List<Suggest> result = service.find(Arrays.asList("db", "getCollection", "find"), dynamicJsInfo);

    Assert.assertEquals(2, result.size());
  }

  @Test
  public void test() {
    TypeAutocompleteService service = new TypeAutocompleteService();
    List<Suggest> result = service.find(Arrays.asList("db", "getCollection", "find"), Collections.emptyMap());

    Assert.assertEquals(1, result.size());
  }

  @Test
  public void partSearchTest() {
    TypeAutocompleteService service = new TypeAutocompleteService();
    List<Suggest> result = service.find(Arrays.asList("db", "getCollection", "fi"), Collections.emptyMap());

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("find", result.get(0).getName());
  }
  
  @Test
  public void actionInsertTest() {
  	TypeAutocompleteService service = new TypeAutocompleteService();
    List<Suggest> result = service.find(Arrays.asList("db", "getCollection", "fi"), Collections.emptyMap());

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("find", result.get(0).getName());
    
    result.get(0).apply(new SuggestContext("test", null) {
      @Override
      public void replaceAndSelect(int back, String text, int inTextPosition) {
        Assert.assertEquals("find()", text);
        Assert.assertEquals(5, inTextPosition);
        Assert.assertEquals(2, back);
      }
    });
  }

  @Test
  public void emptyTest() {
    TypeAutocompleteService service = new TypeAutocompleteService();
    List<Suggest> result = service.find(Arrays.asList("db", "getCollection", "tada"), Collections.emptyMap());

    Assert.assertEquals(0, result.size());
  }

  @Test
  public void AllTest() {
    TypeAutocompleteService service = new TypeAutocompleteService();
    List<Suggest> result = service.find(Arrays.asList("db", "getCollection", ""), Collections.emptyMap());

    Assert.assertFalse(result.isEmpty());
  }
}
