package pairro.app;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class MemoryTrimmer {
  private static final long DELAY_SECONDS = 3;

  private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(runnable -> {
    Thread thread = new Thread(runnable, "memory-trimmer");
    thread.setDaemon(true);

    return thread;
  });

  private static ScheduledFuture<?> pending;

  public static synchronized void scheduleTrim() {
    cancel();

    pending = EXECUTOR.schedule(System::gc, DELAY_SECONDS, TimeUnit.SECONDS);
  }

  public static synchronized void cancel() {
    if (pending != null) {
      pending.cancel(false);
      pending = null;
    }
  }
}
