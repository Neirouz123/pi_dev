package controller;

import com.stripe.exception.StripeException;
import entities.Reservation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import services.ServiceReservation;
import services.StripePaymentService;
import test.Main;

import java.io.IOException;
import java.util.List;

public class PaymentController {

    @FXML
    private WebView webView;
    
    @FXML
    private VBox loadingIndicator;
    
    @FXML
    private VBox errorPane;
    
    @FXML
    private Label errorMessage;
    
    private List<Reservation> reservations;
    private StripePaymentService stripeService;
    private ServiceReservation reservationService;
    
    @FXML
    public void initialize() {
        // Initialize services
        stripeService = new StripePaymentService();
        reservationService = new ServiceReservation();
    }
    
    /**
     * Set the reservations to be paid for and load the Stripe checkout page
     * @param reservations List of reservations to pay for
     */
    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
        
        // Load the payment form
        loadStripeCheckout();
    }
    
    /**
     * Load the Stripe checkout in the WebView
     */
    private void loadStripeCheckout() {
        // Show loading indicator
        loadingIndicator.setVisible(true);
        webView.setVisible(false);
        errorPane.setVisible(false);
        
        // Create a background thread to load the checkout URL
        new Thread(() -> {
            try {
                // Get the checkout URL from Stripe
                String checkoutUrl = stripeService.checkout(reservations);
                
                // Load the URL in the WebView on the JavaFX Application Thread
                Platform.runLater(() -> {
                    WebEngine engine = webView.getEngine();
                    
                    // Add a state change listener to detect when payment is complete
                    engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                        // Check if the URL contains success or cancel parameters
                        String location = engine.getLocation();
                        if (location.contains("eventaplan.com/payment/success")) {
                            handlePaymentSuccess();
                        } else if (location.contains("eventaplan.com/payment/cancel")) {
                            handlePaymentCanceled();
                        }
                    });
                    
                    // Load the checkout URL
                    engine.load(checkoutUrl);
                    
                    // Hide loading indicator and show WebView
                    loadingIndicator.setVisible(false);
                    webView.setVisible(true);
                });
            } catch (StripeException e) {
                // Handle errors
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    errorPane.setVisible(true);
                    errorMessage.setText(e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Handle successful payment
     */
    private void handlePaymentSuccess() {
        try {
            // Get the user ID from the first reservation
            if (reservations != null && !reservations.isEmpty()) {
                int userId = reservations.get(0).getUtilisateurId();
                
                // Mark reservations as paid in the database
                for (Reservation reservation : reservations) {
                    // Set reservation as confirmed
                    reservationService.confirmerReservation(reservation.getId());
                }
                
                // Clear the cart (remove reservations from the cart, not from the system)
                reservationService.viderPanier(userId);
                
                System.out.println("✅ Payment successful for " + reservations.size() + " reservations - Cart cleared!");
            }
            
            // Show success message and return to the dashboard
            Platform.runLater(() -> {
                try {
                    // Show success alert before redirecting
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.INFORMATION,
                            "Votre paiement a été effectué avec succès! Vos réservations sont confirmées.",
                            javafx.scene.control.ButtonType.OK
                    );
                    alert.setTitle("Paiement réussi");
                    alert.setHeaderText("Merci pour votre commande!");
                    alert.showAndWait();

                    // Navigate back to dashboard
                    Parent root = FXMLLoader.load(Main.class.getResource("/Dashboard.fxml"));
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) webView.getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.out.println("❌ Error while processing successful payment: " + e.getMessage());
            e.printStackTrace();
            
            // Even if there's an error, try to navigate back
            Platform.runLater(this::retournerAuPanier);
        }
    }
    
    /**
     * Handle canceled payment
     */
    private void handlePaymentCanceled() {
        // Return to the cart
        retournerAuPanier();
    }
    
    /**
     * Return to the cart view
     */
    @FXML
    public void retournerAuPanier() {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource("/Panier.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) webView.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}