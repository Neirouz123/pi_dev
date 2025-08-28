package controller;

import entities.local;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import services.ServiceLocal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AjouterLocal {
    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtAdresse;
    @FXML
    private TextField txtCapacite;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtPrix;
    @FXML
    private Button btnChoisirImage;
    @FXML
    private GridPane imagesGrid;

    private final List<byte[]> imagesList = new ArrayList<>();
    private final ServiceLocal serviceLocal = new ServiceLocal();

    @FXML
    public void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir des images");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(btnChoisirImage.getScene().getWindow());

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            imagesGrid.getChildren().clear();
            imagesList.clear();

            int row = 0;
            int col = 0;

            for (File file : selectedFiles) {
                try {
                    byte[] imageData = Files.readAllBytes(file.toPath());
                    imagesList.add(imageData);

                    // Création de la miniature
                    ImageView imageView = new ImageView(new Image(new FileInputStream(file)));
                    imageView.setFitWidth(80);
                    imageView.setFitHeight(80);
                    imageView.setPreserveRatio(true);

                    imagesGrid.add(imageView, col, row);
                    col++;

                    if (col > 2) { // 3 colonnes par ligne
                        col = 0;
                        row++;
                    }
                } catch (IOException e) {
                    showAlert("Erreur", "Impossible de charger l'image : " + file.getName(), Alert.AlertType.ERROR);
                }
            }
        }
    }

    @FXML
    public void ajouterLocal() {
        if (!validateFields()) {
            showAlert("Champs invalides", "Tous les champs obligatoires doivent être remplis", Alert.AlertType.WARNING);
            return;
        }

        try {
            local newLocal = new local();
            newLocal.setNom(txtNom.getText());
            newLocal.setAdresse(txtAdresse.getText());
            newLocal.setCapacite(Integer.parseInt(txtCapacite.getText()));
            newLocal.setPrix(Double.parseDouble(txtPrix.getText()));
            newLocal.setDescription(txtDescription.getText());
            newLocal.setImages(imagesList); // Set liste d'images

            serviceLocal.ajouter(newLocal);

            showAlert("Succès", "Local ajouté avec " + imagesList.size() + " images", Alert.AlertType.INFORMATION);
            clearFields();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format numérique invalide", Alert.AlertType.ERROR);
        }
    }

    private boolean validateFields() {
        return !txtNom.getText().isEmpty() &&
                !txtAdresse.getText().isEmpty() &&
                !txtCapacite.getText().isEmpty() &&
                !txtPrix.getText().isEmpty() &&
                !imagesList.isEmpty();
    }

    private void clearFields() {
        txtNom.clear();
        txtAdresse.clear();
        txtCapacite.clear();
        txtPrix.clear();
        txtDescription.clear();
        imagesList.clear();
        imagesGrid.getChildren().clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}