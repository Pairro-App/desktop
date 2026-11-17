package pairro.device;

import java.util.UUID;
import java.util.prefs.Preferences;

public class DeviceIdentity {
  private final String KEY_DEVICE_ID = "device_id";

  private final Preferences prefs = Preferences.userNodeForPackage(DeviceIdentity.class);

  public String getId() {
    String existing = prefs.get(KEY_DEVICE_ID, null);

    if (existing != null) {
      return existing;
    }

    String newId = UUID.randomUUID().toString();
    prefs.put(KEY_DEVICE_ID, newId);

    return newId;
  }
}
