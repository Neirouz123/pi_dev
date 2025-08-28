package controller;

import entities.local;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import services.ServiceLocal;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ListeDesLocaux {
    @FXML
    private TextField rechercheTextField;

    @FXML
    private FlowPane flowPaneLocaux;

    @FXML
    private Button filtrerCapaciteButton;

    @FXML
    private Button filtrerDisponibiliteButton;

    private final ServiceLocal serviceLocal = new ServiceLocal();

    @FXML
    public void initialize() {
        try {
            // Initialize with all available locaux
            afficherLocaux(serviceLocal.getAll(new local()));
            
            // Add listener for real-time search
            rechercheTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.trim().isEmpty()) {
                    afficherLocaux(serviceLocal.getAll(new local()));
                } else {
                    List<local> resultats = serviceLocal.rechercherLocal(newValue.trim());
                    afficherLocaux(resultats);
                }
            });
        } catch (Exception e) {
            showError("Initialization Error", "Failed to initialize venue list: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to display locaux using LocalCard
    private void afficherLocaux(List<local> locaux) {
        flowPaneLocaux.getChildren().clear(); // Clear existing items

        for (local l : locaux) {
            try {
                // Get the resource URL
                URL resourceUrl = getClass().getClassLoader().getResource("LocalCard.fxml");
                if (resourceUrl == null) {
                    showError("Resource Error", "Could not find LocalCard.fxml");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(resourceUrl);
                loader.load();
                
                LocalCardController controller = loader.getController();
                controller.setLocalDetails(l);
                
                flowPaneLocaux.getChildren().add(loader.getRoot());
            } catch (IOException e) {
                showError("Loading Error", "Failed to load venue card: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Search method
    @FXML
    public void rechercherLocaux() {
        try {
            String keyword = rechercheTextField.getText().trim();
            if (!keyword.isEmpty()) {
                List<local> resultats = serviceLocal.rechercherLocal(keyword);
                afficherLocaux(resultats);
            } else {
                afficherLocaux(serviceLocal.getAll(new local()));
            }
        } catch (Exception e) {
            showError("Search Error", "Failed to search venues: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Filter by capacity
    @FXML
    private void filtrerParCapacite() {
        try {
            List<local> locauxFiltres = serviceLocal.trierLocaux("capacite", true);
            afficherLocaux(locauxFiltres);
        } catch (Exception e) {
            showError("Filter Error", "Failed to filter by capacity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Filter by availability
    @FXML
    private void filtrerParDisponibilite() {
        try {
            List<local> locauxFiltres = serviceLocal.trierLocaux("disponible", true);
            afficherLocaux(locauxFiltres);
        } catch (Exception e) {
            showError("Filter Error", "Failed to filter by availability: " + e.getMessage());
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
