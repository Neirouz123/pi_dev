package controller;

import entities.Reservation;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MyReservationsController {

    @FXML
    private TableView<Reservation> reservationsTable;

    @FXML
    private TableColumn<Reservation, Integer> idColumn;

    @FXML
    private TableColumn<Reservation, String> venueNameColumn;

    @FXML
    private TableColumn<Reservation, String> dateColumn;

    @FXML
    private TableColumn<Reservation, String> statusColumn;

    @FXML
    private TableColumn<Reservation, Double> priceColumn;

    @FXML
    private TableColumn<Reservation, Void> actionsColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private ComboBox<String> statusFilter;

    private ServiceReservation serviceReservation = new ServiceReservation();
    private ServiceLocal serviceLocal = new ServiceLocal();
    private ObservableList<Reservation> allReservations = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupStatusFilter();
        loadUserReservations();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Venue name column - lookup venue name by localId
        venueNameColumn.setCellValueFactory(cellData -> {
            try {
                local venue = serviceLocal.rechercherLocalParId(cellData.getValue().getLocalId());
                String venueName = venue != null ? venue.getNom() : "Unknown Venue";
                return new SimpleStringProperty(venueName);
            } catch (Exception e) {
                return new SimpleStringProperty("Error loading venue");
            }
        });

        // Date column
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDate().format(dateFormatter));
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
            private final Button cancelButton = new Button("Cancel");
            private final HBox pane = new HBox(5, cancelButton);

            {
                cancelButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");

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
                        cancelButton.setDisable(true);
                        cancelButton.setText("Confirmed");
                        cancelButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupStatusFilter() {
        statusFilter.getItems().addAll("All", "Pending", "Confirmed");
        statusFilter.setValue("All");

        statusFilter.setOnAction(e -> filterReservations());
    }

    private void loadUserReservations() {
        if (!UserSession.isLoggedIn()) {
            return;
        }

        try {
            allReservations.clear();
            List<Reservation> reservations = serviceReservation.getReservationsForUser(
                    UserSession.getCurrentUser().getId()
            );
            allReservations.addAll(reservations);
            filterReservations();
            statusLabel.setText(reservations.size() + " reservations found");
        } catch (Exception e) {
            statusLabel.setText("Error loading reservations: " + e.getMessage());
            showError("Database Error", "Failed to load reservations: " + e.getMessage());
        }
    }

    private void filterReservations() {
        String selectedStatus = statusFilter.getValue();

        if ("All".equals(selectedStatus)) {
            reservationsTable.setItems(allReservations);
        } else {
            ObservableList<Reservation> filtered = FXCollections.observableArrayList();
            boolean showConfirmed = "Confirmed".equals(selectedStatus);

            for (Reservation reservation : allReservations) {
                if (reservation.isConfirmee() == showConfirmed) {
                    filtered.add(reservation);
                }
            }
            reservationsTable.setItems(filtered);
        }
    }

    private void handleCancelReservation(Reservation reservation) {
        if (reservation.isConfirmee()) {
            showError("Cannot Cancel", "Confirmed reservations cannot be cancelled.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Reservation");
        confirmAlert.setHeaderText("Cancel reservation #" + reservation.getId());
        confirmAlert.setContentText("Are you sure you want to cancel this reservation?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceReservation.supprimerReservationParId(reservation.getId());
                loadUserReservations();
                statusLabel.setText("Reservation cancelled successfully");
            } catch (Exception e) {
                showError("Error", "Failed to cancel reservation: " + e.getMessage());
            }
        }
    }

    @FXML
    private void refreshReservations() {
        loadUserReservations();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}