//$Id:$
//
// <p>Description: </p>
// <p>Copyright: Copyright (c) 2005</p>
// <p>Company: ISB AG</p>
//
// MongoUI
// Created on 12 сент. 2015 г.
//
package mongoui.service.js.api;

public class SimpleTextPresentation implements TextPresentation {
  private final String text;


  public SimpleTextPresentation(String text) {
    this.text = text;
  }

  public SimpleTextPresentation(long count) {
    text = String.valueOf(count);
  }

  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return text;
  }
}
