package mongofx.service;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import mongofx.service.AutocompleteService.FieldDescription;

public class AutocompleteServiceTest {

  @Test
  public void test() {
    AutocompleteService service = new AutocompleteService();
    List<FieldDescription> result = service.findAfterDb(Arrays.asList("getCollection", "find"));

    Assert.assertEquals(1, result.size());
  }

  @Test
  public void partSearchTest() {
    AutocompleteService service = new AutocompleteService();
    List<FieldDescription> result = service.findAfterDb(Arrays.asList("getCollection", "fi"));

    Assert.assertEquals(1, result.size());
  }

  @Test
  public void emptyTest() {
    AutocompleteService service = new AutocompleteService();
    List<FieldDescription> result = service.findAfterDb(Arrays.asList("getCollection", "tada"));

    Assert.assertEquals(0, result.size());
  }

  @Test
  public void AllTest() {
    AutocompleteService service = new AutocompleteService();
    List<FieldDescription> result = service.findAfterDb(Arrays.asList("getCollection", ""));

    Assert.assertFalse(result.isEmpty());
  }
}
