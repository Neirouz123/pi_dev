// Calendrier.java
package controller;

import entities.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import services.ServiceReservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

public class Calendrier {
    @FXML
    private AnchorPane calendarGrid;
    private int localId=-1;

    private LocalDate currentDate = LocalDate.now();
    private boolean[][] reservationStatus = new boolean[6][7];

    @FXML
    public void initialize() {
        if (localId != -1 && calendarGrid != null) {
            generateCalendar();
        }
    }
    public void setLocalId(int localId) {
        if (this.localId != localId) { // Évite les mises à jour inutiles
            this.localId = localId;
        }
    }


    public void generateCalendar() {
        if (localId == -1) return;

        // Nettoyer les anciens boutons (sauf le bouton Confirmer si existant)
        calendarGrid.getChildren().removeIf(node -> node instanceof Button && !"confirmerButton".equals(((Button) node).getId()));

        // Réinitialiser et charger les réservations
        resetReservationStatus();
        loadReservations();

        int year = currentDate.getYear();
        Month month = currentDate.getMonth();
        int dayOfWeekStart = LocalDate.of(year, month, 1).getDayOfWeek().getValue() - 1; // Correction ici
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

        if (service.isDateReserved(localId, selectedDate)) {
            showAlert("Date Indisponible",
                    "Ce jour est déjà réservé !",
                    "Veuillez choisir une autre date.");
            return;
        }

        Reservation reservation = new Reservation(
                0,
                this.localId, // ID local
                1, // ID utilisateur
                selectedDate,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                false
        );

        service.ajouterReservation(reservation);
        generateCalendar();
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
        List<Reservation> reservations = service.getReservationsForLocal(localId);
        int currentYear = currentDate.getYear();
        Month currentMonth = currentDate.getMonth();
        int startDay = LocalDate.of(currentYear, currentMonth, 1).getDayOfWeek().getValue() - 1;

        for (Reservation res : reservations) {
            LocalDate resDate = res.getDate();
            if (resDate.getMonth() == currentMonth && resDate.getYear() == currentYear) {
                int day = resDate.getDayOfMonth();
                int position = startDay + day - 1;
                int row = position / 7;
                int col = position % 7;

                if (row < 6 && col < 7) {
                    reservationStatus[row][col] = true;
                }
            }
        }
    }
}