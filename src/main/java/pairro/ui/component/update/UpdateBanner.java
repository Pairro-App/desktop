package pairro.ui.component.update;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import pairro.ui.component.common.ComponentFactory;
import pairro.ui.icon.IconPaths;

public class UpdateBanner extends HBox {

  public UpdateBanner() {
    super(12);
    setAlignment(Pos.CENTER_LEFT);
    getStyleClass().add("pairing-banner");
    hide();
  }

  public void show(String message, Runnable onDownload) {
    Label titleLabel = new Label(message);
    titleLabel.getStyleClass().add("pairing-banner-title");

    HBox infoBox = new HBox(10, titleLabel);
    infoBox.setAlignment(Pos.CENTER_LEFT);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Button downloadBtn = new Button("Download");
    downloadBtn.getStyleClass().addAll(
        "pairing-banner-button",
        "pairing-banner-accept",
        "update-banner-download");
    downloadBtn.setOnAction(e -> {
      if (onDownload != null) {
        onDownload.run();
      }
      hide();
    });

    Button dismissBtn = ComponentFactory.createIconButton(IconPaths.CLOSE,
        "pairing-banner-button pairing-banner-decline");
    ComponentFactory.setTooltip(dismissBtn, "Dismiss");
    dismissBtn.setOnAction(e -> hide());

    HBox actions = new HBox(8, downloadBtn, dismissBtn);
    actions.setAlignment(Pos.CENTER_RIGHT);

    getChildren().setAll(infoBox, spacer, actions);
    setVisible(true);
    setManaged(true);
  }

  public void hide() {
    setVisible(false);
    setManaged(false);
    getChildren().clear();
  }
}
