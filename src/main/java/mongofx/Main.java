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
package mongofx;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import mongofx.service.Executor;
import mongofx.service.MongoService;
import mongofx.service.settings.SettingsService;
import mongofx.ui.main.MainFrameController;
import mongofx.ui.main.UIBuilder;

public class Main extends Application {
  private static final Logger log = LoggerFactory.getLogger(Main.class);

  private static final Injector injector = Guice.createInjector(new MainModule());

  private MongoService mongoService;
  private Executor executorService;

  @Override
  public void start(Stage primaryStage) {
    try {
      SettingsService settings = injector.getInstance(SettingsService.class);
      settings.load();
      mongoService = injector.getInstance(MongoService.class);
      executorService = injector.getInstance(Executor.class);
      UIBuilder builder = injector.getInstance(UIBuilder.class);
      builder.setInjector(injector);
      builder.setStage(primaryStage);
      MainFrameController mainController = builder.loadMainWindow();

      // runLater fix bug that makes main window unresizable if another dialog is showed
      Platform.runLater(() -> builder.showSettingsWindow(mainController));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void stop() throws Exception {
    mongoService.stop();
    executorService.stop();
  }

  public static <T> T getInstance(Class<T> clazz) {
    if (injector == null) {
      return null;
    }
    return injector.getInstance(clazz);
  }

  public static class MainModule extends AbstractModule {

    @Override
    protected void configure() {
      try {
        Properties properties = new Properties();
        properties.load(Main.class.getResourceAsStream("/app.properties"));
        Names.bindProperties(binder(), properties);
      } catch (IOException e) {
        log.error("Error load properties file", e);
      }
    }

  }
}
