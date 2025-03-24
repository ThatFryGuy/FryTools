package com.thefryguy.frytools.client.window;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

public class CommandScannerPanel {
    private final ObservableList<CommandEntry> commands = FXCollections.observableArrayList();
    private final TableView<CommandEntry> commandTable = new TableView<>();
    private final TextField searchField = new TextField();
    private final Label statusLabel = new Label("Ready");

    public CommandScannerPanel() {
        // When joining a server, clear the UI and automatically refresh the command list.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            resetUI();
            Platform.runLater(() -> statusLabel.setText("Connected - Retrieving command data..."));
            refreshCommands();
        });
    }

    /**
     * Refresh the list of available commands by querying the client's command dispatcher.
     */
    private void refreshCommands() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.getNetworkHandler() != null) {
            statusLabel.setText("Refreshing...");
            // Execute on the client thread to safely access the network handler.
            client.execute(() -> {
                try {
                    // Get the command dispatcher's root node from the network handler.
                    RootCommandNode<CommandSource> rootNode = client.getNetworkHandler().getCommandDispatcher().getRoot();
                    Platform.runLater(() -> {
                        commands.clear();
                        parseCommandTree(rootNode);
                        statusLabel.setText("Loaded " + commands.size() + " commands");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("Error retrieving command tree: " + e.getMessage()));
                    e.printStackTrace();
                }
            });
        } else {
            statusLabel.setText("Player or network handler is not available.");
        }
    }

    /**
     * Recursively parse the command tree starting from the root node.
     */
    private void parseCommandTree(RootCommandNode<CommandSource> rootNode) {
        try {
            rootNode.getChildren().forEach(node -> parseNode(node, ""));
        } catch (Exception e) {
            Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
        }
    }

    /**
     * Recursively parse each node and add the command (with syntax) to the list.
     */
    private void parseNode(CommandNode<CommandSource> node, String prefix) {
        String fullCommand = prefix + node.getName();

        if (node.getCommand() != null) {
            commands.add(new CommandEntry(
                    fullCommand,
                    "/" + fullCommand + buildArguments(node)
            ));
        }

        node.getChildren().forEach(child -> parseNode(child, fullCommand + " "));
    }

    /**
     * Build a simple representation of the command arguments.
     */
    private String buildArguments(CommandNode<CommandSource> node) {
        if (node.getRedirect() != null) {
            return " -> " + node.getRedirect().getName();
        }
        // This is a simple fallback; for a more detailed argument list you might need to inspect the node further.
        return node.getRequirement() != null ? " <args>" : "";
    }

    private void styleTextField(TextField field) {
        field.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #808080 #FFFFFF #FFFFFF #808080; " +
                        "-fx-border-width: 2px;"
        );
    }

    private Button createClassicButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-base: #C0C0C0; " +
                        "-fx-font-family: 'MS Sans Serif'; " +
                        "-fx-border-color: #808080 #FFFFFF #FFFFFF #808080; " +
                        "-fx-border-width: 2px;"
        );
        return btn;
    }

    /**
     * Filter commands based on the text in the search field.
     */
    private void filterCommands(String filter) {
        if (filter.isEmpty()) {
            commandTable.setItems(commands);
            return;
        }

        ObservableList<CommandEntry> filtered = FXCollections.observableArrayList();
        String lowerFilter = filter.toLowerCase();

        for (CommandEntry entry : commands) {
            if (entry.getName().toLowerCase().contains(lowerFilter) ||
                    entry.getSyntax().toLowerCase().contains(lowerFilter)) {
                filtered.add(entry);
            }
        }
        commandTable.setItems(filtered);
    }

    private void resetUI() {
        commands.clear();
        statusLabel.setText("Ready");
    }

    /**
     * Build and return the panel containing the command scanner UI.
     */
    public VBox createCommandScannerPanel() {
        VBox layout = new VBox(5);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: #C0C0C0;");

        // Create the search panel with a text field and refresh button.
        HBox searchPanel = new HBox(5);
        searchField.setPromptText("Search commands...");
        searchField.setPrefWidth(250);
        styleTextField(searchField);
        searchField.textProperty().addListener((obs, old, newVal) -> filterCommands(newVal));

        Button refreshBtn = createClassicButton("↻");
        refreshBtn.setOnAction(e -> refreshCommands());
        searchPanel.getChildren().addAll(searchField, refreshBtn);

        setupTable();

        // Status bar at the bottom
        HBox statusBar = new HBox(5);
        statusLabel.setStyle("-fx-text-fill: #000000;");
        statusBar.getChildren().add(statusLabel);

        layout.getChildren().addAll(searchPanel, commandTable, statusBar);
        return layout;
    }

    /**
     * Set up the table view to display commands.
     */
    private void setupTable() {
        commandTable.setPlaceholder(new Label("No commands received yet - join a server"));
        commandTable.setStyle(
                "-fx-border-color: #808080 #FFFFFF #FFFFFF #808080; " +
                        "-fx-border-width: 2px; " +
                        "-fx-background-color: white;"
        );

        TableColumn<CommandEntry, String> nameCol = new TableColumn<>("Command");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setPrefWidth(200);

        TableColumn<CommandEntry, String> syntaxCol = new TableColumn<>("Syntax");
        syntaxCol.setCellValueFactory(cellData -> cellData.getValue().syntaxProperty());
        syntaxCol.setPrefWidth(400);

        commandTable.getColumns().addAll(nameCol, syntaxCol);
        commandTable.setItems(commands);
    }

    /**
     * Simple data class for holding a command and its syntax.
     */
    public static class CommandEntry {
        private final SimpleStringProperty name;
        private final SimpleStringProperty syntax;

        public CommandEntry(String name, String syntax) {
            this.name = new SimpleStringProperty(name);
            this.syntax = new SimpleStringProperty(syntax);
        }

        public String getName() { return name.get(); }
        public String getSyntax() { return syntax.get(); }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty syntaxProperty() { return syntax; }
    }
}
