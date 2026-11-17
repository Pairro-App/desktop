package pairro.pairing;

import java.util.List;

import pairro.device.Device;
import pairro.network.server.ClientHandler;

public interface DashboardEventListener {
  public void onPairRequest(
    String deviceName,
    String addressKey,
    String calculatedPin,
    ClientHandler client,
    String deviceId
  );

  public void onConnectionConflict(
    Device connectedDevice,
    String incomingDeviceName,
    String addressKey,
    String deviceId,
    ClientHandler client
  );

  public void onConnectedDeviceChanged(Device device);

  public void onDeviceListUpdated(List<Device> devices);

  public void onDataReceived(
    String data,
    String hostAddress
  );
}
