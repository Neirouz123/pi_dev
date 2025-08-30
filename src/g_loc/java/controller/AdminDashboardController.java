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

public class AdminDashboardController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        // Set welcome message
        if (UserSession.isLoggedIn()) {
            welcomeLabel.setText("Welcome, " + UserSession.getCurrentUser().getUsername() + " (Admin)");
        }

        // Load default view - venue backoffice
        openVenueBackoffice();
    }

    @FXML
    public void openVenueBackoffice() {
        try {
            URL resource = getClass().getClassLoader().getResource("VenueBackoffice.fxml");
            if (resource == null) {
                resource = getClass().getResource("/VenueBackoffice.fxml");
            }

            if (resource == null) {
                showError("Resource Not Found", "Could not find VenueBackoffice.fxml");
                return;
            }

            Parent newContent = FXMLLoader.load(resource);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);

            // Anchor the content
            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 0.0);
            AnchorPane.setRightAnchor(newContent, 0.0);

        } catch (IOException e) {
            showError("Error", "Could not load venue management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void openUserManagement() {
        switchScene("UserManagement.fxml");
    }
    private void loadScene(String fxmlFile) {
        try {
            URL resource = getClass().getClassLoader().getResource(fxmlFile);
            if (resource == null) {
                resource = getClass().getResource("/" + fxmlFile);
            }

            if (resource == null) {
                showError("Resource Error", "Could not find " + fxmlFile);
                return;
            }

            Parent newContent = FXMLLoader.load(resource);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);

            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 0.0);
            AnchorPane.setRightAnchor(newContent, 0.0);

            System.out.println("Successfully loaded: " + fxmlFile);

        } catch (IOException e) {
            showError("Loading Error", "Could not load " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void openReservationManagement() {
        switchScene("ReservationManagement.fxml");
    }

    @FXML
    public void openReports() {
        switchScene("Reports.fxml");
    }

    @FXML
    public void logout() {
        try{
        UserSession.logout();

        URL resource = getClass().getClassLoader().getResource("Login.fxml");
        if (resource == null) {
            resource = getClass().getResource("/Login.fxml");
        }

        if (resource == null) {
            showError("Error", "Could not find Login.fxml");
            return;
        }

        Parent root = FXMLLoader.load(resource);
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