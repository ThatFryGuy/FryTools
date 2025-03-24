package com.thefryguy.frytools.client.window;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignScannerPanel extends VBox {
    private final MinecraftClient client;
    private GridPane signGrid;

    public SignScannerPanel() {
        super(10);
        this.client = MinecraftClient.getInstance(); // Ensure this is initialized on the client thread
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

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #C0C0C0; " +
                "-fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; " +
                "-fx-border-width: 2px;");
        refreshButton.setOnAction(e -> replaceSignData());

        ScrollPane scrollPane = new ScrollPane(signGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(200);

        getChildren().addAll(
                titleLabel,
                scrollPane,
                refreshButton
        );

        replaceSignData();
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

        client.execute(() -> { // Ensure this is executed on the client thread
            Map<BlockPos, String> signData = new HashMap<>();
            BlockPos playerPos = client.player.getBlockPos();
            int radius = 75;

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = playerPos.add(x, y, z);
                        BlockState state = client.world.getBlockState(pos);

                        if (!isSign(state)) continue; // Check if it's a sign before getting block entity

                        BlockEntity blockEntity = client.world.getBlockEntity(pos);

                        if (blockEntity instanceof SignBlockEntity sign) {
                            StringBuilder content = new StringBuilder();
                            // Using filtered version of getMessages() to avoid empty or hidden lines
                            Arrays.stream(sign.getFrontText().getMessages(true)) // Use the filtered version
                                    .map(Text::getString)
                                    .filter(lineText -> !lineText.isEmpty()) // Avoid empty lines
                                    .forEach(lineText -> content.append(lineText).append(" "));
                            String text = !content.isEmpty() ? content.toString().trim() : "[Empty Sign]";
                            signData.put(pos, text); // Replaced add() with put()
                        }
                    }
                }
            }

            Platform.runLater(() -> {
                clearGrid(signGrid);

                int row = 1;
                for (Map.Entry<BlockPos, String> entry : signData.entrySet()) {
                    BlockPos pos = entry.getKey();
                    String positionText = String.format("(%d, %d, %d)",
                            pos.getX(), pos.getY(), pos.getZ());

                    signGrid.addRow(row++,
                            createDataCell(positionText),
                            createDataCell(entry.getValue())
                    );
                }
            });
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

    // This method checks if the block is a sign.
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
