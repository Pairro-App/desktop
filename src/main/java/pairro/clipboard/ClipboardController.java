package pairro.clipboard;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ClipboardController {
  private Clipboard clipboard;
  private boolean initialized;

  private synchronized Clipboard clipboard() {
    if (!initialized) {
      initialized = true;
      clipboard = initializeClipboard();
    }

    return clipboard;
  }

  private Clipboard initializeClipboard() {
    try {
      return Toolkit.getDefaultToolkit().getSystemClipboard();
    } catch (HeadlessException exception) {
      System.err.println("[ClipboardController] Failed to access system clipboard: " + exception.getMessage());

      return null;
    }
  }

  public String getText() {
    return readText(true);
  }

  public String peekText() {
    return readText(false);
  }

  private String readText(boolean logErrors) {
    if (clipboard() == null) {
      return null;
    }

    try {
      if (!clipboard().isDataFlavorAvailable(DataFlavor.stringFlavor)) {
        return null;
      }

      Object content = clipboard().getData(DataFlavor.stringFlavor);

      return content instanceof String ? (String) content : null;
    } catch (UnsupportedFlavorException | IOException | IllegalStateException exception) {
      if (logErrors) {
        System.err.println("[ClipboardController] Failed to read clipboard: " + exception.getMessage());
      }

      return null;
    }
  }

  public void setText(String text) {
    if (clipboard() == null) {
      return;
    }

    try {
      clipboard().setContents(new StringSelection(text), null);
    } catch (IllegalStateException exception) {
      System.err.println("[ClipboardController] Failed to write clipboard: " + exception.getMessage());
    }
  }

  public void appendText(String text) {
    String existingText = getText();
    String combinedText = (existingText == null || existingText.isEmpty())
      ? text
      : existingText + "\n" + text;

    setText(combinedText);
  }
}
