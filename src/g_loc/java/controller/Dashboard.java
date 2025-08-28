package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

public class Dashboard {

    @FXML
    private AnchorPane contentArea;

    @FXML
    public void initialize() {
        // Load initial content
        switchScene("Accueil.fxml");
    }

    @FXML
    public void Ajouter_Local() {
        switchScene("AjouterLocal.fxml");
    }

    @FXML
    public void Liste_Locaux() {
        switchScene("ListedesLocaux.fxml");
    }
    
    @FXML
    public void openBackoffice() {
        try {
            // Try to load using a more direct approach
            URL resource = getClass().getClassLoader().getResource("VenueBackoffice.fxml");
            if (resource == null) {
                // Try alternate path if first approach fails
                resource = getClass().getResource("/VenueBackoffice.fxml");
            }
            
            if (resource == null) {
                showError("Resource Not Found", "Could not find VenueBackoffice.fxml. Please ensure the file exists in the resources directory.");
                return;
            }
            
            Parent newContent = FXMLLoader.load(resource);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);

            // Anchor the new content to fill the content area
            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 0.0);
            AnchorPane.setRightAnchor(newContent, 0.0);
        } catch (IOException e) {
            showError("Error", "Could not load VenueBackoffice.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void switchScene(String fxmlFile) {
        try {
            URL resource = getClass().getClassLoader().getResource(fxmlFile);
            if (resource == null) {
                showError("Error", "Could not find file: " + fxmlFile);
                return;
            }
            
            Parent newContent = FXMLLoader.load(resource);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);

            // Anchor the new content to fill the content area
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
    
    @FXML
    public void OuvrirPanier(ActionEvent actionEvent) {
        switchScene("Panier.fxml");
    }
}
