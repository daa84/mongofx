package mongofx.js.support;

import org.junit.Test;

import static org.junit.Assert.*;

public class JsFormatterTest {

  @Test
  public void testBeatufy() throws Exception {
    JsFormatter beautify = new JsFormatter();
    String res = beautify.beautify("function(){test.call}");
    String expected = "function() {\n" +
        "    test.call\n" +
        "}";
    assertEquals(expected, res);
  }
}