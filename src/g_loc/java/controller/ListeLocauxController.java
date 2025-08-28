package controller;

import entities.local;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import services.ServiceLocal;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListeLocauxController implements Initializable {
    @FXML
    private TextField searchField;
    
    @FXML
    private GridPane locauxContainer;
    
    @FXML
    private Button filterCapaciteBtn;
    
    @FXML
    private Button filterDisponibiliteBtn;
    
    private final ServiceLocal serviceLocal = new ServiceLocal();
    private static ListeLocauxController instance;
    
    public ListeLocauxController() {
        instance = this;
    }
    
    public static ListeLocauxController getInstance() {
        return instance;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshLocaux();
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                refreshLocaux();
            } else {
                List<local> resultats = serviceLocal.rechercherLocal(newValue.trim());
                displayLocaux(resultats);
            }
        });
    }
    
    @FXML
    private void handleFilterCapacite() {
        List<local> locauxFiltres = serviceLocal.trierLocaux("capacite", true);
        displayLocaux(locauxFiltres);
    }
    
    @FXML
    private void handleFilterDisponibilite() {
        List<local> locauxFiltres = serviceLocal.trierLocaux("disponible", true);
        displayLocaux(locauxFiltres);
    }
    
    @FXML
    private void handleAjouterLocal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("AjouterLocal.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Ajouter un local");
            dialog.setDialogPane((DialogPane) loader.load());
            dialog.showAndWait();
            refreshLocaux();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void refreshLocaux() {
        List<local> locaux = serviceLocal.getAll(new local());
        displayLocaux(locaux);
    }
    
    private void displayLocaux(List<local> locaux) {
        locauxContainer.getChildren().clear();
        
        int col = 0;
        int row = 0;
        
        for (local l : locaux) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("LocalCard.fxml"));
                loader.load();
                
                LocalCardController controller = loader.getController();
                controller.setLocalDetails(l);
                
                locauxContainer.add(loader.getRoot(), col, row);
                
                col++;
                if (col == 3) {
                    col = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
} 