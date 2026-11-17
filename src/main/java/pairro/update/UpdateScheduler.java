package pairro.update;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class UpdateScheduler {
  private final UpdateChecker checker;
  private final Duration initialDelay;
  private final Duration interval;
  private final Consumer<Release> listener;

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
    Thread thread = new Thread(runnable, "update-checker");
    thread.setDaemon(true);
    return thread;
  });

  private Version lastAnnounced;

  public UpdateScheduler(
    UpdateChecker checker,
    Duration initialDelay,
    Duration interval,
    Consumer<Release> listener
  ) {
    this.checker = checker;
    this.initialDelay = initialDelay;
    this.interval = interval;
    this.listener = listener;
  }

  public void start() {
    executor.scheduleWithFixedDelay(
      this::runCheck,
      initialDelay.toSeconds(),
      interval.toSeconds(),
      TimeUnit.SECONDS);
  }

  public void stop() {
    executor.shutdownNow();
  }

  private void runCheck() {
    try {
      checker.checkForUpdate()
        .filter(this::isNewlyFound)
        .ifPresent(release -> {
          lastAnnounced = release.version();
          listener.accept(release);
        });
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    } catch (Exception exception) {
      System.err.println("[UpdateScheduler] Check failed: " + exception.getMessage());
    }
  }

  private boolean isNewlyFound(Release release) {
    return lastAnnounced == null || release.version().isNewerThan(lastAnnounced);
  }
}
