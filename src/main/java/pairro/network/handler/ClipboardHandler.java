package pairro.network.handler;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import pairro.clipboard.ClipboardController;
import pairro.clipboard.ClipboardHistory;
import pairro.clipboard.ClipboardMonitor;
import pairro.network.Commands;
import pairro.network.security.Codec;
import pairro.network.server.ClientHandler;

public class ClipboardHandler {
  private final ClipboardController clipboardController;
  private final ClipboardHistory clipboardHistory;
  private final ClipboardMonitor clipboardMonitor;

  public ClipboardHandler(ClipboardController clipboardController) {
    this.clipboardController = clipboardController;
    this.clipboardHistory = new ClipboardHistory();
    this.clipboardMonitor = new ClipboardMonitor(clipboardController, this.clipboardHistory);
  }

  public void start() {
    this.clipboardMonitor.start();
  }

  public void stop() {
    this.clipboardMonitor.stop();
  }

  public boolean handleCommand(
    String data,
    ClientHandler client
  ) {
    if (data == null || data.isEmpty()) {
      return false;
    }

    if (Commands.Clipboard.GET.equals(data)) {
      sendState(client);

      return true;
    }

    if (Commands.Clipboard.HISTORY_GET.equals(data)) {
      this.clipboardMonitor.start();
      sendHistory(client);

      return true;
    }

    if (Commands.Clipboard.HISTORY_CLEAR.equals(data)) {
      this.clipboardHistory.clear();

      return true;
    }

    if (data.startsWith(Commands.Clipboard.SET)) {
      handleSet(data);

      return true;
    }

    if (data.startsWith(Commands.Clipboard.APPEND)) {
      handleAppend(data);

      return true;
    }

    return false;
  }

  private void sendState(ClientHandler client) {
    if (client == null) {
      return;
    }

    String text = clipboardController.getText();

    if (text == null) {
      return;
    }

    client.send(Commands.Clipboard.STATE + Codec.toBase64(text));
  }

  private void sendHistory(ClientHandler client) {
    if (client == null) {
      return;
    }

    for (ClipboardHistory.Entry entry : this.clipboardHistory.snapshot()) {
      client.send(Commands.Clipboard.ENTRY + entry.getId() + "," + entry.getTimestamp() + "," + Codec.toBase64(entry.getText()));
    }

    client.send(Commands.Clipboard.HISTORY_END);
  }

  private void handleSet(String data) {
    String text = decode(data.substring(Commands.Clipboard.SET.length()));

    if (text != null) {
      clipboardController.setText(text);
    }
  }

  private void handleAppend(String data) {
    String text = decode(data.substring(Commands.Clipboard.APPEND.length()));

    if (text != null) {
      clipboardController.appendText(text);
    }
  }

  private String decode(String payload) {
    try {
      return Codec.textFromBase64(payload);
    } catch (IllegalArgumentException exception) {
      System.err.println("[Clipboard] Malformed payload: " + payload);

      return null;
    }
  }
}
