package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ClientDashboardController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        // Set welcome message
        if (UserSession.isLoggedIn()) {
            welcomeLabel.setText("Welcome, " + UserSession.getCurrentUser().getUsername());
        }

        // Load default view - venue listing
        browseVenues();
    }

    @FXML
    public void browseVenues() {
        switchScene("ListedesLocaux.fxml");
    }

    @FXML
    public void viewCart() {
        switchScene("Panier.fxml");
    }

    @FXML
    public void viewMyReservations() {
        switchScene("MyReservations.fxml");
    }

    @FXML
    public void logout() {
        try {
            UserSession.logout();

            // Return to login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 500));
            stage.setTitle("EventaPlan - Login");
            stage.centerOnScreen();

        } catch (IOException e) {
            showError("Error", "Could not return to login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void switchScene(String fxmlFile) {
        try {
            URL resource = getClass().getClassLoader().getResource(fxmlFile);
            if (resource == null) {
                showError("Error", "Could not find file: " + fxmlFile);
                return;
            }

            Parent newContent = FXMLLoader.load(resource);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);

            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 0.0);
            AnchorPane.setRightAnchor(newContent, 0.0);

        } catch (IOException e) {
            showError("Error", "Could not load file: " + fxmlFile + "\nError: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}