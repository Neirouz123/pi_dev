package controller;

import entities.local;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.ServiceLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AjouterLocalController {
    @FXML
    private TextField nomField;
    
    @FXML
    private TextField adresseField;
    
    @FXML
    private TextField capaciteField;
    
    @FXML
    private TextField prixField;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private ImageView imagePreview;
    
    @FXML
    private Button ajouterButton;
    
    private List<byte[]> selectedImages = new ArrayList<>();
    private final ServiceLocal serviceLocal = new ServiceLocal();
    
    @FXML
    private void initialize() {
        // Initialize any necessary components
    }
    
    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                byte[] imageData = Files.readAllBytes(selectedFile.toPath());
                selectedImages.add(imageData);
                
                // Show preview of the last added image
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de charger l'image.");
            }
        }
    }
    
    @FXML
    private void handleAjouter() {
        try {
            // Validate input fields
            if (!validateFields()) {
                return;
            }
            
            // Create new local object
            local newLocal = new local(
                0, // ID will be set by database
                Integer.parseInt(capaciteField.getText()),
                true, // Initially available
                Double.parseDouble(prixField.getText()),
                selectedImages,
                nomField.getText(),
                descriptionArea.getText(),
                adresseField.getText()
            );
            
            // Add local to database
            serviceLocal.ajouter(newLocal);
            
            // Refresh the list of locals
            ListeLocauxController listeController = ListeLocauxController.getInstance();
            if (listeController != null) {
                listeController.refreshLocaux();
            }
            
            // Show success message
            showAlert("Succès", "Local ajouté avec succès!");
            
            // Close the window
            ((Stage) ajouterButton.getScene().getWindow()).close();
            
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez vérifier les valeurs numériques (capacité et prix).");
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de l'ajout du local.");
            e.printStackTrace();
        }
    }
    
    private boolean validateFields() {
        if (nomField.getText().isEmpty() || adresseField.getText().isEmpty() ||
            capaciteField.getText().isEmpty() || prixField.getText().isEmpty() ||
            descriptionArea.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return false;
        }
        
        try {
            Integer.parseInt(capaciteField.getText());
            Double.parseDouble(prixField.getText());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La capacité et le prix doivent être des nombres valides.");
            return false;
        }
        
        return true;
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 