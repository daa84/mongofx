//$Id:$
//
// <p>Description: </p>
// <p>Copyright: Copyright (c) 2005</p>
// <p>Company: ISB AG</p>
//
// MongoUI
// Created on 12 сент. 2015 г.
//
package mongoui.ui.main;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class NewDbController {
  @Inject
  private UIBuilder uiBuilder;

  @FXML
  private TextField dbName;

  @FXML
  public void back() {
    uiBuilder.back();
  }

  @FXML
  public void create() {
  }

}
