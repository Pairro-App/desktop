package pairro.network;

import pairro.network.server.ClientHandler;

public interface NetworkPairingListener {
  public void onPairRequest(
    String deviceId,
    String deviceName,
    String publicKey,
    ClientHandler client
  );

  public void onAuthInit(
    String deviceId,
    String clientNonce,
    ClientHandler client
  );

  public void onAuthProof(
    String proof,
    ClientHandler client
  );

  public void onDisconnect(ClientHandler client);
}
