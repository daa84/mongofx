package mongofx.ui.main;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.script.ScriptException;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.fxmisc.richtext.CodeArea;
import org.reactfx.EventStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import mongofx.js.api.ObjectListPresentation;
import mongofx.js.api.TextPresentation;
import mongofx.service.AutocompleteService;
import mongofx.service.MongoDatabase;

public class QueryTabController {

  private static final Logger log = LoggerFactory.getLogger(QueryTabController.class);

  @FXML
  private CodeArea codeArea;

  @Inject
  private UIBuilder uiBuilder;

  @FXML
  private TreeTableView<DocumentTreeValue> queryResultTree;
  @FXML
  private CodeArea queryResultText;

  private MongoDatabase mongoDatabase;

  @FXML
  private TextField limitResult;

  @FXML
  private ToggleButton viewAsTree;

  @FXML
  private ToggleButton viewAsText;

  private ObjectListPresentation objectListResult;

  @FXML
  private ToggleGroup viewToogleGroup;

  @Inject
  private AutocompleteService autocompleteService;

  @FXML
  protected void initialize() {
    new CodeAreaBuilder(codeArea, uiBuilder.getPrimaryStage()).setup().setupAutocomplete(autocompleteService);
    new CodeAreaBuilder(queryResultText, uiBuilder.getPrimaryStage()).setup();
    EventStreams.changesOf(viewToogleGroup.selectedToggleProperty()).subscribe(e -> updateResultListView());
  }

  public void setDb(MongoDatabase mongoDatabase, String collectionName) {
    this.mongoDatabase = mongoDatabase;
    codeArea.replaceText("db.getCollection('" + collectionName + "').find({})");
    codeArea.getUndoManager().forgetHistory();
  }

  @FXML
  public void codeAreaOnKeyReleased(KeyEvent ev) {
    if (ev.getCode() == KeyCode.F5) {
      executeScript();
    }
  }

  public void executeScript() {
    try {
      Optional<Object> documents = mongoDatabase.eval(codeArea.getText());
      buildResultView(documents);
    }
    catch (ScriptException e) {
      showOnlyText(e.getMessage());
      log.error("Error execute script", e);
    }
  }

  private void buildResultView(Optional<Object> documents) {
    if (documents.isPresent()) {
      Object result = documents.get();

      objectListResult = null;

      if (result instanceof TextPresentation) {
        showOnlyText(String.valueOf(result));
      }
      else {
        setViewModeVisible(true);
        objectListResult = (ObjectListPresentation)result;
        updateResultListView();
      }
    }
    else {
      showOnlyText("Empty result");
    }
  }

  private void showOnlyText(String text) {
    setViewModeVisible(false);
    queryResultText.replaceText(text);
    showText();
  }

  private void updateResultListView() {
    Stream<Document> resultStream = StreamSupport.stream(objectListResult.spliterator(), false)//
        .limit(Integer.parseInt(limitResult.getText()));

    if (viewAsTree.isSelected()) {
      TreeItem<DocumentTreeValue> root = new TreeItem<>();
      buildTreeFromDocuments(root, resultStream);
      queryResultTree.setRoot(root);
      showTree();
    }
    else {
      queryResultText.replaceText(String.valueOf(buildTextFromList(resultStream)));
      queryResultText.selectRange(0, 0);
      showText();
    }
  }

  private void showText() {
    queryResultTree.setVisible(false);
    queryResultText.setVisible(true);
  }

  private void showTree() {
    queryResultTree.setVisible(true);
    queryResultText.clear();
    queryResultText.setVisible(false);
  }

  private String buildTextFromList(Stream<Document> resultStream) {
    return resultStream.map(d -> d.toJson(new JsonWriterSettings(JsonMode.SHELL, true)))
        .collect(Collectors.joining(",\n", "[", "]"));
  }

  private void setViewModeVisible(boolean b) {
    viewAsText.setVisible(b);
    viewAsTree.setVisible(b);
  }

  private void buildTreeFromDocuments(TreeItem<DocumentTreeValue> root, Stream<? extends Object> documents) {
    root.getChildren().addAll(documents.map(d -> new TreeItem<>(new DocumentTreeValue(null, d)))
        .peek(i -> buildChilds(i)).collect(Collectors.toList()));
  }

  private void buildChilds(TreeItem<DocumentTreeValue> i) {
    Object value = i.getValue().getValue();
    if (value instanceof Document) {
      i.getChildren()
          .addAll(((Document)value).entrySet().stream().map(f -> mapFieldToItem(f)).collect(Collectors.toList()));
    }
  }

  @SuppressWarnings("unchecked")
  private TreeItem<DocumentTreeValue> mapFieldToItem(Entry<String, Object> f) {
    Object value = f.getValue();
    if (value instanceof Document) {
      TreeItem<DocumentTreeValue> root = new TreeItem<>(new DocumentTreeValue(f.getKey(), value));
      buildChilds(root);
      return root;
    }
    if (value instanceof List) {
      TreeItem<DocumentTreeValue> root = new TreeItem<>(new DocumentTreeValue(f.getKey(), value));
      buildTreeFromArray(root, (List<Object>)value);
      return root;
    }
    return new TreeItem<>(new DocumentTreeValue(f.getKey(), f.getValue()));
  }

  private void buildTreeFromArray(TreeItem<DocumentTreeValue> root, List<Object> documents) {
    for (int i = 0; i < documents.size(); i++) {
      Object item = documents.get(i);
      TreeItem<DocumentTreeValue> treeItem = new TreeItem<>(new DocumentTreeValue(String.valueOf(i), item));
      root.getChildren().add(treeItem);
      buildChilds(treeItem);
    }
  }

  public void startTab() {
    int startIdx = codeArea.getText().indexOf("{");
    if (startIdx < 0) {
      startIdx = 0;
    }
    startIdx++;
    codeArea.selectRange(startIdx, startIdx);
    codeArea.requestFocus();
    executeScript();
  }
}
