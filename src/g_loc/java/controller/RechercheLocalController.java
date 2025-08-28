package controller;

import entities.local;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import services.ServiceLocal;

import java.io.IOException;
import java.util.List;

public class RechercheLocalController {

    private local selectedLocal;
    @FXML
    private TextField rechercheTextField;

    @FXML
    private FlowPane flowPaneLocaux;

    @FXML
    private Button filtrerCapaciteButton;

    @FXML
    private Button filtrerDisponibiliteButton;

    private final  ServiceLocal serviceLocal = new ServiceLocal();



    @FXML
    private void openAfficherLocal() {
        try {
            // Load the AfficherLocal.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("AfficherLocal.fxml"));
            AnchorPane afficherLocalPane = loader.load();

            // Get the controller for AfficherLocal view
            AfficherLocalController afficherLocalController = loader.getController();

            // Pass the selected local object to the controller
            afficherLocalController.setLocalDetails(selectedLocal);

            // Replace the content of flowPaneLocaux with the details view
            flowPaneLocaux.getChildren().setAll(afficherLocalPane);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    public void initialize() {
        // Bind the filter buttons to their actions
        filtrerCapaciteButton.setOnAction(event -> filtrerParCapacite());
        filtrerDisponibiliteButton.setOnAction(event -> filtrerParDisponibilite());

        // Initialize with all available locaux
        afficherLocaux(serviceLocal.getAll(new local()));
    }

    // Method to display locaux in the FlowPane
    private void afficherLocaux(List<local> locaux) {
        flowPaneLocaux.getChildren().clear(); // Clear existing items

        for (local l : locaux) {
            VBox vbox = new VBox(10);
            vbox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 20; -fx-border-radius: 10; -fx-background-radius: 10;");

            Label nomLabel = new Label(l.getNom());
            nomLabel.setStyle("-fx-font-weight: bold;");
            Label capaciteLabel = new Label("Capacity: " + l.getCapacite());
            Label disponibiliteLabel = new Label(l.isDisponible() ? "Available" : "Booked");
            disponibiliteLabel.setStyle(l.isDisponible() ? "-fx-text-fill: green;" : "-fx-text-fill: red;");

            // Add the local to the VBox
            vbox.getChildren().addAll(nomLabel, capaciteLabel, disponibiliteLabel);

            // Add the click event to open AfficherLocal
            vbox.setOnMouseClicked(event -> {
                // Set the selectedLocal to the clicked local
                selectedLocal = l;
                openAfficherLocal(); // Now call the method to load the new view
            });

            // Add the VBox to the FlowPane
            flowPaneLocaux.getChildren().add(vbox);
        }
    }


    // Search method
    @FXML
    public void rechercherLocaux() {
        String keyword = rechercheTextField.getText().trim();
        if (!keyword.isEmpty()) {
            List<local> resultats = serviceLocal.rechercherLocal(keyword);
            afficherLocaux(resultats);
        } else {
            afficherLocaux(serviceLocal.getAll(new local()));
        }
    }

    // Filter by capacity
    @FXML
    private void filtrerParCapacite() {
        List<local> locauxFiltres = serviceLocal.trierLocaux("capacite", true); // Sort by capacity ascending
        afficherLocaux(locauxFiltres);
    }

    // Filter by availability
    @FXML
    private void filtrerParDisponibilite() {
        List<local> locauxFiltres = serviceLocal.trierLocaux("disponible", true); // Sort by availability
        afficherLocaux(locauxFiltres);
    }

}


