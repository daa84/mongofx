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
    List<FieldDescription> result = service.find(Arrays.asList("db", "getCollection", "find"));

    Assert.assertEquals(1, result.size());
  }

  @Test
  public void partSearchTest() {
    AutocompleteService service = new AutocompleteService();
    List<FieldDescription> result = service.find(Arrays.asList("db", "getCollection", "fi"));

    Assert.assertEquals(1, result.size());
  }

  @Test
  public void emptyTest() {
    AutocompleteService service = new AutocompleteService();
    List<FieldDescription> result = service.find(Arrays.asList("db", "getCollection", "tada"));

    Assert.assertEquals(0, result.size());
  }

  @Test
  public void AllTest() {
    AutocompleteService service = new AutocompleteService();
    List<FieldDescription> result = service.find(Arrays.asList("db", "getCollection", ""));

    Assert.assertFalse(result.isEmpty());
  }
}
