package pairro.ui.view;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import pairro.app.AppCoordinator;
import pairro.device.Device;
import pairro.network.server.ClientHandler;
import pairro.pairing.DashboardEventListener;
import pairro.store.SettingsStore;
import pairro.ui.component.device.DevicesListPanel;
import pairro.ui.component.information.InformationPanel;
import pairro.ui.component.pairing.PairingBanner;
import pairro.ui.component.settings.SettingsPanel;
import pairro.ui.component.update.UpdateBanner;

public class DashboardView implements DashboardEventListener {
  private static final double SHEET_OFFSET = 700;
  private static final double SETTINGS_OFFSET = 470;

  private final VBox rootContainer;
  private final StackPane mainContent;
  private final Region scrim = new Region();
  private ParallelTransition sheetAnimation;
  private TranslateTransition settingsAnimation;
  private SettingsPanel settingsPanel;
  private final PairingBanner pairingBanner = new PairingBanner();
  private final UpdateBanner updateBanner = new UpdateBanner();

  private DevicesListPanel devicesListPanel;
  private InformationPanel informationPanel;
  private final AppCoordinator controller;
  private final SettingsStore settingsStore;

  public DashboardView(
    Stage ownerStage,
    AppCoordinator controller,
    SettingsStore settingsStore
  ) {
    this.controller = controller;
    this.settingsStore = settingsStore;

    this.controller.setConnectionStateListener(this::onConnectionStateChanged);
    this.controller.setConnectionTransitionListener(this::onConnectionTransitionStarted);

    initPanels();

    scrim.getStyleClass().add("sheet-scrim");
    scrim.setVisible(false);
    scrim.setOnMouseClicked(e -> closeInformationPanel());

    mainContent = new StackPane(devicesListPanel, scrim);
    mainContent.getStyleClass().add("dashboard-main");

    Rectangle clip = new Rectangle();
    clip.widthProperty().bind(mainContent.widthProperty());
    clip.heightProperty().bind(mainContent.heightProperty());
    mainContent.setClip(clip);

    rootContainer = new VBox(mainContent, pairingBanner, updateBanner);
    VBox.setVgrow(mainContent, Priority.ALWAYS);
    VBox.setMargin(pairingBanner, new Insets(0, 12, 12, 12));
    VBox.setMargin(updateBanner, new Insets(0, 12, 12, 12));
  }

  public void showUpdateAvailable(
    String message,
    Runnable onDownload
  ) {
    Platform.runLater(() -> updateBanner.show(message, onDownload));
  }

  private void initPanels() {
    devicesListPanel = new DevicesListPanel(
      settingsStore.getDeviceName(),
      controller.isConnected(),
      controller.getDevicesList(),
      this::onDeviceSelected,
      controller::unpairDevice,
      this::handleToggleConnection,
      controller::refreshDevices,
      controller::disconnectConnectedDevice,
      this::openSettings
    );

    devicesListPanel.setConnectedDevice(controller.getConnectedDevice());
  }

  private void onDeviceSelected(Device device) {
    ensureInformationPanel();
    informationPanel.showDevice(device);

    if (!informationPanel.isVisible()) {
      openSheet();
    }
  }

  private void ensureInformationPanel() {
    if (informationPanel != null) {
      return;
    }

    informationPanel = new InformationPanel(this::closeInformationPanel);
    informationPanel.setVisible(false);

    mainContent.getChildren().add(2, informationPanel);
    StackPane.setAlignment(informationPanel, Pos.BOTTOM_CENTER);
  }

  private void ensureSettingsPanel() {
    if (settingsPanel != null) {
      return;
    }

    settingsPanel = new SettingsPanel(this::closeSettings, settingsStore, this::onDeviceNameChanged);
    settingsPanel.setVisible(false);

    mainContent.getChildren().add(settingsPanel);
  }

  private static void enableAnimationCache(Node node) {
    node.setCache(true);
    node.setCacheHint(CacheHint.SPEED);
  }

  private static void disableAnimationCache(Node node) {
    node.setCache(false);
    node.setCacheHint(CacheHint.DEFAULT);
  }

  public void releaseTransientViews() {
    stopSheetAnimation();
    stopSettingsAnimation();

    scrim.setVisible(false);
    scrim.setOpacity(0);

    if (informationPanel != null) {
      mainContent.getChildren().remove(informationPanel);
      informationPanel = null;
    }

    if (settingsPanel != null) {
      mainContent.getChildren().remove(settingsPanel);
      settingsPanel = null;
    }
  }

  private void openSheet() {
    stopSheetAnimation();

    scrim.setOpacity(0);
    scrim.setVisible(true);
    informationPanel.setTranslateY(SHEET_OFFSET);
    informationPanel.setVisible(true);
    enableAnimationCache(informationPanel);

    TranslateTransition slide = new TranslateTransition(Duration.millis(240), informationPanel);
    slide.setToY(0);
    slide.setInterpolator(Interpolator.EASE_OUT);

    FadeTransition fade = new FadeTransition(Duration.millis(240), scrim);
    fade.setToValue(1);

    InformationPanel opening = informationPanel;
    sheetAnimation = new ParallelTransition(slide, fade);
    sheetAnimation.setOnFinished(e -> disableAnimationCache(opening));
    sheetAnimation.play();
  }

  private void closeInformationPanel() {
    if (informationPanel == null || !informationPanel.isVisible()) {
      return;
    }

    stopSheetAnimation();

    InformationPanel closing = informationPanel;
    enableAnimationCache(closing);

    TranslateTransition slide = new TranslateTransition(Duration.millis(190), informationPanel);
    slide.setToY(SHEET_OFFSET);
    slide.setInterpolator(Interpolator.EASE_IN);

    FadeTransition fade = new FadeTransition(Duration.millis(190), scrim);
    fade.setToValue(0);

    sheetAnimation = new ParallelTransition(slide, fade);
    sheetAnimation.setOnFinished(e -> {
      scrim.setVisible(false);
      closing.clear();
      disableAnimationCache(closing);
      mainContent.getChildren().remove(closing);

      if (informationPanel == closing) {
        informationPanel = null;
      }
    });
    sheetAnimation.play();
  }

  private void stopSheetAnimation() {
    if (sheetAnimation != null) {
      sheetAnimation.stop();
      sheetAnimation = null;
    }
  }

  private void openSettings() {
    stopSettingsAnimation();
    ensureSettingsPanel();

    settingsPanel.setTranslateX(SETTINGS_OFFSET);
    settingsPanel.setVisible(true);

    settingsAnimation = new TranslateTransition(Duration.millis(240), settingsPanel);
    settingsAnimation.setToX(0);
    settingsAnimation.setInterpolator(Interpolator.EASE_OUT);
    settingsAnimation.play();
  }


  private void closeSettings() {
    if (settingsPanel == null || !settingsPanel.isVisible()) {
      return;
    }

    stopSettingsAnimation();

    SettingsPanel closing = settingsPanel;

    settingsAnimation = new TranslateTransition(Duration.millis(190), closing);
    settingsAnimation.setToX(SETTINGS_OFFSET);
    settingsAnimation.setInterpolator(Interpolator.EASE_IN);
    settingsAnimation.setOnFinished(e -> {
      mainContent.getChildren().remove(closing);

      if (settingsPanel == closing) {
        settingsPanel = null;
      }
    });
    settingsAnimation.play();
  }

  private void stopSettingsAnimation() {
    if (settingsAnimation != null) {
      settingsAnimation.stop();
      settingsAnimation = null;
    }
  }

  private void onDeviceNameChanged(String deviceName) {
    devicesListPanel.setDeviceName(deviceName);
    controller.renameDevice();
  }

  private void handleToggleConnection() {
    controller.toggleConnection();
  }

  private void onConnectionTransitionStarted(boolean goingOnline) {
    if (!goingOnline) {
      devicesListPanel.setShuttingDown(true);
    }
  }

  private void onConnectionStateChanged(boolean nowConnected) {
    devicesListPanel.setConnected(nowConnected);

    if (!nowConnected) {
      closeInformationPanel();
    }
  }

  @Override
  public void onPairRequest(
    String deviceName,
    String addressKey,
    String calculatedPin,
    ClientHandler client,
    String deviceId
  ) {
    Platform.runLater(() -> {
      pairingBanner.showRequest(deviceName, addressKey, calculatedPin, (address, accepted) -> {
        controller.handlePairingResponse(client, address, deviceId, deviceName, accepted);
      });
    });
  }

  @Override
  public void onConnectionConflict(
    Device connectedDevice,
    String incomingDeviceName,
    String addressKey,
    String deviceId,
    ClientHandler client
  ) {
    Platform.runLater(() -> {
      pairingBanner.showConnectionConflict(connectedDevice.getName(), incomingDeviceName, keepNew -> {
        controller.resolveConnectionConflict(client, addressKey, deviceId, incomingDeviceName, keepNew);
      });
    });
  }

  @Override
  public void onConnectedDeviceChanged(Device device) {
    Platform.runLater(() -> {
      devicesListPanel.setConnectedDevice(device);
    });
  }

  @Override
  public void onDeviceListUpdated(java.util.List<Device> devices) {
    Platform.runLater(() -> {
      devicesListPanel.setDevices(devices);
    });
  }

  @Override
  public void onDataReceived(String data, String hostAddress) {
    Platform.runLater(() -> {
      System.out.println("[Dashboard] Data from " + hostAddress + ": " + data);
    });
  }

  public VBox getRoot() {
    return rootContainer;
  }
}
