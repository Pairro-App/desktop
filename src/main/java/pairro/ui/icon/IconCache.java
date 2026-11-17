package pairro.ui.icon;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.image.Image;

public final class IconCache {
  private static final Map<Integer, Image> IMAGES = new ConcurrentHashMap<>();

  public static Image get(int size) {
    Image cached = IMAGES.get(size);

    if (cached != null) {
      return cached;
    }

    try (InputStream stream = IconLoader.loadIconStream(size)) {
      if (stream == null) {
        return null;
      }

      Image image = new Image(stream);
      Image previous = IMAGES.putIfAbsent(size, image);

      return previous != null ? previous : image;
    } catch (IOException exception) {
      System.err.println("[IconCache] Could not load icon " + size + ": " + exception.getMessage());

      return null;
    }
  }

  public static List<Image> windowIcons() {
    List<Image> icons = new ArrayList<>();

    for (int size : IconLoader.getWindowIconSizes()) {
      Image image = get(size);

      if (image != null) {
        icons.add(image);
      }
    }

    return icons;
  }
}
