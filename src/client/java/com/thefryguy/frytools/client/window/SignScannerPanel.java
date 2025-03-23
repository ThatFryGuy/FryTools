package com.thefryguy.frytools.client.window;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SignScannerPanel extends VBox {
    private final MinecraftClient client;
    private GridPane signGrid;
    private Map<BlockPos, String[]> signDataCache = new HashMap<>();

    public SignScannerPanel() {
        super(10);
        this.client = MinecraftClient.getInstance();
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #C0C0C0;");
        initializeComponents();
    }

    public static Node createSignScannerPanel() {
        return new SignScannerPanel();
    }

    private void initializeComponents() {
        Label titleLabel = new Label("Sign Scanner");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #000080;");

        signGrid = new GridPane();
        signGrid.setHgap(10);
        signGrid.setVgap(5);
        signGrid.setPadding(new Insets(5, 10, 5, 10));

        addGridHeader(signGrid, "Position", "Content");

        // Button Panel with Windows 98 style
        HBox buttonPanel = new HBox(10);
        buttonPanel.setPadding(new Insets(5, 0, 5, 0));

        Button refreshButton = createStyledButton("Refresh", this::replaceSignData);
        Button saveButton = createStyledButton("Save CSV", this::saveToCSV);

        buttonPanel.getChildren().addAll(refreshButton, saveButton);

        ScrollPane scrollPane = new ScrollPane(signGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(200);
        scrollPane.setStyle("-fx-background: #C0C0C0;");

        getChildren().addAll(
                titleLabel,
                scrollPane,
                buttonPanel
        );

        replaceSignData();
    }

    private Button createStyledButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #C0C0C0; " +
                "-fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; " +
                "-fx-border-width: 2px; " +
                "-fx-font-family: 'MS Sans Serif'; " +
                "-fx-font-size: 12px; " +
                "-fx-padding: 3 8 3 8;");
        button.setOnAction(e -> action.run());
        return button;
    }

    private void addGridHeader(GridPane grid, String... headers) {
        for (int i = 0; i < headers.length; i++) {
            Label header = new Label(headers[i]);
            header.setStyle("-fx-font-weight: bold; -fx-background-color: #000080; -fx-text-fill: white;");
            grid.add(header, i, 0);
        }
    }

    private void replaceSignData() {
        if (client.world == null || client.player == null) return;

        client.execute(() -> {
            Map<BlockPos, String[]> newData = new HashMap<>();
            BlockPos playerPos = client.player.getBlockPos();
            int radius = 75;

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = playerPos.add(x, y, z);
                        BlockState state = client.world.getBlockState(pos);

                        if (!isSign(state)) continue;

                        BlockEntity blockEntity = client.world.getBlockEntity(pos);
                        if (blockEntity instanceof SignBlockEntity sign) {
                            String[] lines = new String[4];
                            for (int i = 0; i < 4; i++) {
                                lines[i] = sign.getFrontText().getMessage(i, false).getString()
                                        .replace(";", ":") // Sanitize CSV delimiters
                                        .replace("\n", " "); // Remove newlines
                            }
                            newData.put(pos, lines);
                        }
                    }
                }
            }

            this.signDataCache = newData;

            Platform.runLater(() -> {
                clearGrid(signGrid);
                int row = 1;
                for (Map.Entry<BlockPos, String[]> entry : signDataCache.entrySet()) {
                    BlockPos pos = entry.getKey();
                    String positionText = String.format("(%d, %d, %d)",
                            pos.getX(), pos.getY(), pos.getZ());

                    String content = String.join(" ", Arrays.stream(entry.getValue())
                            .filter(line -> !line.isEmpty())
                            .toList());

                    if (content.isEmpty()) content = "[Empty Sign]";

                    signGrid.addRow(row++,
                            createDataCell(positionText),
                            createDataCell(content)
                    );
                }
            });
        });
    }

    private void saveToCSV() {
        TextInputDialog locationDialog = new TextInputDialog("My Location");
        locationDialog.setTitle("Location Name");
        locationDialog.setHeaderText("Enter a name for this scan location:");
        locationDialog.setContentText("Name:");

        Optional<String> locationResult = locationDialog.showAndWait();
        locationResult.ifPresent(location -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Sign Data");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    // Write CSV header
                    writer.write("Location;X;Y;Z;Line1;Line2;Line3;Line4");
                    writer.newLine();

                    // Write data rows
                    for (Map.Entry<BlockPos, String[]> entry : signDataCache.entrySet()) {
                        BlockPos pos = entry.getKey();
                        String[] lines = entry.getValue();
                        writer.write(String.format("%s;%d;%d;%d;%s;%s;%s;%s",
                                location,
                                pos.getX(), pos.getY(), pos.getZ(),
                                lines[0], lines[1], lines[2], lines[3]));
                        writer.newLine();
                    }

                    // Show confirmation
                    Platform.runLater(() ->
                            new Alert(Alert.AlertType.INFORMATION,
                                    "Saved " + signDataCache.size() + " signs to:\n" + file.getAbsolutePath())
                                    .showAndWait()
                    );
                } catch (IOException e) {
                    Platform.runLater(() ->
                            new Alert(Alert.AlertType.ERROR,
                                    "Failed to save file: " + e.getMessage())
                                    .showAndWait()
                    );
                }
            }
        });
    }

    private Label createDataCell(String text) {
        Label cell = new Label(text);
        cell.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #808080 #FFFFFF #FFFFFF #808080; " +
                "-fx-border-width: 1px; " +
                "-fx-padding: 2px 4px;");
        return cell;
    }

    private void clearGrid(GridPane grid) {
        grid.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null &&
                        GridPane.getRowIndex(node) >= 1
        );
    }

    private boolean isSign(BlockState state) {
        return state.getBlock() == Blocks.OAK_SIGN ||
                state.getBlock() == Blocks.SPRUCE_SIGN ||
                state.getBlock() == Blocks.BIRCH_SIGN ||
                state.getBlock() == Blocks.JUNGLE_SIGN ||
                state.getBlock() == Blocks.ACACIA_SIGN ||
                state.getBlock() == Blocks.DARK_OAK_SIGN ||
                state.getBlock() == Blocks.OAK_WALL_SIGN ||
                state.getBlock() == Blocks.SPRUCE_WALL_SIGN ||
                state.getBlock() == Blocks.BIRCH_WALL_SIGN ||
                state.getBlock() == Blocks.JUNGLE_WALL_SIGN ||
                state.getBlock() == Blocks.ACACIA_WALL_SIGN ||
                state.getBlock() == Blocks.DARK_OAK_WALL_SIGN;
    }
}