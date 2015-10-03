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
