package mongofx.codearea;


import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import mongofx.service.suggest.Suggest;

public class SuggestListView extends ListView<Suggest> {
  public SuggestListView() {
    super();
    setCellFactory(c -> new SuggestListCell());
  }

  public static class SuggestListCell extends ListCell<Suggest> {
    private Label name = new Label();
    private Label first = new Label("(");
    private Label requiredArgs = new Label();
    private Label optionalArgs = new Label();
    private Label last = new Label(")");
    private HBox pane = new HBox(name, first, requiredArgs, optionalArgs, last);

    public SuggestListCell() {
      super();
      getStyleClass().add("suggest-args");
      first.getStyleClass().add("suggest-args");
      last.getStyleClass().add("suggest-args");
      requiredArgs.getStyleClass().addAll("suggest-args", "suggest-required-args");
      optionalArgs.getStyleClass().addAll("suggest-args", "suggest-optional-args");
    }

    @Override
    protected void updateItem(Suggest item, boolean empty) {
      super.updateItem(item, empty);
      if (empty) {
        setGraphic(null);
      } else {
        if (item.isFunction()) {
          setFunctionArgsVisible(true);
          name.setText(item.getName());
          requiredArgs.setText(item.getParametersHintRequired());
          optionalArgs.setText(item.getParametersHintOptional());
        } else {
          name.setText(item.getName());
          setFunctionArgsVisible(false);
        }
        setGraphic(pane);
      }
    }

    private void setFunctionArgsVisible(boolean visible) {
      requiredArgs.setVisible(visible);
      optionalArgs.setVisible(visible);
      last.setVisible(visible);
      first.setVisible(visible);
    }
  }
}
