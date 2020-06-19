package com.neptunedreams.framework.task;

import java.util.concurrent.CountDownLatch;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Run an operation a single time after a preset interval, with the option of restarting the clock at any time.
 * Once the operation is performed, the timer does not restart until the restart() method is called. It may be called at 
 * any time. This was created to implement an auto-save feature, but it may be used elsewhere.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 6/19/20
 * <p>Time: 1:20 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class RestartableTimer {
  private final Thread restartableThread;

  /**
   * Construct a RestartableTimer using the specified delay time, and performing the specified operation.
   * @param triggerTimeMillis The delay before the operation is performed
   * @param operation The operation
   */
  public RestartableTimer(long triggerTimeMillis, Runnable operation) {
    restartableThread = new Thread(new Task(triggerTimeMillis, operation), "RestartableTimer Thread");
  }

  /**
   * Must be called after instantiation. This does not start the timer. The timer only starts with a call to restart().
   */
  public void start() {
    restartableThread.setDaemon(true);
    restartableThread.start();
  }

  /**
   * Start or restart the timer. If the timer is running, it gets restarted at the beginning. If it's not, it starts.
   */
  public void restart() {
    restartableThread.interrupt();
  }
  
  private static class Task implements Runnable {
    private final Runnable operation;
    private final CountDownLatch latch = new CountDownLatch(1);
    private final long triggerTimeMillis;
    
    Task(long triggerTime, Runnable operation) {
      this.operation = operation;
      triggerTimeMillis = triggerTime;
    }

    @Override
    public void run() {
      //noinspection InfiniteLoopStatement
      while (true) {
        try {
          latch.await(); // An interrupt here starts the timer.
        } catch (InterruptedException ignored) { }
        startTheTimer(); // When the timer finishes, it comes back here.
      }
    }

    private void startTheTimer() {
      while (true) {
        try {
          //noinspection BusyWait
          Thread.sleep(triggerTimeMillis); // an interrupt here restarts the timer
          operation.run();
          return;
        } catch (InterruptedException ignored) { }
      }
    }
  }
}
