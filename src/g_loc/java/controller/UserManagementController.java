package controller;

import entities.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import services.ServiceUtilisateur;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class UserManagementController {

    @FXML
    private TableView<Utilisateur> usersTable;

    @FXML
    private TableColumn<Utilisateur, Integer> idColumn;

    @FXML
    private TableColumn<Utilisateur, String> usernameColumn;

    @FXML
    private TableColumn<Utilisateur, String> roleColumn;

    @FXML
    private TableColumn<Utilisateur, String> emailColumn;

    @FXML
    private TableColumn<Utilisateur, String> createdAtColumn;

    @FXML
    private TableColumn<Utilisateur, Void> actionsColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private ObservableList<Utilisateur> allUsers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadUsers();
        setupSearch();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Format created date
        createdAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                String formattedDate = cellData.getValue().getCreatedAt()
                        .toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                return new SimpleStringProperty(formattedDate);
            }
            return new SimpleStringProperty("-");
        });

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        Callback<TableColumn<Utilisateur, Void>, TableCell<Utilisateur, Void>> cellFactory =
                new Callback<TableColumn<Utilisateur, Void>, TableCell<Utilisateur, Void>>() {
                    @Override
                    public TableCell<Utilisateur, Void> call(final TableColumn<Utilisateur, Void> param) {
                        final TableCell<Utilisateur, Void> cell = new TableCell<Utilisateur, Void>() {

                            private final Button editButton = new Button("Edit Role");
                            private final Button deleteButton = new Button("Delete");
                            private final HBox pane = new HBox(5, editButton, deleteButton);

                            {
                                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
                                deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");

                                editButton.setOnAction(event -> {
                                    Utilisateur user = getTableView().getItems().get(getIndex());
                                    if (user != null) {
                                        handleEditUserRole(user);
                                    }
                                });

                                deleteButton.setOnAction(event -> {
                                    Utilisateur user = getTableView().getItems().get(getIndex());
                                    if (user != null) {
                                        handleDeleteUser(user);
                                    }
                                });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    Utilisateur user = getTableView().getItems().get(getIndex());
                                    // Don't allow admin to delete themselves
                                    if (user != null && user.getId() == UserSession.getCurrentUser().getId()) {
                                        deleteButton.setDisable(true);
                                        deleteButton.setText("Current User");
                                    }
                                    setGraphic(pane);
                                }
                            }
                        };
                        return cell;
                    }
                };

        actionsColumn.setCellFactory(cellFactory);
    }

    private void loadUsers() {
        try {
            allUsers.clear();
            List<Utilisateur> users = serviceUtilisateur.getAllUsers();
            allUsers.addAll(users);
            usersTable.setItems(allUsers);
            statusLabel.setText(users.size() + " users loaded");
        } catch (SQLException e) {
            statusLabel.setText("Error loading users: " + e.getMessage());
            showError("Database Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                usersTable.setItems(allUsers);
            } else {
                ObservableList<Utilisateur> filteredUsers = FXCollections.observableArrayList();
                String searchText = newValue.toLowerCase().trim();

                for (Utilisateur user : allUsers) {
                    if (user.getUsername().toLowerCase().contains(searchText) ||
                            user.getRole().toLowerCase().contains(searchText) ||
                            (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText))) {
                        filteredUsers.add(user);
                    }
                }
                usersTable.setItems(filteredUsers);
            }
        });
    }

    private void handleEditUserRole(Utilisateur user) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(user.getRole(), "CLIENT", "ADMIN");
        dialog.setTitle("Edit User Role");
        dialog.setHeaderText("Change role for user: " + user.getUsername());
        dialog.setContentText("Select new role:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newRole -> {
            if (!newRole.equals(user.getRole())) {
                try {
                    serviceUtilisateur.updateUserRole(user.getId(), newRole);
                    user.setRole(newRole); // Update local object
                    usersTable.refresh();
                    statusLabel.setText("User role updated successfully");
                } catch (SQLException e) {
                    showError("Update Error", "Failed to update user role: " + e.getMessage());
                }
            }
        });
    }

    private void handleDeleteUser(Utilisateur user) {
        // Prevent admin from deleting themselves
        if (user.getId() == UserSession.getCurrentUser().getId()) {
            showError("Operation Not Allowed", "You cannot delete your own account.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete user: " + user.getUsername());
        confirmAlert.setContentText("This action cannot be undone. Are you sure?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceUtilisateur.deleteUser(user.getId());
                allUsers.remove(user);
                usersTable.refresh();
                statusLabel.setText("User deleted successfully");
            } catch (SQLException e) {
                showError("Delete Error", "Failed to delete user: " + e.getMessage());
            }
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}