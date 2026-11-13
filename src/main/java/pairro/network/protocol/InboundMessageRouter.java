package pairro.network.protocol;

import pairro.network.Commands;
import pairro.network.NetworkDataListener;
import pairro.network.NetworkPairingListener;
import pairro.network.handler.AudioHandler;
import pairro.network.handler.ClipboardHandler;
import pairro.network.handler.KeyboardInputHandler;
import pairro.network.handler.MouseInputHandler;
import pairro.network.server.ClientHandler;

public class InboundMessageRouter {
  private final MouseInputHandler mouseInputHandler;
  private final KeyboardInputHandler keyboardInputHandler;
  private final AudioHandler audioHandler;
  private final ClipboardHandler clipboardHandler;
  private final ProtocolMessenger messenger;

  private NetworkPairingListener pairingListener;
  private NetworkDataListener dataListener;

  public InboundMessageRouter(
      MouseInputHandler mouseInputHandler,
      KeyboardInputHandler keyboardInputHandler,
      AudioHandler audioHandler,
      ClipboardHandler clipboardHandler,
      ProtocolMessenger messenger) {
    this.mouseInputHandler = mouseInputHandler;
    this.keyboardInputHandler = keyboardInputHandler;
    this.audioHandler = audioHandler;
    this.clipboardHandler = clipboardHandler;
    this.messenger = messenger;
  }

  public void setPairingListener(NetworkPairingListener listener) {
    this.pairingListener = listener;
  }

  public void setDataListener(NetworkDataListener listener) {
    this.dataListener = listener;
  }

  public void route(String data, ClientHandler client) {
    if (data == null || data.isEmpty()) {
      return;
    }

    if (client != null && !client.isAuthenticated()) {
      routeUnauthenticated(data, client);

      return;
    }

    if (mouseInputHandler.handleCommand(data)) {
      return;
    }

    if (keyboardInputHandler.handleCommand(data)) {
      return;
    }

    if (audioHandler.handleCommand(data, client)) {
      return;
    }

    if (clipboardHandler.handleCommand(data, client)) {
      return;
    }

    Protocol.Envelope message = Protocol.decode(data);

    switch (message.type()) {
      case Commands.Pair.REQUEST: {
        routePairRequest(message, client);
        break;
      }
      case Commands.System.PING: {
        routePing(client);
        break;
      }
      default: {
        routeApplicationData(data, client);
        break;
      }
    }
  }

  private void routeUnauthenticated(String data, ClientHandler client) {
    Protocol.Envelope message = Protocol.decode(data);

    switch (message.type()) {
      case Commands.Pair.REQUEST: {
        routePairRequest(message, client);
        break;
      }
      case Commands.Auth.INIT: {
        routeAuthInit(message, client);
        break;
      }
      case Commands.Auth.PROOF: {
        routeAuthProof(message, client);
        break;
      }
      case Commands.System.PING: {
        routePing(client);
        break;
      }
      default: {
        System.err.println("[Security] Closing unauthenticated connection from "
            + client.getAddress().getHostAddress() + ": unexpected message");

        client.close();
        break;
      }
    }
  }

  private void routePing(ClientHandler client) {
    if (client != null && messenger != null) {
      messenger.sendPong(client);
    }
  }

  private void routePairRequest(Protocol.Envelope message, ClientHandler client) {
    if (pairingListener != null) {
      pairingListener.onPairRequest(
          message.fields().get(Fields.DEVICE_ID),
          message.fields().get(Fields.DEVICE_NAME),
          message.fields().get(Fields.PUBLIC_KEY),
          client);
    }
  }

  private void routeAuthInit(Protocol.Envelope message, ClientHandler client) {
    if (pairingListener != null) {
      pairingListener.onAuthInit(
          message.fields().get(Fields.DEVICE_ID),
          message.fields().get(Fields.NONCE),
          client);
    }
  }

  private void routeAuthProof(Protocol.Envelope message, ClientHandler client) {
    if (pairingListener != null) {
      pairingListener.onAuthProof(message.fields().get(Fields.PROOF), client);
    }
  }

  private void routeApplicationData(String data, ClientHandler client) {
    if (dataListener != null) {
      dataListener.onData(data, client);
    }
  }
}
