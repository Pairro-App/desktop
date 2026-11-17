package pairro.app;

import java.util.List;
import java.util.function.Consumer;

import pairro.device.Device;
import pairro.device.DeviceDirectory;
import pairro.network.lifecycle.ConnectionLifecycleManager;
import pairro.network.server.ClientHandler;
import pairro.pairing.DashboardEventListener;
import pairro.pairing.PairingCoordinator;
import pairro.update.Release;
import pairro.update.UpdateScheduler;

public class AppCoordinator {
  private final PairingCoordinator pairing;
  private final DeviceDirectory devices;

  private ConnectionLifecycleManager connectionLifecycle;
  private DashboardEventListener eventListener;
  private Consumer<Boolean> connectionStateListener;
  private UpdateScheduler updateScheduler;
  private volatile Consumer<Release> updateListener;

  public AppCoordinator(
    PairingCoordinator pairing,
    DeviceDirectory devices
  ) {
    this.pairing = pairing;
    this.devices = devices;
  }

  public void setUpdateScheduler(UpdateScheduler updateScheduler) {
    this.updateScheduler = updateScheduler;
  }

  public void setUpdateListener(Consumer<Release> listener) {
    this.updateListener = listener;
  }

  public void startUpdateChecks() {
    if (this.updateScheduler != null) {
      this.updateScheduler.start();
    }
  }

  public void onUpdateAvailable(Release release) {
    this.updateListener.accept(release);
  }

  public void setConnectionLifecycle(ConnectionLifecycleManager connectionLifecycle) {
    this.connectionLifecycle = connectionLifecycle;

    this.connectionLifecycle.setConnectionStateListener(isConnected -> {
      this.connectionStateListener.accept(isConnected);

      if (!isConnected) {
        notifyDevicesChanged();
      }
    });
  }

  public void setEventListener(DashboardEventListener listener) {
    this.eventListener = listener;
    this.pairing.setEventListener(listener);
  }

  public void setConnectionStateListener(Consumer<Boolean> listener) {
    this.connectionStateListener = listener;
  }

  public void setConnectionTransitionListener(Consumer<Boolean> listener) {
    this.connectionLifecycle.setConnectionTransitionListener(listener);
  }

  public void renameDevice() {
    this.connectionLifecycle.rename();
  }

  public void toggleConnection() {
    this.connectionLifecycle.toggle();
  }

  public void refreshDevices() {
    this.connectionLifecycle.refreshDevices();
  }

  public void handlePairingResponse(
    ClientHandler client,
    String addressKey,
    String deviceId,
    String deviceName,
    boolean accepted
  ) {
    this.pairing.handlePairingResponse(client, addressKey, deviceId, deviceName, accepted);
  }

  public void resolveConnectionConflict(
    ClientHandler client,
    String addressKey,
    String deviceId,
    String deviceName,
    boolean keepNew
  ) {
    this.pairing.resolveConnectionConflict(client, addressKey, deviceId, deviceName, keepNew);
  }

  public void disconnectConnectedDevice() {
    this.pairing.disconnectConnectedDevice();
  }

  public void onDeviceDiscovered(
    String id,
    String name,
    String address
  ) {
    this.devices.upsertDiscovered(address, name, id);

    notifyDevicesChanged();
  }

  public void onDataReceived(
    String data,
    ClientHandler client
  ) {
    String hostAddress = null;

    if (client != null) {
      hostAddress = client.getAddress().getHostAddress();
    }

    this.eventListener.onDataReceived(data, hostAddress);
  }

  public void unpairDevice(Device device) {
    this.devices.unpair(device);

    notifyDevicesChanged();
  }

  private void notifyDevicesChanged() {
    this.eventListener.onDeviceListUpdated(this.devices.getDevicesList());
  }

  public boolean isConnected() {
    return this.connectionLifecycle.isConnected();
  }

  public Device getConnectedDevice() {
    return this.devices.getConnectedDevice();
  }

  public List<Device> getDevicesList() {
    return this.devices.getDevicesList();
  }

  public void shutdown() {
    if (this.updateScheduler != null) {
      this.updateScheduler.stop();
    }

    this.connectionLifecycle.shutdown();
  }
}
