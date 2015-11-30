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
package mongofx.js.support;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class JsPathBuilderTest {

  @Test
  public void test() {
    String code = "function xx() {" //
        + "callFunction();" //
        + "db . getCollection(\"collection\").count";

    List<String> path = JsPathBuilder.buildPath(code).get();

    Assert.assertEquals(3, path.size());
    Assert.assertEquals("db", path.get(0));
    Assert.assertEquals("getCollection", path.get(1));
    Assert.assertEquals("count", path.get(2));
  }

  @Test
  public void testLastEmpty() {
    String code = "function xx() {" //
        + "callFunction();" //
        + "db . getCollection(\"collection\").";

    List<String> path = JsPathBuilder.buildPath(code).get();

    Assert.assertEquals(3, path.size());
    Assert.assertEquals("db", path.get(0));
    Assert.assertEquals("getCollection", path.get(1));
    Assert.assertEquals("", path.get(2));
  }
}
