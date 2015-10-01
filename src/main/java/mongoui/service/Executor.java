package mongoui.service;

import java.util.concurrent.Executors;

import com.google.inject.Singleton;

@Singleton
public class Executor {
  private java.util.concurrent.Executor executor = Executors.newSingleThreadExecutor();

  public void execute(Runnable r) {
    executor.execute(r);
  }
}
