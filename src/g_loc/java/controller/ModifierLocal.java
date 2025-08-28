package controller;

import entities.local;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import services.ServiceLocal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModifierLocal {

    @FXML
    private TextField nomField, adresseField, capaciteField, prixField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private CheckBox disponibleCheckBox;
    @FXML
    private ImageView imageView;

    private local localDetails;
    private List<byte[]> newImages = new ArrayList<>();  // List to hold multiple images
    private final ServiceLocal serviceLocal = new ServiceLocal();

    public void setLocalDetails(local localDetails) {
        this.localDetails = localDetails;

        if (localDetails != null) {
            // Populate fields with existing details
            nomField.setText(localDetails.getNom());
            adresseField.setText(localDetails.getAdresse());
            descriptionArea.setText(localDetails.getDescription());
            capaciteField.setText(String.valueOf(localDetails.getCapacite()));
            disponibleCheckBox.setSelected(localDetails.isDisponible());
            prixField.setText(String.valueOf(localDetails.getPrix()));

            // Display existing images (if available)
            if (localDetails.getImages() != null && !localDetails.getImages().isEmpty()) {
                // Display the first image in the ImageView (could be enhanced for multiple image previews)
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(localDetails.getImages().get(0));
                Image image = new Image(byteArrayInputStream);
                imageView.setImage(image);
            }
        }
    }

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir des images");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
        // Allow multiple files to be selected
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(nomField.getScene().getWindow());

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (File selectedFile : selectedFiles) {
                try {
                    byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());
                    newImages.add(imageBytes);
                    // Preview the last selected image (optional, could show thumbnails or all selected)
                    Image image = new Image(new FileInputStream(selectedFile));
                    imageView.setImage(image);
                } catch (IOException e) {
                    showErrorAlert("Erreur lors du chargement de l'image", "Impossible de charger l'image. Veuillez réessayer.");
                }
            }
        }
    }

    @FXML
    private void enregistrerLocal() {
        // Validate form inputs
        if (nomField.getText().isEmpty() || adresseField.getText().isEmpty() || capaciteField.getText().isEmpty() || prixField.getText().isEmpty()) {
            showErrorAlert("Erreur de validation", "Tous les champs doivent être remplis.");
            return;
        }

        int capacite;
        double prix;

        try {
            capacite = Integer.parseInt(capaciteField.getText());
        } catch (NumberFormatException e) {
            showErrorAlert("Erreur de saisie", "La capacité doit être un nombre entier.");
            return;
        }

        try {
            prix = Double.parseDouble(prixField.getText());
        } catch (NumberFormatException e) {
            showErrorAlert("Erreur de saisie", "Le prix doit être un nombre valide.");
            return;
        }

        if (localDetails != null) {
            localDetails.setNom(nomField.getText());
            localDetails.setAdresse(adresseField.getText());
            localDetails.setDescription(descriptionArea.getText());
            localDetails.setCapacite(capacite);
            localDetails.setDisponible(disponibleCheckBox.isSelected());
            localDetails.setPrix(prix);
            if (!newImages.isEmpty()) {
                localDetails.setImages(newImages);  // Update the list of images
            }

            // Attempt to save the modified local
            serviceLocal.modifier(localDetails);
            showInfoAlert("Local Modifié", "Les informations du local ont été mises à jour avec succès.");
        }
    }

    @FXML
    private void annuler() {
        nomField.getScene().getWindow().hide();
    }

    @FXML
    public void supprimerLocal(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Voulez-vous vraiment supprimer ce local ?");
        alert.setContentText("Cette action est irréversible.");

        // Show the alert and wait for user response
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User confirmed, proceed with deletion
            serviceLocal.supprimer(localDetails.getId());
            showInfoAlert("Local Supprimé", "Le local a été supprimé avec succès.");
            annuler();  // Close the window after deletion
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
