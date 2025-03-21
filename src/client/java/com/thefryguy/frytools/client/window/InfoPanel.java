package com.thefryguy.frytools.client.window;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class InfoPanel extends VBox {

    // Member labels
    private Label tpsValueLabel;
    private Label fpsValueLabel;
    private Label playerValueLabel;
    private Label posValueLabel;
    private Label pingValueLabel;
    private Label otherInfoValueLabel;

    private final MinecraftClient client;

    public InfoPanel() {
        super(15);
        this.client = MinecraftClient.getInstance();
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #C0C0C0; " +
                "-fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; " +
                "-fx-border-width: 2px;");
        setupUI();
        updateFromGame(); // Initial update
    }

    public static Node createInfoPanel() {
        return new InfoPanel();
    }

    private void setupUI() {
        // Title Label
        Label titleLabel = new Label("FryTools Info Panel");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // GridPane for info metrics
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 0));

        // Create and add rows with labels and initial values
        tpsValueLabel = createValueLabel();
        addInfoRow(grid, 0, "Server TPS:", tpsValueLabel);

        fpsValueLabel = createValueLabel();
        addInfoRow(grid, 1, "Client FPS:", fpsValueLabel);

        playerValueLabel = createValueLabel();
        addInfoRow(grid, 2, "Player:", playerValueLabel);

        posValueLabel = createValueLabel();
        addInfoRow(grid, 3, "Position:", posValueLabel);

        pingValueLabel = createValueLabel();
        addInfoRow(grid, 4, "Ping:", pingValueLabel);

        otherInfoValueLabel = createValueLabel();
        addInfoRow(grid, 5, "Other Info:", otherInfoValueLabel);

        // Refresh button
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> new Thread(this::updateFromGame).start());
        refreshButton.setStyle(
                "-fx-background-color: #C0C0C0; " +
                        "-fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; " +
                        "-fx-border-width: 2px; " +
                        "-fx-font-family: 'MS Sans Serif'; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 3 8 3 8;"
        );
        HBox buttonBox = new HBox(refreshButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        getChildren().addAll(titleLabel, grid, buttonBox);
    }

    private Label createValueLabel() {
        Label label = new Label("Loading...");
        label.setStyle("-fx-font-family: 'MS Sans Serif'; " +
                "-fx-font-size: 12px; " +
                "-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #808080 #FFFFFF #FFFFFF #808080; " +
                "-fx-border-width: 1px;");
        return label;
    }

    private void addInfoRow(GridPane grid, int rowIndex, String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold;");
        grid.add(label, 0, rowIndex);
        grid.add(valueLabel, 1, rowIndex);
    }

    public void updateFromGame() {
        // Run on Minecraft's main thread
        client.execute(() -> {
            String tps = getTPSFromGame();
            String fps = getFPSFromGame();
            String player = getPlayerName();
            String pos = getPlayerPosition();
            String ping = getPlayerPing();
            String otherInfo = getOtherGameInfo();

            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                updateInfo(tps, fps, player, pos, ping, otherInfo);
            });
        });
    }

    public void updateInfo(String tps, String fps, String player, String pos, String ping, String otherInfo) {
        tpsValueLabel.setText(tps);
        fpsValueLabel.setText(fps);
        playerValueLabel.setText(player);
        posValueLabel.setText(pos);
        pingValueLabel.setText(ping);
        otherInfoValueLabel.setText(otherInfo);
    }

    // Data collection methods remain the same as original working version
    private String getTPSFromGame() { return "N/A"; }
    private String getFPSFromGame() { return client.fpsDebugString.split(" ")[0] + " FPS"; }
    private String getPlayerName() { return client.player != null ? client.player.getName().getString() : "Unknown"; }
    private String getPlayerPosition() {
        if (client.player != null) {
            BlockPos pos = client.player.getBlockPos();
            return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
        }
        return "Unknown";
    }
    private String getPlayerPing() {
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler != null) {
            return networkHandler.getPlayerListEntry(client.player.getUuid()) != null
                    ? networkHandler.getPlayerListEntry(client.player.getUuid()).getLatency() + "ms"
                    : "N/A";
        }
        return "N/A";
    }
    private String getOtherGameInfo() { return "Game Time: " + client.world.getTimeOfDay() % 24000; }
}