package pairro.notification.toast;

import javafx.scene.image.Image;
import pairro.ui.icon.IconCache;
import pairro.ui.icon.IconLoader;

public final class ToastIcon {
  private static final int TARGET_SIZE = 64;

  public static Image load() {
    return IconCache.get(IconLoader.findBestSize(TARGET_SIZE));
  }
}
