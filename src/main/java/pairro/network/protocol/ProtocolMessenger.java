package pairro.network.protocol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import pairro.network.Commands;
import pairro.network.server.ClientHandler;

public class ProtocolMessenger {

  public void sendPairChallenge(ClientHandler client, String myId, String myName, String publicKey) {
    Map<String, String> fields = new LinkedHashMap<>();

    fields.put(Fields.DEVICE_ID, myId);
    fields.put(Fields.DEVICE_NAME, myName);
    fields.put(Fields.PUBLIC_KEY, publicKey);

    client.send(Protocol.encode(Commands.Pair.CHALLENGE, fields));
  }

  public void sendPairSuccess(ClientHandler client, String myId, String myName, String token) {
    Map<String, String> fields = new LinkedHashMap<>();

    fields.put(Fields.DEVICE_ID, myId);
    fields.put(Fields.DEVICE_NAME, myName);
    fields.put(Fields.TOKEN, token);

    client.send(Protocol.encode(Commands.Pair.SUCCESS, fields));
  }

  public void sendAuthResult(ClientHandler client, boolean ok, String reason) {
    Map<String, String> fields = new LinkedHashMap<>();

    if (!ok && reason != null) {
      fields.put(Fields.REASON, reason);
    }

    client.send(Protocol.encode(ok ? Commands.Auth.OK : Commands.Auth.FAIL, fields));
  }

  public void sendAuthChallenge(ClientHandler client, String serverNonce, String proof) {
    Map<String, String> fields = new LinkedHashMap<>();

    fields.put(Fields.NONCE, serverNonce);
    fields.put(Fields.PROOF, proof);

    client.send(Protocol.encode(Commands.Auth.CHALLENGE, fields));
  }

  public void sendPairDecision(ClientHandler client, boolean accepted, String reason) {
    Map<String, String> fields = new LinkedHashMap<>();

    fields.put(Fields.ACCEPTED, String.valueOf(accepted));

    if (!accepted && reason != null) {
      fields.put(Fields.REASON, reason);
    }

    client.send(Protocol.encode(Commands.Pair.DECISION, fields));
  }

  public void sendDisconnect(ClientHandler client) {
    client.send(Protocol.encode(Commands.System.DISCONNECT, Collections.emptyMap()));
  }

  public void sendPong(ClientHandler client) {
    client.send(Protocol.encode(Commands.System.PONG, Collections.emptyMap()));
  }
}
