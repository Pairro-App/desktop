package pairro.input;

import java.awt.AWTException;
import java.awt.Robot;

public class RobotProvider {
  private static Robot instance;

  public static synchronized Robot getInstance() {
    if (instance == null) {
      try {
        instance = new Robot();
      } catch (AWTException e) {
        throw new IllegalStateException("Could not create Robot: " + e.getMessage(), e);
      }
    }

    return instance;
  }
}
