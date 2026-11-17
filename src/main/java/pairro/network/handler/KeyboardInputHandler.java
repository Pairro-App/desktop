package pairro.network.handler;

import pairro.input.KeyboardController;
import pairro.network.Commands;

public class KeyboardInputHandler {
  private final KeyboardController keyboardController;

  public KeyboardInputHandler(KeyboardController keyboardController) {
    if (keyboardController == null) {
      throw new IllegalArgumentException("KeyboardController cannot be null");
    }

    this.keyboardController = keyboardController;
  }

  public boolean handleCommand(String data) {
    if (data == null || data.isEmpty()) {
      return false;
    }

    if (data.startsWith(Commands.Keyboard.KEY_COMBO)) {
      handleKeyCombo(data);

      return true;
    }

    return false;
  }

  private void handleKeyCombo(String data) {
    try {
      String payload = data.substring(Commands.Keyboard.KEY_COMBO.length());
      String[] parts = payload.split(",");

      int[] keyCodes = new int[parts.length];

      for (int index = 0; index < parts.length; index++) {
        keyCodes[index] = Integer.parseInt(parts[index].trim());
      }

      keyboardController.executeCombo(keyCodes);
    } catch (Exception exception) {
      System.err.println("[Network] Malformed key combo payload: " + data);
    }
  }
}
