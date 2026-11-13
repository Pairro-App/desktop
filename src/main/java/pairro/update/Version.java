package pairro.update;

import java.util.Arrays;

public final class Version implements Comparable<Version> {
  private final String text;
  private final int[] parts;

  private Version(
    String text,
    int[] parts
  ) {
    this.text = text;
    this.parts = parts;
  }

  public static Version parse(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("Version must not be blank");
    }

    String core = raw.trim().replaceFirst("^[vV]", "").split("[-+]")[0];
    String[] split = core.split("\\.");
    int[] parts = new int[split.length];

    try {
      for (int i = 0; i < split.length; i++) {
        parts[i] = Integer.parseInt(split[i]);
      }
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Not a numeric version: " + raw, exception);
    }

    return new Version(core, parts);
  }

  public boolean isNewerThan(Version other) {
    return compareTo(other) > 0;
  }

  @Override
  public int compareTo(Version other) {
    int length = Math.max(parts.length, other.parts.length);

    for (int i = 0; i < length; i++) {
      int a = i < parts.length ? parts[i] : 0;
      int b = i < other.parts.length ? other.parts[i] : 0;

      if (a != b) {
        return Integer.compare(a, b);
      }
    }

    return 0;
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof Version other && compareTo(other) == 0;
  }

  @Override
  public int hashCode() {
    int end = parts.length;

    while (end > 1 && parts[end - 1] == 0) {
      end--;
    }

    return Arrays.hashCode(Arrays.copyOf(parts, end));
  }

  @Override
  public String toString() {
    return text;
  }
}
