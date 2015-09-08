package mongoui.ui.main;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javafx.application.Application;
import javafx.stage.Stage;
import mongoui.settings.SettingsService;

public class Main extends Application {
  @Override
  public void start(Stage primaryStage) {
    try {
      Injector injector = Guice.createInjector(new MainModule());
      SettingsService settings = injector.getInstance(SettingsService.class);
      settings.load();
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

  public static class MainModule extends AbstractModule {

    @Override
    protected void configure() {
    }

  }
}
