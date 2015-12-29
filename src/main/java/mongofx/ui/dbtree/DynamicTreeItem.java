// This file is part of MongoFX.
//
// MongoFX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
// MongoFX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with MongoFX.  If not, see <http://www.gnu.org/licenses/>.

//
// Copyright (c) Andrey Dubravin, 2015
//
package mongofx.ui.dbtree;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;
import mongofx.service.Executor;
import mongofx.ui.msg.PopupService;

public class DynamicTreeItem extends TreeItem<DbTreeValue> {
  private static final Logger log = LoggerFactory.getLogger(DynamicTreeItem.class);

  private final Executor executor;
  private final PopupService popupService;

  private boolean loaded = false;
  private final Function<DbTreeValue, List<TreeItem<DbTreeValue>>> supplier;
  private Runnable onFiled;

  private final ProgressIndicator progress;
  private Node graphic;

  private LoadTask currentLoadTask;

  public DynamicTreeItem(DbTreeValue value, Node graphic, Executor executor, PopupService popupService,
      Function<DbTreeValue, List<TreeItem<DbTreeValue>>> supplier) {
    super(value, graphic);
    this.supplier = supplier;
    this.executor = executor;
    this.popupService = popupService;

    progress = new ProgressIndicator();
    progress.setPrefSize(15, 15);

    parentProperty().addListener(e -> {
      if (getParent() == null && currentLoadTask != null) {
        currentLoadTask.cancel(true);
      }
    });
  }

  public void setOnFiled(Runnable onFiled) {
    this.onFiled = onFiled;
  }

  @Override
  public boolean isLeaf() {
    return loaded && super.getChildren().isEmpty();
  }

  @Override
  public ObservableList<TreeItem<DbTreeValue>> getChildren() {
    ObservableList<TreeItem<DbTreeValue>> children = super.getChildren();
    if (!loaded) {
      load(children);
    }
    return children;
  }

  private void load(ObservableList<TreeItem<DbTreeValue>> children) {
    loaded = true;
    graphic = getGraphic();
    setGraphic(progress);
    if (currentLoadTask != null) {
      currentLoadTask.cancel(true);
    }
    currentLoadTask = new LoadTask(children);
    executor.execute(currentLoadTask);
  }

  public void reload() {
    load(super.getChildren());
  }

  private class LoadTask extends Task<List<TreeItem<DbTreeValue>>> {
    private final ObservableList<TreeItem<DbTreeValue>> children;
    private final DbTreeValue value;

    public LoadTask(ObservableList<TreeItem<DbTreeValue>> children) {
      super();
      this.children = children;
      this.value = DynamicTreeItem.this.getValue();
    }

    @Override
    protected List<TreeItem<DbTreeValue>> call() throws Exception {
      return supplier.apply(value);
    }

    @Override
    protected void done() {
      //FIXME: does not work on empty result
      setGraphic(graphic);
      currentLoadTask = null;
    }

    @Override
    protected void succeeded() {
      List<TreeItem<DbTreeValue>> result = getValue();
      value.setCount(result.size());
      children.setAll(result);
    }

    @Override
    protected void failed() {
      loaded = false;
      if (onFiled != null) {
        onFiled.run();
      }
      Throwable exception = getException();
      if (exception != null) {
        log.warn("Error", exception);
        popupService.showError("Can't connect to MongoDB", exception.getMessage());
      }
      else {
        popupService.showError("Can't connect to MongoDB", "");
      }
    }
  }

}
