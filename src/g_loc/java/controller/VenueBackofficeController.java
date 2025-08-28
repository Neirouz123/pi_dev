package controller;

import entities.local;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import services.ServiceLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VenueBackofficeController {

    @FXML
    private TableView<local> venueTable;
    
    @FXML
    private TableColumn<local, Integer> idColumn;
    
    @FXML
    private TableColumn<local, String> nameColumn;
    
    @FXML
    private TableColumn<local, String> addressColumn;
    
    @FXML
    private TableColumn<local, Integer> capacityColumn;
    
    @FXML
    private TableColumn<local, Double> priceColumn;
    
    @FXML
    private TableColumn<local, Boolean> availabilityColumn;
    
    @FXML
    private TableColumn<local, Void> actionsColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Pagination pagination;
    
    // Dialog components that will be created programmatically
    private Dialog<ButtonType> editDialog;
    private Dialog<ButtonType> deleteDialog;
    private TextField nameField;
    private TextField addressField;
    private TextArea descriptionField;
    private TextField capacityField;
    private TextField priceField;
    private CheckBox availableCheckbox;
    private TextField imagePathField;
    
    private ServiceLocal serviceLocal = new ServiceLocal();
    private ObservableList<local> allVenues = FXCollections.observableArrayList();
    private FilteredList<local> filteredVenues;
    private local currentVenue;
    private byte[] selectedImageData;
    private static final int ITEMS_PER_PAGE = 10;
    
    @FXML
    public void initialize() {
        // Initialize columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        availabilityColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isDisponible()));
        
        // Format the price column to show currency
        priceColumn.setCellFactory(tc -> new TableCell<local, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", price));
                }
            }
        });
        
        // Format the availability column to show Yes/No
        availabilityColumn.setCellFactory(tc -> new TableCell<local, Boolean>() {
            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                if (empty || available == null) {
                    setText(null);
                } else {
                    setText(available ? "Yes" : "No");
                    setStyle(available ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #F44336;");
                }
            }
        });
        
        // Setup actions column with edit and delete buttons
        setupActionsColumn();
        
        // Create dialogs programmatically
        createEditDialog();
        createDeleteDialog();
        
        // Load venues
        loadVenues();
        
        // Setup search functionality
        setupSearch();
        
        // Setup pagination
        setupPagination();
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<local, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, editButton, deleteButton);
            
            {
                // Style buttons
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                
                // Add actions
                editButton.setOnAction(event -> {
                    local venue = getTableRow().getItem();
                    if (venue != null) {
                        handleEditVenue(venue);
                    }
                });
                
                deleteButton.setOnAction(event -> {
                    local venue = getTableRow().getItem();
                    if (venue != null) {
                        handleDeleteVenue(venue);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }
    
    private void loadVenues() {
        try {
            allVenues.clear();
            List<local> venues = serviceLocal.getAll(new local());
            allVenues.addAll(venues);
            
            // Apply filter
            filteredVenues = new FilteredList<>(allVenues);
            updatePageCount();
            showPage(0);
            
            statusLabel.setText(venues.size() + " venues loaded successfully");
        } catch (Exception e) {
            statusLabel.setText("Error loading venues: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "Failed to load venues: " + e.getMessage());
        }
    }
    
    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue.toLowerCase().trim();
            
            filteredVenues.setPredicate(venue -> {
                if (searchText.isEmpty()) {
                    return true;
                }
                
                return venue.getNom().toLowerCase().contains(searchText) ||
                       venue.getAdresse().toLowerCase().contains(searchText) ||
                       venue.getDescription().toLowerCase().contains(searchText) ||
                       String.valueOf(venue.getId()).contains(searchText) ||
                       String.valueOf(venue.getCapacite()).contains(searchText) ||
                       String.valueOf(venue.getPrix()).contains(searchText);
            });
            
            updatePageCount();
            pagination.setCurrentPageIndex(0);
            showPage(0);
        });
    }
    
    private void setupPagination() {
        pagination.setPageFactory(this::createPage);
        updatePageCount();
    }
    
    private void updatePageCount() {
        int totalItems = filteredVenues.size();
        int pageCount = (totalItems + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE; // Ceiling division
        pageCount = Math.max(1, pageCount); // At least one page even if empty
        pagination.setPageCount(pageCount);
    }
    
    private TableView<local> createPage(int pageIndex) {
        showPage(pageIndex);
        return venueTable;
    }
    
    private void showPage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredVenues.size());
        
        if (filteredVenues.isEmpty()) {
            venueTable.setItems(FXCollections.observableArrayList());
        } else {
            venueTable.setItems(FXCollections.observableArrayList(
                    filteredVenues.subList(fromIndex, toIndex)));
        }
    }
    
    @FXML
    private void handleCreateVenue() {
        // Reset form fields
        nameField.setText("");
        addressField.setText("");
        descriptionField.setText("");
        capacityField.setText("");
        priceField.setText("");
        availableCheckbox.setSelected(true);
        imagePathField.setText("");
        selectedImageData = null;
        
        // Set current venue to null (new venue)
        currentVenue = null;
        
        // Show dialog
        editDialog.setTitle("Create New Venue");
        Optional<ButtonType> result = editDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            saveVenue();
        }
    }
    
    private void handleEditVenue(local venue) {
        // Set current venue
        currentVenue = venue;
        
        // Populate form fields
        nameField.setText(venue.getNom());
        addressField.setText(venue.getAdresse());
        descriptionField.setText(venue.getDescription());
        capacityField.setText(String.valueOf(venue.getCapacite()));
        priceField.setText(String.valueOf(venue.getPrix()));
        availableCheckbox.setSelected(venue.isDisponible());
        
        // No way to show current image path, just clear it
        imagePathField.setText("");
        selectedImageData = null;
        
        // Show dialog
        editDialog.setTitle("Edit Venue: " + venue.getNom());
        Optional<ButtonType> result = editDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            saveVenue();
        }
    }
    
    private void handleDeleteVenue(local venue) {
        // Set current venue
        currentVenue = venue;
        
        // Show confirmation dialog
        deleteDialog.setTitle("Delete Venue: " + venue.getNom());
        Optional<ButtonType> result = deleteDialog.showAndWait();
        
        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            deleteVenue();
        }
    }
    
    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Venue Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                selectedImageData = Files.readAllBytes(selectedFile.toPath());
                imagePathField.setText(selectedFile.getAbsolutePath());
            } catch (IOException e) {
                showError("Error", "Failed to read image file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void saveVenue() {
        try {
            // Get values from form
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String description = descriptionField.getText().trim();
            int capacity = Integer.parseInt(capacityField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            boolean available = availableCheckbox.isSelected();
            
            // Validate form
            if (name.isEmpty() || address.isEmpty() || description.isEmpty()) {
                showError("Validation Error", "Name, address, and description cannot be empty.");
                return;
            }
            
            if (capacity <= 0) {
                showError("Validation Error", "Capacity must be greater than 0.");
                return;
            }
            
            if (price <= 0) {
                showError("Validation Error", "Price must be greater than 0.");
                return;
            }
            
            // Prepare images list
            List<byte[]> images = new ArrayList<>();
            if (selectedImageData != null) {
                images.add(selectedImageData);
            } else if (currentVenue != null && currentVenue.getImages() != null && !currentVenue.getImages().isEmpty()) {
                // Keep existing images if no new image selected
                images = currentVenue.getImages();
            }
            
            // Create or update the venue
            if (currentVenue == null) {
                // Create new venue
                local newVenue = new local(
                        capacity,
                        available,
                        price,
                        images,
                        name,
                        description,
                        address
                );
                
                serviceLocal.ajouter(newVenue);
                statusLabel.setText("Venue created successfully");
            } else {
                // Update existing venue
                currentVenue.setNom(name);
                currentVenue.setAdresse(address);
                currentVenue.setDescription(description);
                currentVenue.setCapacite(capacity);
                currentVenue.setPrix(price);
                currentVenue.setDisponible(available);
                currentVenue.setImages(images);
                
                serviceLocal.modifier(currentVenue);
                statusLabel.setText("Venue updated successfully");
            }
            
            // Reload venues to refresh the table
            loadVenues();
            
        } catch (NumberFormatException e) {
            showError("Input Error", "Please enter valid numbers for capacity and price.");
        } catch (Exception e) {
            showError("Error", "Failed to save venue: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deleteVenue() {
        try {
            serviceLocal.supprimer(currentVenue.getId());
            loadVenues();
            statusLabel.setText("Venue deleted successfully");
        } catch (Exception e) {
            showError("Error", "Failed to delete venue: " + e.getMessage());
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
    
    // Create the edit dialog programmatically
    private void createEditDialog() {
        editDialog = new Dialog<>();
        editDialog.setTitle("Venue Details");
        
        // Create the dialog pane
        DialogPane dialogPane = new DialogPane();
        dialogPane.setPrefWidth(500);
        dialogPane.setPrefHeight(400);
        
        // Set button types
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        // Create form fields
        nameField = new TextField();
        addressField = new TextField();
        descriptionField = new TextArea();
        descriptionField.setPrefHeight(100);
        capacityField = new TextField();
        priceField = new TextField();
        availableCheckbox = new CheckBox();
        imagePathField = new TextField();
        imagePathField.setEditable(false);
        
        // Create browse button
        Button browseButton = new Button("Browse");
        browseButton.setOnAction(e -> handleBrowseImage());
        
        // Add components to grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);
        grid.add(new Label("Capacity:"), 0, 3);
        grid.add(capacityField, 1, 3);
        grid.add(new Label("Price:"), 0, 4);
        grid.add(priceField, 1, 4);
        grid.add(new Label("Available:"), 0, 5);
        grid.add(availableCheckbox, 1, 5);
        grid.add(new Label("Image:"), 0, 6);
        
        // Create HBox for image path and browse button
        HBox imageBox = new HBox(10);
        imageBox.getChildren().addAll(imagePathField, browseButton);
        HBox.setHgrow(imagePathField, Priority.ALWAYS);
        grid.add(imageBox, 1, 6);
        
        // Add grid to dialog
        dialogPane.setContent(grid);
        editDialog.setDialogPane(dialogPane);
    }
    
    // Create the delete confirmation dialog programmatically
    private void createDeleteDialog() {
        deleteDialog = new Dialog<>();
        deleteDialog.setTitle("Confirm Deletion");
        
        // Create the dialog pane
        DialogPane dialogPane = new DialogPane();
        
        // Add button types (CANCEL and custom DELETE button with OK_DONE button data)
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, deleteButtonType);
        
        // Create content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 20, 20, 20));
        
        Label warningLabel = new Label("Are you sure you want to delete this venue?");
        warningLabel.setStyle("-fx-font-weight: bold;");
        
        Label cautionLabel = new Label("This action cannot be undone.");
        cautionLabel.setStyle("-fx-text-fill: #F44336;");
        
        content.getChildren().addAll(warningLabel, cautionLabel);
        
        // Add content to dialog
        dialogPane.setContent(content);
        deleteDialog.setDialogPane(dialogPane);
    }
} 