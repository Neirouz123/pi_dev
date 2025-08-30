package controller;

import entities.Reservation;
import entities.Utilisateur;
import entities.local;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.ServiceLocal;
import services.ServiceReservation;
import services.ServiceUtilisateur;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ReservationManagementController {

    @FXML
    private TableView<Reservation> reservationsTable;

    @FXML
    private TableColumn<Reservation, Integer> idColumn;

    @FXML
    private TableColumn<Reservation, String> userColumn;

    @FXML
    private TableColumn<Reservation, String> venueColumn;

    @FXML
    private TableColumn<Reservation, String> dateColumn;

    @FXML
    private TableColumn<Reservation, String> timeColumn;

    @FXML
    private TableColumn<Reservation, String> statusColumn;

    @FXML
    private TableColumn<Reservation, Double> priceColumn;

    @FXML
    private TableColumn<Reservation, Void> actionsColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label statusLabel;

    private ServiceReservation serviceReservation = new ServiceReservation();
    private ServiceLocal serviceLocal = new ServiceLocal();
    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private ObservableList<Reservation> allReservations = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadAllReservations();
        setupSearchAndFilters();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        // User column - lookup username by userId
        userColumn.setCellValueFactory(cellData -> {
            try {
                // You'll need to implement getUserById in ServiceUtilisateur
                Utilisateur user = serviceUtilisateur.getUserById(cellData.getValue().getUtilisateurId());
                String username = user != null ? user.getUsername() : "Unknown User";
                return new SimpleStringProperty(username);
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });

        // Venue column - lookup venue name by localId
        venueColumn.setCellValueFactory(cellData -> {
            try {
                local venue = serviceLocal.rechercherLocalParId(cellData.getValue().getLocalId());
                String venueName = venue != null ? venue.getNom() : "Unknown Venue";
                return new SimpleStringProperty(venueName);
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });

        // Date column
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDate().format(dateFormatter));
            }
            return new SimpleStringProperty("-");
        });

        // Time column - combine start and end time
        timeColumn.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            if (reservation.getHeureDebut() != null && reservation.getHeureFin() != null) {
                String timeRange = reservation.getHeureDebut().format(timeFormatter) +
                        " - " + reservation.getHeureFin().format(timeFormatter);
                return new SimpleStringProperty(timeRange);
            }
            return new SimpleStringProperty("-");
        });

        // Status column
        statusColumn.setCellValueFactory(cellData -> {
            boolean confirmed = cellData.getValue().isConfirmee();
            return new SimpleStringProperty(confirmed ? "Confirmed" : "Pending");
        });

        // Price column
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        priceColumn.setCellFactory(tc -> new TableCell<Reservation, Double>() {
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

        // Status column styling
        statusColumn.setCellFactory(tc -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("Confirmed".equals(status)) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                    }
                }
            }
        });

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<Reservation, Void>() {
            private final Button confirmButton = new Button("Confirm");
            private final Button cancelButton = new Button("Cancel");
            private final HBox pane = new HBox(5, confirmButton, cancelButton);

            {
                confirmButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 4 8;");
                cancelButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 4 8;");

                confirmButton.setOnAction(event -> {
                    Reservation reservation = getTableRow().getItem();
                    if (reservation != null) {
                        handleConfirmReservation(reservation);
                    }
                });

                cancelButton.setOnAction(event -> {
                    Reservation reservation = getTableRow().getItem();
                    if (reservation != null) {
                        handleCancelReservation(reservation);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableRow().getItem();
                    if (reservation != null && reservation.isConfirmee()) {
                        confirmButton.setDisable(true);
                        confirmButton.setText("Confirmed");
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupFilters() {
        statusFilter.getItems().addAll("All", "Pending", "Confirmed");
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> applyFilters());

        startDatePicker.setOnAction(e -> applyFilters());
        endDatePicker.setOnAction(e -> applyFilters());
    }

    private void setupSearchAndFilters() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void loadAllReservations() {
        try {
            allReservations.clear();
            List<Reservation> reservations = serviceReservation.getAllReservations();
            allReservations.addAll(reservations);
            applyFilters();
            statusLabel.setText(reservations.size() + " total reservations");
        } catch (Exception e) {
            statusLabel.setText("Error loading reservations: " + e.getMessage());
            showError("Database Error", "Failed to load reservations: " + e.getMessage());
        }
    }

    private void applyFilters() {
        ObservableList<Reservation> filtered = FXCollections.observableArrayList();
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedStatus = statusFilter.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        for (Reservation reservation : allReservations) {
            // Text search filter
            boolean matchesSearch = searchText.isEmpty() ||
                    String.valueOf(reservation.getId()).contains(searchText) ||
                    String.valueOf(reservation.getUtilisateurId()).contains(searchText) ||
                    String.valueOf(reservation.getLocalId()).contains(searchText);

            // Status filter
            boolean matchesStatus = "All".equals(selectedStatus) ||
                    ("Confirmed".equals(selectedStatus) && reservation.isConfirmee()) ||
                    ("Pending".equals(selectedStatus) && !reservation.isConfirmee());

            // Date range filter
            boolean matchesDateRange = true;
            if (startDate != null && reservation.getDate().isBefore(startDate)) {
                matchesDateRange = false;
            }
            if (endDate != null && reservation.getDate().isAfter(endDate)) {
                matchesDateRange = false;
            }

            if (matchesSearch && matchesStatus && matchesDateRange) {
                filtered.add(reservation);
            }
        }

        reservationsTable.setItems(filtered);
        statusLabel.setText("Showing " + filtered.size() + " of " + allReservations.size() + " reservations");
    }

    private void handleConfirmReservation(Reservation reservation) {
        if (reservation.isConfirmee()) {
            showInfo("Already Confirmed", "This reservation is already confirmed.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Reservation");
        confirmAlert.setHeaderText("Confirm reservation #" + reservation.getId());
        confirmAlert.setContentText("Are you sure you want to confirm this reservation?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceReservation.confirmerReservation(reservation.getId());
                reservation.setConfirmee(true); // Update local object
                reservationsTable.refresh();
                statusLabel.setText("Reservation confirmed successfully");
            } catch (Exception e) {
                showError("Error", "Failed to confirm reservation: " + e.getMessage());
            }
        }
    }

    private void handleCancelReservation(Reservation reservation) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Reservation");
        confirmAlert.setHeaderText("Cancel reservation #" + reservation.getId());
        confirmAlert.setContentText("Are you sure you want to cancel this reservation? This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceReservation.supprimerReservationParId(reservation.getId());
                allReservations.remove(reservation);
                applyFilters();
                statusLabel.setText("Reservation cancelled successfully");
            } catch (Exception e) {
                showError("Error", "Failed to cancel reservation: " + e.getMessage());
            }
        }
    }

    @FXML
    private void refreshData() {
        loadAllReservations();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}