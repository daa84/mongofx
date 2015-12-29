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
package mongofx.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.inject.Singleton;

@Singleton
public class Executor {
  private ExecutorService executor = Executors.newSingleThreadExecutor();
  
  private ExecutorService multiThreadExecutor = new ThreadPoolExecutor(5, 5,
      0L, TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue<>(20), r -> {
      	Thread t = new Thread(r, "Script eval thread pool");
      	t.setDaemon(true);
      	t.setPriority(Thread.NORM_PRIORITY);
				return t;
      });

  public void execute(Runnable r) {
    executor.execute(r);
  }
  
  public void executeMany(Runnable r) {
  	multiThreadExecutor.execute(r);
  }

  public void stop() {
    executor.shutdownNow();
    multiThreadExecutor.shutdownNow();
  }
}
