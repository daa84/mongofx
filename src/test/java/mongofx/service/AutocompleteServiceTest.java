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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import mongofx.service.AutocompleteService.Suggest;

public class AutocompleteServiceTest {

  @Test
  public void test() {
    AutocompleteService service = new AutocompleteService();
    List<Suggest> result = service.find(Arrays.asList("db", "getCollection", "find"));

    Assert.assertEquals(1, result.size());
  }

  @Test
  public void partSearchTest() {
    AutocompleteService service = new AutocompleteService();
    List<Suggest> result = service.find(Arrays.asList("db", "getCollection", "fi"));

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("find", result.get(0).getName());
    Assert.assertEquals("nd", result.get(0).getInserPart());
  }

  @Test
  public void emptyTest() {
    AutocompleteService service = new AutocompleteService();
    List<Suggest> result = service.find(Arrays.asList("db", "getCollection", "tada"));

    Assert.assertEquals(0, result.size());
  }

  @Test
  public void AllTest() {
    AutocompleteService service = new AutocompleteService();
    List<Suggest> result = service.find(Arrays.asList("db", "getCollection", ""));

    Assert.assertFalse(result.isEmpty());
  }
}
