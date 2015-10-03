package mongofx;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javafx.application.Application;
import javafx.stage.Stage;
import mongofx.service.Executor;
import mongofx.service.MongoService;
import mongofx.settings.SettingsService;
import mongofx.ui.main.UIBuilder;

public class Main extends Application {
  private MongoService mongoService;
  private Executor executorService;

  @Override
  public void start(Stage primaryStage) {
    try {
      Injector injector = Guice.createInjector(new MainModule());
      SettingsService settings = injector.getInstance(SettingsService.class);
      settings.load();
      mongoService = injector.getInstance(MongoService.class);
      executorService = injector.getInstance(Executor.class);
      UIBuilder builder = injector.getInstance(UIBuilder.class);
      builder.setInjector(injector);
      builder.setStage(primaryStage);
      builder.loadSettingsWindow();
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

  public static class MainModule extends AbstractModule {

    @Override
    protected void configure() {
    }

  }
}
