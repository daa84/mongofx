package mongofx.js.support;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class JsAntlrPathBuilderTest {
  //  @Test
  //  public void test() {
  //    String code = "function xx() {" //
  //        + "callFunction();" //
  //        + "db . getCollection(\"collection\").count";
  //
  //    List<String> path = JsAntlrPathBuilder.buildPath(code, code.length()).get();
  //    Assert.assertEquals(3, path.size());
  //    Assert.assertEquals("db", path.get(0));
  //    Assert.assertEquals("getCollection", path.get(1));
  //    Assert.assertEquals("count", path.get(2));
  //  }

  @Test
  public void simplePathTest() {
    List<String> path = JsAntlrPathBuilder.buildPath("x.y.z", "x.y.z".length() - 1).get();
    assertEquals(3, path.size());
    assertEquals("x", path.get(0));
    assertEquals("y", path.get(1));
    assertEquals("z", path.get(2));
  }
}
