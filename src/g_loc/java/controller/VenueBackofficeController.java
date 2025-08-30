package controller;

import entities.local;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
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
        editDialog.setTitle("🏢 Venue Management");

        // Create the dialog pane with enhanced styling
        DialogPane dialogPane = new DialogPane();
        dialogPane.setPrefWidth(650);
        dialogPane.setPrefHeight(600);
        dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom, #f8fafc, #e2e8f0); -fx-background-radius: 12;");

        // Set button types
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style the buttons
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);

        okButton.setText("💾 Save Venue");
        okButton.setStyle("-fx-background-color: linear-gradient(to bottom, #48bb78, #38a169); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 20; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");

        cancelButton.setText("❌ Cancel");
        cancelButton.setStyle("-fx-background-color: linear-gradient(to bottom, #e53e3e, #c53030); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 20; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");

        // Create main container
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Create header
        VBox header = new VBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER);

        Label titleLabel = new Label("✨ Venue Information");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #1a202c; " +
                "-fx-font-family: 'Segoe UI', Arial, sans-serif;");

        Separator headerSeparator = new Separator();
        headerSeparator.setMaxWidth(300);
        headerSeparator.setStyle("-fx-background-color: #4299e1;");

        Label subtitleLabel = new Label("Fill in the details below to manage your venue");
        subtitleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b; -fx-font-style: italic;");

        header.getChildren().addAll(titleLabel, headerSeparator, subtitleLabel);

        // Create form grid with enhanced styling
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(18);
        grid.setPadding(new Insets(20, 0, 0, 0));

        // Set column constraints for better layout
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(120);
        col1.setPrefWidth(150);
        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        // Create form fields with enhanced styling
        nameField = new TextField();
        nameField.setPromptText("Enter venue name");
        nameField.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-pref-height: 45;");

        addressField = new TextField();
        addressField.setPromptText("Enter complete address");
        addressField.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-pref-height: 45;");

        descriptionField = new TextArea();
        descriptionField.setPromptText("Detailed description of the venue...");
        descriptionField.setPrefHeight(120);
        descriptionField.setWrapText(true);
        descriptionField.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 8; -fx-padding: 12; -fx-font-size: 14;");

        capacityField = new TextField();
        capacityField.setPromptText("Number of people");
        capacityField.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-pref-height: 45;");

        priceField = new TextField();
        priceField.setPromptText("Price per day (DT)");
        priceField.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-pref-height: 45;");

        availableCheckbox = new CheckBox();
        availableCheckbox.setText("Currently available for booking");
        availableCheckbox.setStyle("-fx-font-size: 14; -fx-text-fill: #2d3748;");

        imagePathField = new TextField();
        imagePathField.setPromptText("No image selected");
        imagePathField.setEditable(false);
        imagePathField.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-pref-height: 45;");

        // Create browse button with enhanced styling
        Button browseButton = new Button("📁 Browse");
        browseButton.setOnAction(e -> handleBrowseImage());
        browseButton.setStyle("-fx-background-color: linear-gradient(to bottom, #4299e1, #3182ce); " +
                "-fx-text-fill: white; -fx-padding: 12 16; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");

        // Create styled labels
        Label nameLabel = new Label("🏢 Name:");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #2d3748;");

        Label addressLabel = new Label("📍 Address:");
        addressLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #2d3748;");

        Label descriptionLabel = new Label("📝 Description:");
        descriptionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #2d3748;");
        descriptionLabel.setAlignment(javafx.geometry.Pos.TOP_LEFT);

        Label capacityLabel = new Label("👥 Capacity:");
        capacityLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #2d3748;");

        Label priceLabel = new Label("💰 Price:");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #2d3748;");

        Label availableLabel = new Label("✅ Availability:");
        availableLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #2d3748;");

        Label imageLabel = new Label("📸 Image:");
        imageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #2d3748;");

        // Add components to grid
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(addressLabel, 0, 1);
        grid.add(addressField, 1, 1);

        grid.add(descriptionLabel, 0, 2);
        grid.add(descriptionField, 1, 2);
        GridPane.setValignment(descriptionLabel, javafx.geometry.VPos.TOP);
        GridPane.setMargin(descriptionLabel, new Insets(8, 0, 0, 0));

        grid.add(capacityLabel, 0, 3);
        grid.add(capacityField, 1, 3);

        grid.add(priceLabel, 0, 4);
        grid.add(priceField, 1, 4);

        grid.add(availableLabel, 0, 5);
        grid.add(availableCheckbox, 1, 5);

        grid.add(imageLabel, 0, 6);

        // Create HBox for image path and browse button with enhanced styling
        HBox imageBox = new HBox(12);
        imageBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        imageBox.getChildren().addAll(imagePathField, browseButton);
        HBox.setHgrow(imagePathField, Priority.ALWAYS);
        grid.add(imageBox, 1, 6);

        // Add separator before buttons
        Separator bottomSeparator = new Separator();
        bottomSeparator.setStyle("-fx-background-color: #e2e8f0;");
        bottomSeparator.setMaxWidth(Double.MAX_VALUE);

        // Add all components to main container
        mainContainer.getChildren().addAll(header, grid);

        // Add main container to dialog
        dialogPane.setContent(mainContainer);
        editDialog.setDialogPane(dialogPane);
    }

    // Create the delete confirmation dialog programmatically with enhanced styling
    private void createDeleteDialog() {
        deleteDialog = new Dialog<>();
        deleteDialog.setTitle("⚠️ Confirm Deletion");

        // Create the dialog pane with enhanced styling
        DialogPane dialogPane = new DialogPane();
        dialogPane.setPrefWidth(500);
        dialogPane.setPrefHeight(300);
        dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom, #fed7d7, #feb2b2); -fx-background-radius: 12;");

        // Add button types (CANCEL and custom DELETE button with OK_DONE button data)
        ButtonType deleteButtonType = new ButtonType("🗑️ Delete", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, deleteButtonType);

        // Style the buttons
        Button deleteButton = (Button) dialogPane.lookupButton(deleteButtonType);
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);

        deleteButton.setStyle("-fx-background-color: linear-gradient(to bottom, #e53e3e, #c53030); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 20; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 3);");

        cancelButton.setText("↩️ Cancel");
        cancelButton.setStyle("-fx-background-color: linear-gradient(to bottom, #718096, #4a5568); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 20; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");

        // Create content container
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Create warning icon (using text as icon)
        Label iconLabel = new Label("⚠️");
        iconLabel.setStyle("-fx-font-size: 48; -fx-text-fill: #e53e3e;");

        // Create warning message
        Label warningLabel = new Label("Are you sure you want to delete this venue?");
        warningLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: #1a202c; " +
                "-fx-text-alignment: center;");
        warningLabel.setWrapText(true);
        warningLabel.setMaxWidth(400);

        // Create caution message
        Label cautionLabel = new Label("This action cannot be undone. All associated data will be permanently removed.");
        cautionLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14; -fx-text-alignment: center; " +
                "-fx-font-style: italic;");
        cautionLabel.setWrapText(true);
        cautionLabel.setMaxWidth(400);

        // Create separator
        Separator separator = new Separator();
        separator.setMaxWidth(300);
        separator.setStyle("-fx-background-color: #e2e8f0;");

        // Add all elements to content
        content.getChildren().addAll(iconLabel, warningLabel, separator, cautionLabel);

        // Add content to dialog
        dialogPane.setContent(content);
        deleteDialog.setDialogPane(dialogPane);
    }
} 