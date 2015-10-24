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
package mongofx.js.api;

import javax.script.SimpleBindings;

import org.junit.Assert;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.IndexOptions;

public class JsApiUtilsTest {

  @Test
  public void buildOptionsTest() {
    SimpleBindings options = new SimpleBindings();

    SimpleBindings weights = new SimpleBindings();
    weights.put("TEST_FIELD", 1);

    options.put("unique", true);
    options.put("weights", weights);
    IndexOptions buildOptions = JsApiUtils.buildOptions(new IndexOptions(), options);

    Assert.assertTrue(buildOptions.isUnique());
    BasicDBObject weightsTarget = (BasicDBObject)buildOptions.getWeights();

    Assert.assertEquals(1, weightsTarget.get("TEST_FIELD"));
  }
}
