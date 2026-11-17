package pairro.network;

import pairro.network.server.ClientHandler;

public interface NetworkDataListener {
  public void onData(String data, ClientHandler client);
}
