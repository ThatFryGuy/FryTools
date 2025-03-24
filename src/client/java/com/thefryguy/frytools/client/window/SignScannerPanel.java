package com.thefryguy.frytools.client.window;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignScannerPanel extends VBox {
    private final MinecraftClient client;
    private final ObservableList<SignEntry> signEntries = FXCollections.observableArrayList();
    // Store last sign data so it can be saved as CSV later.
    private Map<BlockPos, String> lastSignData = new HashMap<>();
    private TableView<SignEntry> signTable;

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

        // Setup table view like in CommandScannerPanel.
        setupTable();

        // Create Refresh and Save as .csv buttons with matching style.
        Button refreshButton = createClassicButton("↻");
        refreshButton.setOnAction(e -> refreshSignData());

        Button saveCsvButton = createClassicButton("Save as .csv");
        saveCsvButton.setOnAction(e -> saveCSV());

        HBox buttonPanel = new HBox(5);
        buttonPanel.getChildren().addAll(refreshButton, saveCsvButton);

        // Optionally, add a status label (here just for demonstration).
        Label statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #000000;");

        getChildren().addAll(
                titleLabel,
                signTable,
                buttonPanel,
                statusLabel
        );

        refreshSignData();
    }

    private void setupTable() {
        signTable = new TableView<>();
        signTable.setPlaceholder(new Label("No signs scanned yet"));
        signTable.setStyle(
                "-fx-border-color: #808080 #FFFFFF #FFFFFF #808080; " +
                        "-fx-border-width: 2px; " +
                        "-fx-background-color: white;"
        );

        TableColumn<SignEntry, String> posCol = new TableColumn<>("Position");
        posCol.setCellValueFactory(new PropertyValueFactory<>("position"));
        posCol.setPrefWidth(200);

        TableColumn<SignEntry, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentCol.setPrefWidth(400);

        signTable.getColumns().addAll(posCol, contentCol);
        signTable.setItems(signEntries);
    }

    private Button createClassicButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: #C0C0C0; " +
                        "-fx-font-family: 'MS Sans Serif'; " +
                        "-fx-border-color: #808080 #FFFFFF #FFFFFF #808080; " +
                        "-fx-border-width: 2px;"
        );
        return btn;
    }

    private void refreshSignData() {
        if (client.world == null || client.player == null) return;

        client.execute(() -> {
            Map<BlockPos, String> signData = new HashMap<>();
            ObservableList<SignEntry> newEntries = FXCollections.observableArrayList();
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
                            StringBuilder content = new StringBuilder();
                            Arrays.stream(sign.getFrontText().getMessages(true))
                                    .map(Text::getString)
                                    .filter(lineText -> !lineText.isEmpty())
                                    .forEach(lineText -> content.append(lineText).append(" "));
                            String text = !content.isEmpty() ? content.toString().trim() : "[Empty Sign]";
                            signData.put(pos, text);
                            String positionText = String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ());
                            newEntries.add(new SignEntry(positionText, text));
                        }
                    }
                }
            }

            Platform.runLater(() -> {
                lastSignData = signData;
                signEntries.setAll(newEntries);
            });
        });
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

    private void saveCSV() {
        if (lastSignData.isEmpty()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Signs as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Position,Content\n");
                for (Map.Entry<BlockPos, String> entry : lastSignData.entrySet()) {
                    BlockPos pos = entry.getKey();
                    String position = String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ());
                    writer.write("\"" + position + "\",\"" + entry.getValue().replace("\"", "\"\"") + "\"\n");
                }
                System.out.println("CSV saved successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error saving CSV: " + e.getMessage());
            }
        }
    }

    public static class SignEntry {
        private final SimpleStringProperty position;
        private final SimpleStringProperty content;

        public SignEntry(String position, String content) {
            this.position = new SimpleStringProperty(position);
            this.content = new SimpleStringProperty(content);
        }

        public String getPosition() {
            return position.get();
        }

        public SimpleStringProperty positionProperty() {
            return position;
        }

        public String getContent() {
            return content.get();
        }

        public SimpleStringProperty contentProperty() {
            return content;
        }
    }
}
