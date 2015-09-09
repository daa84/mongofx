package mongoui.ui.main;

import org.fxmisc.richtext.CodeArea;

import javafx.fxml.FXML;

public class QueryTabController {

  @FXML
  private CodeArea codeArea;

  @FXML
  protected void initialize() {
    CodeAreaBuilder.setup(codeArea);
  }

  public void setDbName(String dbName) {
    codeArea.replaceText("db." + dbName + ".find({})");
  }

}
