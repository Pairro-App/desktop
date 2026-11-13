package pairro.update;

import java.io.IOException;
import java.util.Optional;

import pairro.store.SettingsStore;

public final class UpdateChecker {
  private final ReleaseProvider provider;
  private final Version currentVersion;
  private final SettingsStore settingsStore;

  public UpdateChecker(
    ReleaseProvider provider,
    Version currentVersion,
    SettingsStore settingsStore
  ) {
    this.provider = provider;
    this.currentVersion = currentVersion;
    this.settingsStore = settingsStore;
  }

  public Optional<Release> checkForUpdate() throws IOException, InterruptedException {
    if (!settingsStore.isCheckUpdate()) {
      return Optional.empty();
    }

    return provider.fetchLatest().filter(release -> release.version().isNewerThan(currentVersion));
  }
}
