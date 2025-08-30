package controller;

import entities.Reservation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import services.ServiceLocal;
import services.ServiceReservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;

public class Calendrier {
    private LocalDate selectedDate; // store the date selected by the user
    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    @FXML
    private AnchorPane calendarGrid;
    private int localId = -1;

    private LocalDate currentDate = LocalDate.now();
    private boolean[][] reservationStatus = new boolean[6][7];

    @FXML
    public void initialize() {
        if (localId != -1 && calendarGrid != null) {
            generateCalendar();
        }
    }

    public void setLocalId(int localId) {
        if (this.localId != localId) {
            this.localId = localId;
        }
    }

    public void generateCalendar() {
        if (localId == -1) return;

        // Clear previous buttons
        calendarGrid.getChildren().removeIf(node -> node instanceof Button);

        resetReservationStatus();
        loadReservations();

        int year = currentDate.getYear();
        Month month = currentDate.getMonth();
        int dayOfWeekStart = LocalDate.of(year, month, 1).getDayOfWeek().getValue() - 1;
        int daysInMonth = month.length(currentDate.isLeapYear());

        double startX = 20;
        double startY = 50;
        double buttonWidth = 60;
        double buttonHeight = 40;

        int row = 0;
        int col = dayOfWeekStart % 7;

        for (int day = 1; day <= daysInMonth; day++) {
            Button dayButton = createDayButton(day, startX, startY, buttonWidth, buttonHeight, row, col);
            updateButtonStyle(dayButton, row, col);
            setupButtonAction(dayButton, year, month, day);

            calendarGrid.getChildren().add(dayButton);

            if (++col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private Button createDayButton(int day, double x, double y, double w, double h, int row, int col) {
        Button btn = new Button(String.valueOf(day));
        btn.setMinSize(w, h);
        btn.setLayoutX(x + (col * w));
        btn.setLayoutY(y + (row * h));
        return btn;
    }

    private void updateButtonStyle(Button btn, int row, int col) {
        String style = reservationStatus[row][col] ?
                "-fx-background-color: #af1a1a; -fx-font-size: 14;" :
                "-fx-background-color: #559a55; -fx-font-size: 14;";
        btn.setStyle(style);
    }

    private void setupButtonAction(Button btn, int year, Month month, int day) {
        btn.setOnAction(event -> handleDaySelection(year, month, day));
    }

    private void handleDaySelection(int year, Month month, int day) {
        LocalDate selectedDate = LocalDate.of(year, month, day);
        ServiceReservation service = new ServiceReservation();
        ServiceLocal serviceLocal = new ServiceLocal();

        if (service.isDateReserved(localId, selectedDate)) {
            showAlert("Date Indisponible",
                    "Ce jour est déjà réservé !",
                    "Veuillez choisir une autre date.");
            return;
        }
        if (!UserSession.isLoggedIn()) {
            showAlert("Authentication Required",
                    "Please log in to make a reservation",
                    "You will be redirected to the login page.");
            redirectToLogin();
            return;
        }

        try {
            // Get the price from the local
            double prix = serviceLocal.getPrixById(localId);

            // Create the reservation with price
            Reservation reservation = new Reservation(
                    0,
                    localId,
                    UserSession.getCurrentUser().getId(),
                    selectedDate,
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0),
                    false, // confirmee
                    prix   // price from local
            );

            service.ajouterReservation(reservation);
            System.out.println("✔️ Réservation ajoutée au panier pour le " + selectedDate);

            // Open the Panier view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Panier.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) calendarGrid.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Panier - EventaPlan");
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ajouter la réservation", e.getMessage());
            e.printStackTrace();
        }
    }   private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) calendarGrid.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("EventaPlan - Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void resetReservationStatus() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                reservationStatus[i][j] = false;
            }
        }
    }

    private void loadReservations() {
        ServiceReservation service = new ServiceReservation();
        service.getReservationsForLocal(localId).forEach(res -> {
            LocalDate resDate = res.getDate();
            if (resDate.getMonth() == currentDate.getMonth() && resDate.getYear() == currentDate.getYear()) {
                int startDay = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), 1).getDayOfWeek().getValue() - 1;
                int position = startDay + resDate.getDayOfMonth() - 1;
                int row = position / 7;
                int col = position % 7;
                if (row < 6 && col < 7) {
                    reservationStatus[row][col] = true;
                }
            }
        });
    }
}
