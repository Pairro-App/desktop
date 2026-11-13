package pairro.network.handler;

import pairro.audio.AudioController;
import pairro.network.Commands;
import pairro.network.server.ClientHandler;

public class AudioHandler {
  private final AudioController audioController;

  public AudioHandler(AudioController audioController) {
    this.audioController = audioController;
  }

  public boolean handleCommand(
    String data,
    ClientHandler client
  ) {
    if (data == null || data.isEmpty() || audioController == null) {
      return false;
    }

    if (Commands.Volume.GET.equals(data)) {
      sendState(client);
      return true;
    }

    if (data.startsWith(Commands.Volume.SET)) {
      handleVolumeSet(data);
      return true;
    }

    if (data.startsWith(Commands.Volume.MUTE)) {
      handleVolumeMute(data);
      return true;
    }

    return false;
  }

  private void sendState(ClientHandler client) {
    if (client == null) {
      return;
    }

    int volumePercent = audioController.getVolumePercent();

    if (volumePercent < 0) {
      return;
    }

    int mutedFlag = audioController.isMuted() ? 1 : 0;

    client.send(Commands.Volume.STATE + volumePercent + "," + mutedFlag);
  }

  private void handleVolumeSet(String data) {
    try {
      String payload = data.substring(Commands.Volume.SET.length());
      int volumePercent = Integer.parseInt(payload.trim());

      audioController.setVolumePercent(volumePercent);
    } catch (NumberFormatException exception) {
      System.err.println("[Audio] Malformed volume payload: " + data);
    }
  }

  private void handleVolumeMute(String data) {
    String payload = data.substring(Commands.Volume.MUTE.length()).trim();

    if ("1".equals(payload)) {
      audioController.mute();
    } else if ("0".equals(payload)) {
      audioController.unmute();
    } else {
      System.err.println("[Audio] Malformed mute payload: " + data);
    }
  }
}
