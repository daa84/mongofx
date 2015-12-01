package mongofx.service.suggest;

import org.junit.Assert;
import org.junit.Test;

import mongofx.service.suggest.TemplateAutocompleteService.InsertTemplateAction;

public class InsertTemplateActionTest {
	@Test
	public void testVarReplace() {
		InsertTemplateAction a = new InsertTemplateAction(5, "");
		String result = a.processVars("/*$collectionName*/", new SuggestContext("test", null));
		Assert.assertEquals("test", result);
	}
}
