package com.thefryguy.frytools.client.window;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class CoordinateConverterPanel {
    private final TextField chunkXField = new TextField();
    private final TextField chunkZField = new TextField();
    private final TextField worldXField = new TextField();
    private final TextField worldYField = new TextField();
    private final TextField worldZField = new TextField();

    public GridPane createCoordinateConverterPanel() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setStyle("-fx-background-color: #C0C0C0;");

        // Chunk Coordinates Column
        grid.add(new Label("Chunk Coords:"), 0, 0);
        grid.add(new Label("X:"), 0, 1);
        grid.add(chunkXField, 1, 1);
        grid.add(new Label("Z:"), 0, 2);
        grid.add(chunkZField, 1, 2);

        // Convert Button
        Button convertButton = new Button("Convert");
        convertButton.setStyle("-fx-base: #A0A0A0;");
        convertButton.setOnAction(e -> convertCoordinates());
        grid.add(convertButton, 0, 3, 2, 1);

        // Copy XYZ Button
        Button copyXYZButton = new Button("Copy XYZ");
        copyXYZButton.setStyle("-fx-base: #A0A0A0;");
        copyXYZButton.setOnAction(e -> copyAllToClipboard());
        grid.add(copyXYZButton, 0, 4, 2, 1);

        // World Coordinates Column
        grid.add(new Label("World Coords:"), 2, 0);

        // World X
        grid.add(new Label("X:"), 2, 1);
        grid.add(worldXField, 3, 1);
        Button copyX = createCopyButton(worldXField);
        grid.add(copyX, 4, 1);

        // World Y
        grid.add(new Label("Y:"), 2, 2);
        grid.add(worldYField, 3, 2);
        Button copyY = createCopyButton(worldYField);
        grid.add(copyY, 4, 2);

        // World Z
        grid.add(new Label("Z:"), 2, 3);
        grid.add(worldZField, 3, 3);
        Button copyZ = createCopyButton(worldZField);
        grid.add(copyZ, 4, 3);

        // Style fields
        worldXField.setEditable(false);
        worldYField.setEditable(false);
        worldZField.setEditable(false);
        worldYField.setText("0"); // Default Y to 0

        return grid;
    }

    private void convertCoordinates() {
        try {
            int chunkX = parseOrZero(chunkXField.getText());
            int chunkZ = parseOrZero(chunkZField.getText());

            worldXField.setText(String.valueOf(chunkX * 16));
            worldZField.setText(String.valueOf(chunkZ * 16));
        } catch (NumberFormatException e) {
            worldXField.setText("Invalid");
            worldZField.setText("Invalid");
        }
    }

    private int parseOrZero(String input) {
        return input.isEmpty() ? 0 : Integer.parseInt(input);
    }

    private Button createCopyButton(TextField field) {
        Button button = new Button("Copy");
        button.setStyle("-fx-base: #A0A0A0;");
        button.setOnAction(e -> copyToClipboard(field.getText()));
        return button;
    }

    private void copyAllToClipboard() {
        String x = worldXField.getText();
        String z = worldZField.getText();
        copyToClipboard(String.format("%s, 0, %s", x, z));
    }

    private void copyToClipboard(String content) {
        ClipboardContent cc = new ClipboardContent();
        cc.putString(content);
        Clipboard.getSystemClipboard().setContent(cc);
    }
}