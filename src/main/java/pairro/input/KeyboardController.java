package pairro.input;

import java.awt.Robot;

public class KeyboardController {
  private Robot robot() {
    return RobotProvider.getInstance();
  }

  public void executeKey(int keyCode) {
    executeCombo(new int[] { keyCode });
  }

  public void executeCombo(int[] keyCodes) {
    int pressedCount = 0;

    try {
      for (int keyCode : keyCodes) {
        robot().keyPress(keyCode);

        pressedCount++;
      }
    } catch (Exception e) {
      System.err.println("[KeyboardController] Failed to press combo: " + e.getMessage());
    } finally {
      for (int i = pressedCount - 1; i >= 0; i--) {
        try {
          robot().keyRelease(keyCodes[i]);
        } catch (Exception e) {
          System.err.println("[KeyboardController] Failed to release key: " + e.getMessage());
        }
      }
    }
  }
}
