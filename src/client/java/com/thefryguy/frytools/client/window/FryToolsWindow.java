package com.thefryguy.frytools.client.window;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FryToolsWindow {
    private static Stage stage;
    private static SignScannerPanel signScannerPanel;
    private static CoordinateConverterPanel coordConverterPanel;
    private static CommandScannerPanel commandScannerPanel;

    public static void start() {
        Platform.startup(() -> {
            stage = new Stage();
            setupWindow();
        });
    }

    private static void setupWindow() {
        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: #C0C0C0;");

        // 1. System Menu Bar
        MenuBar menuBar = createMenuBar();
        mainPane.setTop(menuBar);

        // 2. Tabs
        TabPane tabPane = new TabPane();
        tabPane.setStyle(
                "-fx-background-color: #C0C0C0; " +
                        "-fx-font-family: 'MS Sans Serif', Arial; " +
                        "-fx-font-size: 12px; " +
                        "-fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; " +
                        "-fx-border-width: 2px;"
        );

        // Info Tab
        Tab infoTab = new Tab("Info");
        infoTab.setContent(InfoPanel.createInfoPanel());
        infoTab.setClosable(false);

        // Entities Tab
        Tab entitiesTab = new Tab("Entities");
        entitiesTab.setContent(EntityPanel.createEntityPanel());
        entitiesTab.setClosable(false);

        // Sign Scanner Tab
        Tab signScannerTab = new Tab("Sign Scanner");
        signScannerTab.setClosable(false);
        signScannerPanel = new SignScannerPanel();
        signScannerTab.setContent(signScannerPanel.createSignScannerPanel());

        // Coordinate Converter Tab
        Tab coordConverterTab = new Tab("Coord Converter");
        coordConverterTab.setClosable(false);
        coordConverterPanel = new CoordinateConverterPanel();
        coordConverterTab.setContent(coordConverterPanel.createCoordinateConverterPanel());

        // Command Scanner Tab
        Tab commandScannerTab = new Tab("Commands");
        commandScannerTab.setClosable(false);
        commandScannerPanel = new CommandScannerPanel();
        commandScannerTab.setContent(commandScannerPanel.createCommandScannerPanel());

        // Add all tabs
        tabPane.getTabs().addAll(
                infoTab,
                entitiesTab,
                signScannerTab,
                coordConverterTab,
                commandScannerTab
        );
        mainPane.setCenter(tabPane);

        // 3. Status Bar
        HBox statusBar = createStatusBar();
        mainPane.setBottom(statusBar);

        // Window configuration
        Scene scene = new Scene(mainPane, 800, 600);
        styleWindow(scene);
        stage.setScene(scene);
        stage.setTitle("FryTools Admin Panel");
        stage.show();
    }

    private static MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle(
                "-fx-background-color: #C0C0C0; " +
                        "-fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; " +
                        "-fx-border-width: 2px;"
        );

        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> stage.close());
        fileMenu.getItems().add(exitItem);

        Menu viewMenu = new Menu("View");
        CheckMenuItem refreshItem = new CheckMenuItem("Auto-Refresh");
        viewMenu.getItems().add(refreshItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
        return menuBar;
    }

    private static HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setStyle(
                "-fx-background-color: #C0C0C0; " +
                        "-fx-border-color: #808080 #FFFFFF #FFFFFF #808080; " +
                        "-fx-border-width: 2px; " +
                        "-fx-padding: 4px;"
        );
        Label statusLabel = new Label("Ready");
        statusBar.getChildren().add(statusLabel);
        return statusBar;
    }

    private static void styleWindow(Scene scene) {
        scene.setFill(Color.TRANSPARENT);
        scene.getRoot().setStyle(
                "-fx-border-color: #FFFFFF #808080 #808080 #FFFFFF; " +
                        "-fx-border-width: 2px; " +
                        "-fx-background-color: #C0C0C0;"
        );
    }
}