package com.thefryguy.frytools.client.window;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class EntityPanel extends VBox {
    private final MinecraftClient client;
    private GridPane entityGrid;
    private GridPane totalsGrid;
    private final Map<String, Integer> entityCounts = new HashMap<>();
    private boolean sortDescending = true;

    public EntityPanel() {
        super(10);
        this.client = MinecraftClient.getInstance();
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #C0C0C0;");
        setupUI();
    }

    public static Node createEntityPanel() {
        return new EntityPanel();
    }

    private void setupUI() {
        Label titleLabel = new Label("Entity Manager");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #000080;");

        // Entity List Section
        Label listHeader = createSectionHeader("Nearby Entities");
        entityGrid = createStyledGrid();
        addGridHeader(entityGrid, "Type", "Position", "UUID");

        // Totals Section
        Label totalsHeader = createSectionHeader("Entity Totals");
        totalsGrid = createStyledGrid();
        addGridHeader(totalsGrid, "Entity Type", "Count");

        // Sort Button
        Button sortButton = new Button("Sort ▼");
        sortButton.setStyle(createButtonStyle());
        sortButton.setOnAction(e -> toggleSortOrder(sortButton));

        // Refresh Button
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle(createButtonStyle());
        refreshButton.setOnAction(e -> updateEntities());

        // Control Panel
        HBox buttonPanel = new HBox(10, sortButton, refreshButton);
        buttonPanel.setPadding(new Insets(5, 0, 5, 0));

        // Scroll Panes
        ScrollPane listScroll = createScrollPane(entityGrid);
        ScrollPane totalsScroll = createScrollPane(totalsGrid);

        getChildren().addAll(
                titleLabel,
                listHeader,
                listScroll,
                totalsHeader,
                totalsScroll,
                buttonPanel
        );

        updateEntities();
    }

    private Label createSectionHeader(String text) {
        Label header = new Label(text);
        header.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 5 0; -fx-font-size: 14px;");
        return header;
    }

    private GridPane createStyledGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 10, 5, 10));
        return grid;
    }

    private void addGridHeader(GridPane grid, String... headers) {
        for (int i = 0; i < headers.length; i++) {
            Label header = createCellLabel(headers[i]);
            header.setStyle("-fx-font-weight: bold; -fx-background-color: #000080; -fx-text-fill: white;");
            grid.add(header, i, 0);
        }
    }

    private String createButtonStyle() {
        return "-fx-background-color: #C0C0C0; " +
                "-fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; " +
                "-fx-border-width: 2px; " +
                "-fx-font-family: 'MS Sans Serif'; " +
                "-fx-font-size: 12px; " +
                "-fx-padding: 3 8 3 8;";
    }

    private void toggleSortOrder(Button sortButton) {
        sortDescending = !sortDescending;
        sortButton.setText(sortDescending ? "Sort ▼" : "Sort ▲");
        updateEntities();
    }

    private ScrollPane createScrollPane(GridPane content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setStyle("-fx-background-color: #C0C0C0;");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(200);
        return scrollPane;
    }

    private Label createCellLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-family: 'MS Sans Serif'; " +
                "-fx-font-size: 12px; " +
                "-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #808080 #FFFFFF #FFFFFF #808080; " +
                "-fx-border-width: 1px; " +
                "-fx-padding: 2px 4px;");
        return label;
    }

    private void addEntityRow(int row, String type, String position, String uuid) {
        Platform.runLater(() -> entityGrid.addRow(row,
                createCellLabel(type),
                createCellLabel(position),
                createCellLabel(uuid)
        ));
    }

    public void updateEntities() {
        if (client.world == null || client.player == null) return;

        client.execute(() -> {
            entityCounts.clear();
            Iterable<Entity> entities = client.world.getEntities();
            Map<String, List<BlockPos>> entityPositions = new HashMap<>();

            for (Entity entity : entities) {
                if (entity == client.player) continue;
                String typeName = getCleanEntityName(entity);
                entityCounts.put(typeName, entityCounts.getOrDefault(typeName, 0) + 1);
                entityPositions.computeIfAbsent(typeName, k -> new ArrayList<>()).add(entity.getBlockPos());
            }

            List<Map.Entry<String, Integer>> sortedCounts = entityCounts.entrySet().stream()
                    .sorted((e1, e2) -> sortDescending ?
                            e2.getValue().compareTo(e1.getValue()) :
                            e1.getValue().compareTo(e2.getValue()))
                    .toList();

            Platform.runLater(() -> {
                clearGrid(entityGrid);
                clearGrid(totalsGrid);

                // Add entity list
                int listRow = 1;
                for (Map.Entry<String, List<BlockPos>> entry : entityPositions.entrySet()) {
                    List<BlockPos> positions = entry.getValue();
                    int displayCount = Math.min(positions.size(), 10);

                    for (int i = 0; i < displayCount; i++) {
                        BlockPos pos = positions.get(i);
                        addEntityRow(listRow++,
                                entry.getKey(),
                                String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ()),
                                positions.size() + " total"
                        );
                    }
                }

                // Add sorted totals
                int totalsRow = 1;
                for (Map.Entry<String, Integer> entry : sortedCounts) {
                    Label countLabel = createCellLabel(entry.getValue().toString());
                    countLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #000080;");
                    totalsGrid.addRow(totalsRow++,
                            createCellLabel(entry.getKey()),
                            countLabel
                    );
                }
            });
        });
    }

    private String getCleanEntityName(Entity entity) {
        String rawName = entity.getType().getTranslationKey();
        String[] parts = rawName.split("\\.");
        if (parts.length == 0) return "Unknown";

        String namePart = parts[parts.length - 1];
        return Arrays.stream(namePart.split("_"))
                .map(word -> word.isEmpty() ? "" :
                        word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    private void clearGrid(GridPane grid) {
        grid.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null &&
                        GridPane.getRowIndex(node) >= 1
        );
    }
}