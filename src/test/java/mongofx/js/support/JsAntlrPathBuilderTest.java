package mongofx.js.support;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class JsAntlrPathBuilderTest {
  @Test
  public void test() {
    String code = "function xx() {" //
        + "callFunction();" //
        + "db . getCollection(\"collection\").count";

    List<String> path = new JsAntlrPathBuilder().buildPath("x.y.z = 10", "x.y.".length()).get();
    Assert.assertEquals(3, path.size());
    Assert.assertEquals("db", path.get(0));
    Assert.assertEquals("getCollection", path.get(1));
    Assert.assertEquals("count", path.get(2));
  }
}
