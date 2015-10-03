package mongofx.js.api;

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
