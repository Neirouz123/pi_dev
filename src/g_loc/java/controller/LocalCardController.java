package controller;

import entities.local;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class LocalCardController {
    @FXML
    private ImageView mainImage;

    @FXML
    private Label nomLabel;

    @FXML
    private Label adresseLabel;

    @FXML
    private Label capaciteLabel;

    @FXML
    private HBox disponibiliteLabel;

    @FXML
    private Circle statusDot;

    @FXML
    private Label statusText;

    @FXML
    private Button detailsButton;

    @FXML
    private Button reserverButton;

    @FXML
    private Label prixLabel;

    @FXML
    private Label categoryLabel;

    private local localDetails;

    // Format currency with proper symbol
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fr", "TN"));

    public void setLocalDetails(local local) {
        this.localDetails = local;

        // Set text values with improved formatting
        nomLabel.setText(local.getNom());
        adresseLabel.setText(local.getAdresse());
        capaciteLabel.setText(local.getCapacite() + " people");

        // Format price with currency
        String formattedPrice = currencyFormatter.format(local.getPrix());
        prixLabel.setText(formattedPrice);

        // Set up category label
        setupCategoryLabel(local);

        // Set up availability status
        setupAvailabilityStatus(local);

        // Load image
        loadVenueImage(local);

        // Set up button interactions
        setupButtonHoverEffects();
    }

    // Sets up the category label based on venue properties
    private void setupCategoryLabel(local local) {
        // Determine category based on capacity and price
        String category = "Standard";
        String badgeColor = "#f5f5f5";
        String textColor = "#757575";

        if (local.getPrix() > 1000) {
            category = "Premium";
            badgeColor = "#FFF8E1";
            textColor = "#FF8F00";
        } else if (local.getCapacite() > 200) {
            category = "Large";
            badgeColor = "#E8F5E9";
            textColor = "#388E3C";
        } else if (local.getCapacite() < 50) {
            category = "Intimate";
            badgeColor = "#E3F2FD";
            textColor = "#1976D2";
        }

        categoryLabel.setText(category);
        categoryLabel.setStyle("-fx-font-size: 11; -fx-text-fill: " + textColor +
                              "; -fx-background-color: " + badgeColor +
                              "; -fx-padding: 2 8; -fx-background-radius: 4;");
    }

    // Sets up availability status (available/booked)
    private void setupAvailabilityStatus(local local) {
        if (local.isDisponible()) {
            // Available styling
            statusText.setText("Available");
            disponibiliteLabel.setStyle("-fx-background-color: #4CAF50; -fx-padding: 4 8; -fx-background-radius: 4;");
            statusDot.setFill(Color.WHITE);

            // Enable booking button
            reserverButton.setDisable(false);
            reserverButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(25, 118, 210, 0.27));
            shadow.setRadius(6);
            shadow.setSpread(0.05);
            reserverButton.setEffect(shadow);
        } else {
            // Booked styling
            statusText.setText("Booked");
            disponibiliteLabel.setStyle("-fx-background-color: #F44336; -fx-padding: 4 8; -fx-background-radius: 4;");
            statusDot.setFill(Color.WHITE);

            // Apply a muted effect to the image for booked venues
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.1);
            colorAdjust.setSaturation(-0.2);
            mainImage.setEffect(colorAdjust);

            // Disable booking button
            reserverButton.setDisable(true);
            reserverButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: not-allowed;");
            reserverButton.setOpacity(0.7);
        }
    }

    // Loads the venue image
    private void loadVenueImage(local local) {
        if (local.getImages() != null && !local.getImages().isEmpty()) {
            ByteArrayInputStream bis = new ByteArrayInputStream(local.getImages().get(0));
            Image venueImage = new Image(bis);
            mainImage.setImage(venueImage);

            // Simple fade-in effect
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainImage);
            fadeIn.setFromValue(0.6);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } else {
            // Set a placeholder image
            mainImage.setImage(new Image("https://via.placeholder.com/280x160.png?text=Venue"));
        }
    }

    // Sets up button hover effects
    private void setupButtonHoverEffects() {
        // Details button hover effect
        detailsButton.setOnMouseEntered(e -> {
            detailsButton.setStyle("-fx-background-color: #f5f9ff; -fx-border-color: #1976D2; -fx-text-fill: #1976D2; -fx-background-radius: 4; -fx-border-radius: 4; -fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        });

        detailsButton.setOnMouseExited(e -> {
            detailsButton.setStyle("-fx-background-color: white; -fx-border-color: #1976D2; -fx-text-fill: #1976D2; -fx-background-radius: 4; -fx-border-radius: 4; -fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        });

        // Book button hover effect (only if enabled)
        if (!reserverButton.isDisable()) {
            reserverButton.setOnMouseEntered(e -> {
                reserverButton.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
            });

            reserverButton.setOnMouseExited(e -> {
                reserverButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
            });
        }
    }

    @FXML
    private void handleDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("AfficherLocalClient.fxml"));
            Scene scene = new Scene(loader.load());

            AfficherLocalClient controller = loader.getController();
            controller.setLocalDetails(localDetails);

            Stage stage = new Stage();
            stage.setTitle("Venue Details - " + localDetails.getNom());
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleReserver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Calendrier.fxml"));
            Parent root = loader.load();

            // Pass the localId (so calendar knows which venue to display)
            Calendrier calendarController = loader.getController();
            calendarController.setLocalId(localDetails.getId());
            calendarController.generateCalendar(); // refresh after setting ID

            // Show calendar
            Scene scene = new Scene(root);
            Stage stage = (Stage) reserverButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("EventaPlan - Calendrier");
            stage.show();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d’ouvrir le calendrier");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    // Helper method to open the calendar view
    private void openCalendarView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Calendrier.fxml"));
            Scene scene = new Scene(loader.load());

            Calendrier controller = loader.getController();
            controller.setLocalId(localDetails.getId());
            controller.generateCalendar();

            Stage stage = new Stage();
            stage.setTitle("Book Venue - " + localDetails.getNom());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 