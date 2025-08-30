package services;

import entities.Utilisateur;
import tools.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUtilisateur {

    private Connection connection;

    public ServiceUtilisateur() {
        // Initialize database connection
        // Replace with your actual database connection logic
        try {
            this.connection = DataSource.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Utilisateur getUserById(int userId) throws SQLException {
        String query = "SELECT * FROM utilisateurs WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
        }

        return null;
    }

    /**
     * Authenticate user with username and password
     */
    public Utilisateur authenticate(String username, String password) throws SQLException {
        String query = "SELECT * FROM utilisateurs WHERE username = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // In production, hash the password

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
        }

        return null; // Authentication failed
    }

    /**
     * Register a new user
     */
    public boolean register(Utilisateur user) throws SQLException {
        String query = "INSERT INTO utilisateurs (username, password, role, email, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword()); // Hash in production
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getEmail());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Check if username already exists
     */
    public boolean usernameExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM utilisateurs WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }

        return false;
    }

    /**
     * Get all users (admin function)
     */
    public List<Utilisateur> getAllUsers() throws SQLException {
        List<Utilisateur> users = new ArrayList<>();
        String query = "SELECT * FROM utilisateurs ORDER BY created_at DESC";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                users.add(user);
            }
        }

        return users;
    }

    /**
     * Update user role (admin function)
     */
    public boolean updateUserRole(int userId, String newRole) throws SQLException {
        String query = "UPDATE utilisateurs SET role = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newRole);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Delete user (admin function)
     */
    public boolean deleteUser(int userId) throws SQLException {
        String query = "DELETE FROM utilisateurs WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}

