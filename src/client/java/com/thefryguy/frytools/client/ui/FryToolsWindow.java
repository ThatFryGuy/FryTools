package com.thefryguy.frytools.client.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FryToolsWindow {
    private static Stage stage;

    public static void start() {
        Platform.startup(() -> {
            stage = new Stage();
            setupWindow();
        });
    }

    private static void setupWindow() {
        // Main container with classic Win95 layout
        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: #C0C0C0;");

        // 1. System Menu Bar
        MenuBar menuBar = createMenuBar();
        mainPane.setTop(menuBar);

        // 2. Tabbed Interface
        TabPane tabPane = createTabPane();
        mainPane.setCenter(tabPane);

        // 3. Status Bar
        HBox statusBar = createStatusBar();
        mainPane.setBottom(statusBar);

        // Window configuration
        Scene scene = new Scene(mainPane, 800, 600);
        styleWindow(scene);
        stage.setScene(scene);
        stage.setTitle("FryTools Admin Panel");
        stage.setAlwaysOnTop(true);
        stage.show();
    }

    private static MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #C0C0C0; -fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; -fx-border-width: 2px;");

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> stage.close());
        fileMenu.getItems().add(exitItem);

        // View Menu
        Menu viewMenu = new Menu("View");
        CheckMenuItem refreshItem = new CheckMenuItem("Auto-Refresh");
        viewMenu.getItems().addAll(refreshItem);

        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
        return menuBar;
    }

    private static TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #C0C0C0; " +
                "-fx-font-family: 'MS Sans Serif', Arial; " +
                "-fx-font-size: 12px; " +
                "-fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; " +
                "-fx-border-width: 2px;");

        // Process Tab (Example)
        Tab processesTab = new Tab("Processes");
        processesTab.setContent(createProcessView());
        processesTab.setClosable(false);

        // Players Tab
        Tab playersTab = new Tab("Players");
        playersTab.setContent(createPlayerView());
        playersTab.setClosable(false);

        tabPane.getTabs().addAll(processesTab, playersTab);
        return tabPane;
    }

    private static HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setStyle("-fx-background-color: #C0C0C0; " +
                "-fx-border-color: #808080 #FFFFFF #FFFFFF #808080; " +
                "-fx-border-width: 2px; " +
                "-fx-padding: 4px;");

        Label cpuLabel = new Label("CPU Usage: 15%");
        Label memoryLabel = new Label("Memory: 1.2GB/4.0GB");
        Label statusLabel = new Label("Ready");

        statusBar.getChildren().addAll(statusLabel, new Separator(), cpuLabel, new Separator(), memoryLabel);
        return statusBar;
    }

    private static void styleWindow(Scene scene) {
        scene.setFill(Color.TRANSPARENT);
        scene.getRoot().setStyle(
                "-fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; " +
                        "-fx-border-width: 2px; " +
                        "-fx-background-color: #C0C0C0;");
    }

    private static VBox createProcessView() {
        VBox content = new VBox(5);
        content.setStyle("-fx-padding: 8px;");

        // Classic Win95 TreeView
        TreeView<String> processTree = new TreeView<>();
        TreeItem<String> rootItem = new TreeItem<>("Minecraft Processes");
        rootItem.getChildren().addAll(
                new TreeItem<>("Main Render Thread"),
                new TreeItem<>("Server Thread"),
                new TreeItem<>("Audio Thread")
        );
        processTree.setRoot(rootItem);
        processTree.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #808080 #FFFFFF #FFFFFF #808080; " +
                "-fx-border-width: 2px;");

        // Classic scrollbar styling
        processTree.lookupAll(".scroll-bar").forEach(node ->
                node.setStyle("-fx-background-color: #C0C0C0; " +
                        "-fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; " +
                        "-fx-border-width: 1px;")
        );

        content.getChildren().addAll(
                new Label("Running Processes:"),
                processTree,
                createClassicButton("End Process")
        );
        return content;
    }

    private static VBox createPlayerView() {
        VBox content = new VBox(5);
        content.setStyle("-fx-padding: 8px;");

        TableView<String> playerTable = new TableView<>();
        // Add table columns and data here...

        content.getChildren().addAll(
                new Label("Connected Players:"),
                playerTable,
                createClassicButton("Teleport to Player")
        );
        return content;
    }

    private static Button createClassicButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #C0C0C0; " +
                "-fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; " +
                "-fx-border-width: 2px; " +
                "-fx-font-family: 'MS Sans Serif'; " +
                "-fx-font-size: 12px; " +
                "-fx-padding: 3 8 3 8;");

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-background-color: #000080; -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-background-color: #000080; -fx-text-fill: white;", "")));

        return button;
    }
}