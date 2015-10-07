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
