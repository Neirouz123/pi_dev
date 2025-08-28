package controller;

import entities.local;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class AfficherLocalController {

    @FXML
    private Label localNameLabel;

    @FXML
    private Label localCapacityLabel;

    @FXML
    private Label localAvailabilityLabel;

    @FXML
    private Label localAdresseLabel;

    @FXML
    private Label localPrixLabel;

    @FXML
    private Label localDescriptionLabel;

    @FXML
    private Button modifierButton;

    private local localDetails;

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Button btnModifierLocal;

    @FXML
    private FlowPane imagesFlowPane; // FlowPane to hold the images dynamically

    // Initialize the controller with the local details
    public void setLocalDetails(local localDetails) {
        this.localDetails = localDetails;

        if (localDetails != null) {
            // Set the local's name
            localNameLabel.setText(localDetails.getNom());

            // Set the local's address
            localAdresseLabel.setText("Adresse: " + localDetails.getAdresse());

            // Set the description of the local
            localDescriptionLabel.setText("Description: " + localDetails.getDescription());

            // Set the capacity of the local
            localCapacityLabel.setText("Capacité: " + localDetails.getCapacite());

            // Set the availability status of the local
            localAvailabilityLabel.setText(localDetails.isDisponible() ? "Disponible" : "Loué");
            localAvailabilityLabel.setStyle(localDetails.isDisponible() ? "-fx-text-fill: green;" : "-fx-text-fill: red;");

            // Set the price of the local
            localPrixLabel.setText("Prix: " + localDetails.getPrix() + " DT");

            // Display the images (if available)
            List<byte[]> images = localDetails.getImages();
            if (images != null && !images.isEmpty()) {
                imagesFlowPane.getChildren().clear(); // Clear any previous images

                for (byte[] imageBytes : images) {
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
                    Image image = new Image(byteArrayInputStream);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(150);
                    imageView.setFitHeight(150);
                    imageView.setPreserveRatio(true);
                    imagesFlowPane.getChildren().add(imageView);
                }
            } else {
                // Use a default image if no images are available
                Image defaultImage = new Image("file:/C:/Users/nayrouz/Pictures/Screenshots/Screenshot 2025-02-18 191121.png");
                ImageView defaultImageView = new ImageView(defaultImage);
                defaultImageView.setFitWidth(150);
                defaultImageView.setFitHeight(150);
                defaultImageView.setPreserveRatio(true);
                imagesFlowPane.getChildren().add(defaultImageView);
            }
        }
    }

    // Method triggered when the "Modifier" button is clicked
    @FXML
    private void modifier_Local() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ModifierLocal.fxml"));
            AnchorPane editPane = loader.load();

            // Get the controller of the "ModifierLocal" FXML file
            ModifierLocal editController = loader.getController();
            editController.setLocalDetails(localDetails);

            Stage stage = new Stage();
            stage.setTitle("Modifier Local");
            stage.setScene(new Scene(editPane));
            stage.showAndWait();

            // Refresh the local details after editing
            setLocalDetails(localDetails);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to open the editing form (optionally if you need to change content dynamically)
    private void openEditLocalForm(local localDetails) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ModifierLocal.fxml"));
            AnchorPane pane = loader.load();

            ModifierLocal editController = loader.getController();
            editController.setLocalDetails(localDetails);

            // Set the loaded pane in the content area of the current view
            if (contentArea != null) {
                contentArea.getChildren().setAll(pane);
            } else {
                System.out.println("Error: contentArea is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
