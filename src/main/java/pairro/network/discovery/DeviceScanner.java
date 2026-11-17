package pairro.network.discovery;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import pairro.network.protocol.Fields;

public class DeviceScanner {
  private static final long SCAN_INTERVAL_SECONDS = 10;
  private static final long LIST_TIMEOUT_MS = 2000;

  private final Object lock = new Object();
  private JmDNS jmdns;
  private ServiceListener listener;
  private ScheduledExecutorService scheduler;
  private final String deviceId;
  private final String serviceType = "_pairro._tcp.local.";
  private final DeviceDiscoveredListener onDeviceDiscovered;

  public DeviceScanner(
    DeviceDiscoveredListener onDeviceDiscovered,
    String deviceId
  ) {
    this.onDeviceDiscovered = onDeviceDiscovered;
    this.deviceId = deviceId;
  }

  public void start() {
    synchronized (lock) {
      if (this.scheduler != null) {
        return;
      }

      this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "device-scanner");
        thread.setDaemon(true);

        return thread;
      });

      this.scheduler.scheduleWithFixedDelay(this::scan, 0, SCAN_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
  }

  private void scan() {
    JmDNS instance;

    try {
      instance = ensureJmDns();
    } catch (IOException exception) {
      exception.printStackTrace();

      return;
    }

    try {
      for (ServiceInfo info : instance.list(serviceType, LIST_TIMEOUT_MS)) {
        report(info);
      }
    } catch (RuntimeException exception) {
      System.err.println("[DeviceScanner] Scan failed: " + exception.getMessage());
    }
  }

  private JmDNS ensureJmDns() throws IOException {
    synchronized (lock) {
      if (this.jmdns != null) {
        return this.jmdns;
      }

      JmDNS created = JmDNS.create(InetAddress.getLocalHost());

      ServiceListener serviceListener = new ServiceListener() {
        @Override
        public void serviceAdded(ServiceEvent event) {
          created.requestServiceInfo(event.getType(), event.getName());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
          report(event.getInfo());
        }
      };

      created.addServiceListener(serviceType, serviceListener);

      this.jmdns = created;
      this.listener = serviceListener;

      return created;
    }
  }

  private void report(ServiceInfo info) {
    if (info == null) {
      return;
    }

    String[] addresses = info.getHostAddresses();

    if (addresses == null || addresses.length == 0) {
      return;
    }

    String address = addresses[0];
    String id = info.getPropertyString(Fields.DEVICE_ID);
    String type = info.getPropertyString(Fields.DEVICE_TYPE);

    if (!isIPv4(address) || id == null || "desktop".equals(type) || id.equals(deviceId)) {
      return;
    }

    onDeviceDiscovered.onDiscovered(id, info.getName(), address);
  }

  private void closeJmDns() {
    JmDNS instance;
    ServiceListener serviceListener;

    synchronized (lock) {
      instance = this.jmdns;
      serviceListener = this.listener;
      this.jmdns = null;
      this.listener = null;
    }

    if (instance == null) {
      return;
    }

    if (serviceListener != null) {
      instance.removeServiceListener(serviceType, serviceListener);
    }

    try {
      instance.close();
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  public void stop() {
    synchronized (lock) {
      if (this.scheduler != null) {
        this.scheduler.shutdownNow();
        this.scheduler = null;
      }
    }

    closeJmDns();
  }

  private boolean isIPv4(String address) {
    try {
      InetAddress inetAddress = InetAddress.getByName(address);

      if (inetAddress instanceof Inet4Address) {
        return true;
      }
    } catch (Exception exception) {

    }

    return false;
  }
}
