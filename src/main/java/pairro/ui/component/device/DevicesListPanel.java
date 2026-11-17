package pairro.ui.component.device;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import pairro.device.Device;

public class DevicesListPanel extends VBox {
  private final VBox rowsBox = new VBox(4);
  private final VBox emptyState = new DevicesEmptyState();
  private final Consumer<Device> onSelectDevice;
  private final Consumer<Device> onUnpairDevice;
  private final Runnable onDisconnect;
  private final DeviceHeaderCard headerCard;

  private final Map<String, DeviceRow> rowCache = new HashMap<>();
  private final Map<String, String> rowSignatures = new HashMap<>();
  private String searchQuery = "";

  private List<Device> lastDevices = List.of();
  private Device connectedDevice;
  private Instant connectedSince;

  public DevicesListPanel(
    String deviceName,
    boolean connected,
    List<Device> devices,
    Consumer<Device> onSelectDevice,
    Consumer<Device> onUnpairDevice,
    Runnable onToggleConnection,
    Runnable onRefresh,
    Runnable onDisconnect,
    Runnable onOpenSettings
  ) {
    super(0);
    this.onSelectDevice = onSelectDevice;
    this.onUnpairDevice = onUnpairDevice;
    this.onDisconnect = onDisconnect;

    getStyleClass().add("devices-panel");
    setPrefWidth(260);

    this.headerCard = new DeviceHeaderCard(deviceName, connected, onToggleConnection, onRefresh, onOpenSettings);
    DeviceSearchBar searchBar = new DeviceSearchBar(this::filter);

    VBox heroArea = new VBox(12, headerCard, searchBar);
    heroArea.getStyleClass().add("devices-hero");

    Label caption = new Label("DEVICES");
    caption.getStyleClass().add("header-card-caption");

    HBox captionBar = new HBox(caption);
    captionBar.setAlignment(Pos.CENTER_LEFT);
    captionBar.getStyleClass().add("sheet-caption-bar");

    ScrollPane scrollPane = createScrollPane();
    VBox.setVgrow(scrollPane, Priority.ALWAYS);

    VBox body = new VBox(0, scrollPane);
    body.getStyleClass().add("devices-sheet-body");
    VBox.setVgrow(body, Priority.ALWAYS);

    VBox sheet = new VBox(0, captionBar, body);
    sheet.getStyleClass().add("devices-sheet");
    VBox.setVgrow(sheet, Priority.ALWAYS);

    getChildren().addAll(heroArea, sheet);
    setDevices(devices);
  }

  public void setDevices(List<Device> devices) {
    lastDevices = devices == null ? List.of() : devices;

    List<Device> paired = lastDevices.stream()
      .filter(Device::isPaired)
      .sorted(Comparator.comparing(d -> !isConnected(d)))
      .toList();
    List<Device> unpaired = lastDevices.stream().filter(d -> !d.isPaired()).toList();

    if (paired.isEmpty() && unpaired.isEmpty()) {
      rowCache.clear();
      rowSignatures.clear();

      rowsBox.setAlignment(Pos.CENTER);

      VBox.setVgrow(rowsBox, Priority.ALWAYS);

      rowsBox.getChildren().setAll(emptyState);

      return;
    }

    rowsBox.setAlignment(Pos.TOP_LEFT);
    rowsBox.setPadding(new Insets(0, 0, 16, 0));

    List<Node> nodes = new ArrayList<>();
    Map<String, DeviceRow> nextCache = new HashMap<>();
    Map<String, String> nextSignatures = new HashMap<>();

    for (Device device : paired) {
      nodes.add(rowFor(device, nextCache, nextSignatures));
    }

    if (!paired.isEmpty() && !unpaired.isEmpty()) {
      Region gap = new Region();
      gap.setMinHeight(8);
      gap.setPrefHeight(8);
      gap.setMaxHeight(8);
      nodes.add(gap);
    }

    for (Device device : unpaired) {
      nodes.add(rowFor(device, nextCache, nextSignatures));
    }

    rowCache.clear();
    rowCache.putAll(nextCache);
    rowSignatures.clear();
    rowSignatures.putAll(nextSignatures);

    rowsBox.getChildren().setAll(nodes);
    filter(searchQuery);
  }

  private DeviceRow rowFor(
    Device device,
    Map<String, DeviceRow> nextCache,
    Map<String, String> nextSignatures
  ) {
    String key = device.getId();
    String signature = signatureOf(device);
    DeviceRow row = rowCache.get(key);

    if (row == null || !signature.equals(rowSignatures.get(key))) {
      row = createRow(device);
    }

    nextCache.put(key, row);
    nextSignatures.put(key, signature);

    return row;
  }

  private String signatureOf(Device device) {
    return device.getName() + '|' + device.isPaired() + '|' + device.getStatus() + '|'
      + (isConnected(device) ? connectedSince : "-");
  }

  public void setDeviceName(String deviceName) {
    headerCard.setDeviceName(deviceName);
  }

  public void setConnected(boolean connected) {
    headerCard.setConnected(connected);
  }

  public void setShuttingDown(boolean shuttingDown) {
    headerCard.setShuttingDown(shuttingDown);
  }

  public void setConnectedDevice(Device device) {
    if (device == null) {
      connectedDevice = null;
      connectedSince = null;
    } else if (connectedDevice == null || !Objects.equals(connectedDevice.getName(), device.getName())) {
      connectedSince = Instant.now();
      connectedDevice = device;
    } else {
      connectedDevice = device;
    }

    setDevices(lastDevices);
  }

  private DeviceRow createRow(Device device) {
    Instant since = isConnected(device) ? connectedSince : null;

    return new DeviceRow(device, since, onSelectDevice, onUnpairDevice, onDisconnect);
  }

  private boolean isConnected(Device device) {
    return connectedDevice != null && Objects.equals(connectedDevice.getName(), device.getName());
  }

  private ScrollPane createScrollPane() {
    ScrollPane scrollPane = new ScrollPane(rowsBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.getStyleClass().add("groups-scroll");

    return scrollPane;
  }

  private void filter(String query) {
    String q = query == null ? "" : query.trim().toLowerCase();
    searchQuery = q;

    for (Node node : rowsBox.getChildren()) {
      if (node instanceof DeviceRow row) {
        boolean matches = q.isEmpty() || row.getDevice().getName().toLowerCase().contains(q);

        node.setVisible(matches);
        node.setManaged(matches);
      }
    }
  }
}
