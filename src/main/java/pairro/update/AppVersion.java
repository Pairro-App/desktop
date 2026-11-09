package pairro.update;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public final class AppVersion {
  private static final String RESOURCE = "/version.properties";

  public static Optional<Version> current() {
    try (InputStream stream = AppVersion.class.getResourceAsStream(RESOURCE)) {
      if (stream == null) {
        return Optional.empty();
      }

      Properties properties = new Properties();
      properties.load(stream);

      String value = properties.getProperty("version");

      if (value == null || value.contains("${")) {
        return Optional.empty();
      }

      return Optional.of(Version.parse(value));
    } catch (IOException | IllegalArgumentException exception) {
      System.err.println("[AppVersion] Could not read version: " + exception.getMessage());
      return Optional.empty();
    }
  }
}
