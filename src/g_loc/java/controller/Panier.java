package controller;

import entities.Reservation;
import entities.Utilisateur;
import entities.local;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ServiceLocal;
import services.ServiceReservation;

import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Panier {

    @FXML
    private Label cartItemsCount;
    @FXML
    private StackPane emptyCartPlaceholder;
    @FXML
    private ScrollPane cartItemsScroll;
    @FXML
    private VBox cartItemsList;
    @FXML
    private Label totalLabel;

    private ObservableList<Reservation> reservations = FXCollections.observableArrayList();
    private Utilisateur currentUser;
    private ServiceReservation serviceReservation = new ServiceReservation();
    private ServiceLocal serviceLocal = new ServiceLocal();

    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        System.out.println("Initializing Panier controller");

        // Setup scrollpane behavior
        cartItemsScroll.setFitToWidth(true);
        cartItemsScroll.setFitToHeight(false);
        cartItemsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cartItemsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Adjust scroll styling
        cartItemsScroll.getStyleClass().add("cart-scroll-pane");
        cartItemsScroll.setStyle("-fx-background-color: white; -fx-background: white; -fx-border-width: 0;");

        // Make sure the scroll bar is on the right
        cartItemsScroll.setPannable(false);
        cartItemsScroll.setPadding(new javafx.geometry.Insets(0, 0, 0, 0));

        // Set minimum height for cartItemsList
        cartItemsList.setMinHeight(100);
        cartItemsList.setPadding(new javafx.geometry.Insets(0, 5, 0, 0));

        // Ensure the scroll pane is properly styled
        cartItemsList.setStyle("-fx-background-color: white;");

        // Get the current user
        currentUser = getCurrentUser();
        if (currentUser == null) {
            // Redirect to login if no user is logged in
            redirectToLogin();
            return;
        }
        System.out.println("Current user: " + currentUser.getId() + " - " + currentUser.getUsername());

        // Load reservations for the current user
        loadUserReservations(currentUser.getId());

        // Update cart UI based on reservations
        updateCartView();

        // Force layout update after a short delay to ensure proper rendering
        javafx.application.Platform.runLater(this::forceUpdateLayout);

        // Debug visibility
        System.out.println("Empty placeholder visible: " + emptyCartPlaceholder.isVisible());
        System.out.println("Cart items scroll visible: " + cartItemsScroll.isVisible());
    }

    // Method to force layout updates
    private void forceUpdateLayout() {
        System.out.println("Forcing layout update");

        // Make sure the ScrollPane is visible if there are items
        if (!reservations.isEmpty()) {
            emptyCartPlaceholder.setVisible(false);
            cartItemsScroll.setVisible(true);

            // Calculate appropriate height
            int itemCount = (cartItemsList.getChildren().size() + 1) / 2; // Considering separators
            double heightPerItem = 100; // Height per item in pixels
            double totalHeight = Math.min(itemCount * heightPerItem, 400); // Cap at 400px

            // Set the height properties dynamically
            cartItemsScroll.setPrefHeight(totalHeight);
            cartItemsScroll.setMaxHeight(totalHeight);
            cartItemsScroll.setMinHeight(totalHeight);

            // Force layout recalculation
            cartItemsScroll.requestLayout();
            cartItemsList.requestLayout();

            System.out.println("Forced visibility and height: " + totalHeight + "px. Items: " + cartItemsList.getChildren().size());
        }
    }

    // Method to load reservations by the current user
    private void loadUserReservations(int userId) {
        try {
            reservations.clear();
            reservations.addAll(serviceReservation.getReservationsForUser(userId));
            System.out.println("Loaded " + reservations.size() + " reservations for user " + userId);

            // Update cart items count label
            updateCartItemsCount();
        } catch (Exception e) {
            System.out.println("Error loading reservations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to update the cart items count
    private void updateCartItemsCount() {
        int count = reservations.size();
        String text = "(" + count + " " + (count > 1 ? "RÉSERVATIONS" : "RÉSERVATION") + ")";
        cartItemsCount.setText(text);
    }

    // Method to update the cart view based on reservations
    private void updateCartView() {
        // Debug information
        System.out.println("Updating cart view with " + reservations.size() + " reservations");

        if (reservations.isEmpty()) {
            // Show empty cart placeholder
            emptyCartPlaceholder.setVisible(true);
            cartItemsScroll.setVisible(false);
            System.out.println("Cart is empty, showing placeholder");
        } else {
            // Hide empty placeholder and show cart items
            emptyCartPlaceholder.setVisible(false);
            cartItemsScroll.setVisible(true);
            System.out.println("Cart has items, showing " + reservations.size() + " items");

            // Clear previous items
            cartItemsList.getChildren().clear();

            // Add each reservation as a cart item
            for (Reservation reservation : reservations) {
                HBox itemView = createCartItemView(reservation);
                cartItemsList.getChildren().add(itemView);
                cartItemsList.getChildren().add(new Separator());
                System.out.println("Added reservation: " + reservation.getId());
            }

            // Remove the last separator to avoid extra space
            if (cartItemsList.getChildren().size() > 0) {
                cartItemsList.getChildren().remove(cartItemsList.getChildren().size() - 1);
            }

            // Make sure the cart list uses proper spacing
            cartItemsList.setSpacing(0);

            // Calculate and set the appropriate height for the scroll pane based on items
            int itemCount = (cartItemsList.getChildren().size() + 1) / 2; // Considering separators
            double heightPerItem = 100; // Height per item in pixels
            double totalHeight = Math.min(itemCount * heightPerItem, 400); // Cap at 400px

            // Set the height properties dynamically
            cartItemsScroll.setPrefHeight(totalHeight);
            cartItemsScroll.setMaxHeight(totalHeight);
            cartItemsScroll.setMinHeight(totalHeight);
            System.out.println("Setting scroll pane height to: " + totalHeight + "px for " + itemCount + " items");
        }

        // Update total
        updateTotal();
    }
    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) cartItemsList.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("EventaPlan - Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Method to create a cart item view for a reservation
    private HBox createCartItemView(Reservation reservation) {
        System.out.println("Creating cart item view for reservation: " + reservation.getId());

        // Container
        HBox itemContainer = new HBox();
        itemContainer.setAlignment(Pos.CENTER_LEFT);
        itemContainer.setPadding(new Insets(10, 15, 10, 5));
        itemContainer.setMinHeight(100);
        itemContainer.setMaxHeight(100);
        itemContainer.setStyle("-fx-background-color: white; -fx-border-color: #f0f0f0; -fx-border-width: 1; -fx-border-radius: 4;");

        // Get local details
        local venue = null;
        String localName = "Unknown Venue";
        try {
            venue = serviceLocal.rechercherLocalParId(reservation.getLocalId());
            if (venue != null) {
                localName = venue.getNom();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Image placeholder
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-min-width: 80; -fx-max-width: 80; -fx-min-height: 80; -fx-max-height: 80; -fx-background-color: #f5f5f5; -fx-background-radius: 4;");
        Label venueIcon = new Label("🏢");
        venueIcon.setStyle("-fx-font-size: 32; -fx-text-fill: #bdbdbd;");
        imageContainer.getChildren().add(venueIcon);

        // Details
        VBox detailsContainer = new VBox();
        detailsContainer.setSpacing(4);
        detailsContainer.setPadding(new Insets(0, 10, 0, 10));
        HBox.setHgrow(detailsContainer, Priority.ALWAYS);

        Label nameLabel = new Label(localName);
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #424242;");

        String description = (venue != null && venue.getDescription() != null && !venue.getDescription().isEmpty())
                ? venue.getDescription()
                : "Venue";
        Label descriptionLabel = new Label("Description : " + description);
        descriptionLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #757575; -fx-font-style: italic;");

        String formattedDate = (reservation.getDate() != null)
                ? reservation.getDate().format(dateFormatter)
                : "N/A";
        Label dateLabel = new Label("Date : " + formattedDate);
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #757575;");

        detailsContainer.getChildren().addAll(nameLabel, descriptionLabel, dateLabel);

        // Delete button
        Button deleteButton = new Button("×");
        deleteButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #F44336;" +
                        "-fx-font-size: 16;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );
        deleteButton.setOnAction(e -> deleteReservation(reservation.getId()));


        // Price container + actions
        VBox priceContainer = new VBox();
        priceContainer.setAlignment(Pos.CENTER_RIGHT);
        priceContainer.setSpacing(5);
        priceContainer.setMinWidth(120);
        priceContainer.setPadding(new Insets(0, 5, 0, 0));

        Label priceLabel = new Label(currencyFormatter.format(reservation.getPrix()));
        priceLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        Label stockLabel = new Label("EN STOCK");
        stockLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");

        HBox actionContainer = new HBox();
        actionContainer.setAlignment(Pos.CENTER_RIGHT);
        actionContainer.setSpacing(5);
        actionContainer.getChildren().addAll(deleteButton);

        priceContainer.getChildren().addAll(priceLabel, actionContainer, stockLabel);

        // Add all to container
        itemContainer.getChildren().addAll(imageContainer, detailsContainer, priceContainer);

        return itemContainer;
    }


    // Method to delete a reservation
    private void deleteReservation(int reservationId) {
        try {
            serviceReservation.supprimerReservationParId(reservationId);
            loadUserReservations(currentUser.getId());
            updateCartView();
            System.out.println("✅ Reservation removed successfully!");
        } catch (Exception ex) {
            System.out.println("❌ Failed to remove reservation: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Method to update the total price
    private void updateTotal() {
        double total = reservations.stream().mapToDouble(Reservation::getPrix).sum();
        totalLabel.setText(currencyFormatter.format(total));
    }

    // Method to clear the cart
    @FXML
    private void viderPanier() {
        try {
            // Delete reservations from the database for the current user
            serviceReservation.viderPanier(currentUser.getId());

            // Clear the list and update UI
            reservations.clear();
            updateCartView();

            System.out.println("✅ Cart cleared successfully!");
        } catch (SQLException ex) {
            System.out.println("❌ Failed to clear cart: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Method to proceed to payment
    @FXML
    private void passerAuPaiement() {
        if (reservations.isEmpty()) {
            System.out.println("Le panier est vide, impossible de procéder au paiement.");
            // Show an alert or message to inform the user
            return;
        }

        try {
            // Load the payment view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PaymentView.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the reservations
            PaymentController paymentController = loader.getController();
            paymentController.setReservations(reservations);

            // Show the payment view
            Scene scene = new Scene(root);
            Stage stage = (Stage) totalLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("EventaPlan - Paiement Sécurisé");
            stage.show();

            System.out.println("Opening payment view for " + reservations.size() + " reservations");
        } catch (IOException e) {
            System.out.println("Failed to open payment view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to explore other services
    @FXML
    private void explorerServices() {
        // Implement the navigation logic here (e.g., open a new view to browse services)
        System.out.println("Exploring services...");
    }

    // Placeholder method to get the current user (replace with actual user session or authentication logic)
    private Utilisateur getCurrentUser() {
        return UserSession.getCurrentUser();
    }

    @FXML
    private void Liste_Locaux() {
        try {
            // Load the Dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();

            // Get the controller and switch to venues list
            Dashboard dashboard = loader.getController();
            dashboard.Liste_Locaux();

            // Show the dashboard scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) totalLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("EventaPlan - Venue Management");
            stage.show();

            System.out.println("Navigated to venue listing");
        } catch (Exception e) {
            System.out.println("Failed to navigate to venue listing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
