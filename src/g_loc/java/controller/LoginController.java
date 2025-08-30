package controller;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceUtilisateur;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label messageLabel;

    @FXML
    private ComboBox<String> roleComboBox;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();

    @FXML
    public void initialize() {
        // Initialize role selection
        roleComboBox.getItems().addAll("CLIENT", "ADMIN");
        roleComboBox.setValue("CLIENT"); // Default to client

        // Set up form validation
        setupFormValidation();
    }

    private void setupFormValidation() {
        // Enable/disable login button based on form completion
        loginButton.disableProperty().bind(
                usernameField.textProperty().isEmpty()
                        .or(passwordField.textProperty().isEmpty())
        );
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String selectedRole = roleComboBox.getValue();

        try {
            // Attempt to authenticate user
            Utilisateur user = serviceUtilisateur.authenticate(username, password);

            if (user != null) {
                // Check if user role matches selected role
                if (!user.getRole().equalsIgnoreCase(selectedRole)) {
                    showMessage("Invalid role selected for this account.", "error");
                    return;
                }

                // Store current user session
                UserSession.setCurrentUser(user);

                // Navigate to appropriate dashboard based on role
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    openAdminDashboard();
                } else {
                    openClientDashboard();
                }

                // Close login window
                Stage loginStage = (Stage) loginButton.getScene().getWindow();
                loginStage.close();

            } else {
                showMessage("Invalid username or password.", "error");
            }

        } catch (Exception e) {
            showMessage("Login failed: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String selectedRole = roleComboBox.getValue();

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please fill in all fields.", "error");
            return;
        }

        if (password.length() < 6) {
            showMessage("Password must be at least 6 characters long.", "error");
            return;
        }

        try {
            // Check if username already exists
            if (serviceUtilisateur.usernameExists(username)) {
                showMessage("Username already exists. Please choose another.", "error");
                return;
            }

            // Create new user
            Utilisateur newUser = new Utilisateur();
            newUser.setUsername(username);
            newUser.setPassword(password); // In real app, hash this password
            newUser.setRole(selectedRole);

            // Register user
            serviceUtilisateur.register(newUser);

            showMessage("Registration successful! Please login with your credentials.", "success");

            // Clear form
            clearForm();

        } catch (Exception e) {
            showMessage("Registration failed: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    private void openAdminDashboard() {
        try {
            // Try multiple resource loading approaches
            URL resource = loadFXMLResource("AdminDashboard.fxml");
            if (resource == null) {
                // Fallback to existing Dashboard if AdminDashboard not found
                resource = loadFXMLResource("Dashboard.fxml");
            }

            if (resource == null) {
                showMessage("Could not find admin dashboard file.", "error");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("EventaPlan - Admin Dashboard");
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();

            System.out.println("Admin dashboard opened successfully");

        } catch (IOException e) {
            showMessage("Failed to open admin dashboard: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    private void openClientDashboard() {
        try {
            URL resource = loadFXMLResource("ClientDashboard.fxml");
            if (resource == null) {
                // Fallback to existing Dashboard if ClientDashboard not found
                resource = loadFXMLResource("Dashboard.fxml");
            }

            if (resource == null) {
                showMessage("Could not find client dashboard file.", "error");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("EventaPlan - Book Venues");
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();

            System.out.println("Client dashboard opened successfully");


        } catch (IOException e) {
            showMessage("Failed to open client dashboard: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }
    private URL loadFXMLResource(String fileName) {
        // Try different resource loading approaches
        URL resource = null;

        // Method 1: Class loader resource
        resource = getClass().getClassLoader().getResource(fileName);
        if (resource != null) {
            System.out.println("Found " + fileName + " using ClassLoader");
            return resource;
        }

        // Method 2: Class resource with leading slash
        resource = getClass().getResource("/" + fileName);
        if (resource != null) {
            System.out.println("Found " + fileName + " using Class resource with /");
            return resource;
        }

        // Method 3: Class resource without leading slash
        resource = getClass().getResource(fileName);
        if (resource != null) {
            System.out.println("Found " + fileName + " using Class resource");
            return resource;
        }

        // Method 4: Try in controller package
        resource = getClass().getResource("/controller/" + fileName);
        if (resource != null) {
            System.out.println("Found " + fileName + " in controller package");
            return resource;
        }

        System.out.println("Could not find " + fileName + " in any location");
        return null;
    }



    private void showMessage(String message, String type) {
        messageLabel.setText(message);

        if ("error".equals(type)) {
            messageLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
        } else if ("success".equals(type)) {
            messageLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
        }
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        messageLabel.setText("");
    }
}