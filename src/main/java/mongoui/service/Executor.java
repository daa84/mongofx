package mongoui.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.inject.Singleton;

@Singleton
public class Executor {
  private ExecutorService executor = Executors.newSingleThreadExecutor();

  public void execute(Runnable r) {
    executor.execute(r);
  }

  public void stop() {
    executor.shutdownNow();
  }
}
