package pairro.network.handler;

import pairro.input.MouseController;
import pairro.network.Commands;

public class MouseInputHandler {
  private final MouseController mouseController;

  public MouseInputHandler(MouseController mouseController) {
    if (mouseController == null) {
      throw new IllegalArgumentException("MouseController cannot be null");
    }

    this.mouseController = mouseController;
  }

  public boolean handleCommand(String data) {
    if (data == null || data.isEmpty()) {
      return false;
    }

    if (data.startsWith(Commands.Mouse.MOVE)) {
      handleMouseMove(data);
      return true;
    }

    if (Commands.Mouse.LEFT_CLICK.equals(data)) {
      mouseController.leftClick();

      return true;
    }

    if (Commands.Mouse.RIGHT_CLICK.equals(data)) {
      mouseController.rightClick();

      return true;
    }

    if (Commands.Mouse.LEFT_DOWN.equals(data)) {
      mouseController.leftButtonDown();

      return true;
    }

    if (Commands.Mouse.LEFT_UP.equals(data)) {
      mouseController.leftButtonUp();

      return true;
    }

    return false;
  }

  private void handleMouseMove(String data) {
    try {
      String payload = data.substring(Commands.Mouse.MOVE.length());
      String[] parts = payload.split(",", 2);

      int x = Integer.parseInt(parts[0].trim());
      int y = Integer.parseInt(parts[1].trim());

      mouseController.moveRelative(x, y);
    } catch (Exception e) {
      System.err.println("[Network] Malformed mouse move payload: " + data);
    }
  }

  public void releaseIfHeld() {
    if (mouseController != null) {
      mouseController.releaseIfHeld();
    }
  }
}
