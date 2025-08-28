package services;

import entities.Reservation;
import tools.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservation implements IReservation {
    private Connection cnx;


    public ServiceReservation() {
        this.cnx = DataSource.getInstance().getConnection();
    }
    @Override
    public void viderPanier(int userId) throws SQLException {
        String query = "DELETE FROM reservation WHERE utilisateur_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        }
    }


    @Override
    public void supprimerReservation(int reservationId) throws SQLException {
        String query = "DELETE FROM reservation WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, reservationId);
            pst.executeUpdate();
        }
    }
    
    public void supprimerReservationParId(int reservationId) {
        try {
            String query = "DELETE FROM reservation WHERE id = ?";
            try (PreparedStatement pst = cnx.prepareStatement(query)) {
                pst.setInt(1, reservationId);
                int rowsDeleted = pst.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("✅ Reservation deleted successfully!");
                } else {
                    System.out.println("⚠️ No reservation found with ID: " + reservationId);
                }
            }
        } catch (SQLException ex) {
            System.out.println("❌ Error deleting reservation: " + ex.getMessage());
            throw new RuntimeException("Failed to delete reservation", ex);
        }
    }

    @Override
    public String getLocalNameByReservationId(int localId) {
        String localName = null;
        // Directly query the local table using the local ID
        String query = "SELECT nom FROM local WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, localId); // Use localId parameter
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                localName = rs.getString("nom");
            }
        } catch (SQLException ex) {
            System.out.println("❌ Error while retrieving local name: " + ex.getMessage());
        }

        return localName;
    }


    // ✅ Ajouter une réservation
    @Override
    public void ajouterReservation(Reservation reservation) {
        try {
            String req = "INSERT INTO reservation (local_id, utilisateur_id, date, heureDebut, heureFin, isConfirmee) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, reservation.getLocalId());  // updated to localId
            pstmt.setInt(2, reservation.getUtilisateurId());
            pstmt.setDate(3, Date.valueOf(reservation.getDate()));
            pstmt.setTime(4, Time.valueOf(reservation.getHeureDebut()));
            pstmt.setTime(5, Time.valueOf(reservation.getHeureFin()));
            pstmt.setBoolean(6, reservation.isConfirmee());  // updated to isConfirmed

            pstmt.executeUpdate();

            // Get generated ID for the reservation
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                reservation.setId(rs.getInt(1));
            }

        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de l'ajout de la réservation : " + ex.getMessage());
        }
    }

    // ✅ Obtenir les réservations pour un local
    @Override
    public List<Reservation> getReservationsForLocal(int localId) {
        List<Reservation> localReservations = new ArrayList<>();
        String req = "SELECT * FROM reservation WHERE local_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(req)) {
            pstmt.setInt(1, localId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Create and add the reservation object to the list
                Reservation reservation = new Reservation(
                        rs.getInt("id"),
                        rs.getInt("local_id"),
                        rs.getInt("utilisateur_id"),  // updated to utilisateur_id
                        rs.getDate("date").toLocalDate(),
                        rs.getTime("heureDebut").toLocalTime(),
                        rs.getTime("heureFin").toLocalTime(),
                        rs.getBoolean("isConfirmee")  // updated to isConfirmed
                );
                localReservations.add(reservation);
            }
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de la récupération des réservations pour le local : " + ex.getMessage());
        }
        return localReservations;
    }

    // ✅ Annuler une réservation
    @Override
    public boolean annulerReservation(int reservationId) {
        String req = "DELETE FROM reservation WHERE id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(req)) {
            pstmt.setInt(1, reservationId);
            int rowsDeleted = pstmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de l'annulation de la réservation : " + ex.getMessage());
            return false;
        }
    }

    // ✅ Confirmer une réservation
    @Override
    public boolean confirmerReservation(int reservationId) {
        String req = "UPDATE reservation SET isConfirmee = TRUE WHERE id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(req)) {
            pstmt.setInt(1, reservationId);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de la confirmation de la réservation : " + ex.getMessage());
            return false;
        }
    }

    // ✅ Obtenir les réservations pour un utilisateur
    @Override
    public List<Reservation> getReservationsForUser(int utilisateurId) {
        List<Reservation> userReservations = new ArrayList<>();
        String req = "SELECT * FROM reservation WHERE utilisateur_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(req)) {
            pstmt.setInt(1, utilisateurId);
            ResultSet rs = pstmt.executeQuery();
            ServiceLocal serviceLocal = new ServiceLocal(); // Initialize ServiceLocal
            while (rs.next()) {
                Reservation reservation = new Reservation(
                        rs.getInt("id"),
                        rs.getInt("local_id"),
                        rs.getInt("utilisateur_id"),
                        rs.getDate("date").toLocalDate(),
                        rs.getTime("heureDebut").toLocalTime(),
                        rs.getTime("heureFin").toLocalTime(),
                        rs.getBoolean("isConfirmee")
                );
                // Fetch and set the Local's price
                try {
                    double prix = serviceLocal.getPrixById(reservation.getLocalId());
                    reservation.setPrix(prix);
                } catch (SQLException e) {
                    System.err.println("Error fetching price: " + e.getMessage());
                    reservation.setPrix(0.0); // Default value on error
                }
                userReservations.add(reservation);
            }
        } catch (SQLException ex) {
            System.out.println("Error retrieving reservations: " + ex.getMessage());
        }
        return userReservations;
    }

    public boolean isDateReserved(int localId, LocalDate date) {
        String req = "SELECT COUNT(*) FROM reservation WHERE local_id = ? AND date = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(req)) {
            pstmt.setInt(1, localId);
            pstmt.setDate(2, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.out.println("❌ Erreur de vérification de disponibilité : " + ex.getMessage());
        }
        return false;
    }

    // ✅ Obtenir toutes les réservations
    @Override
    public List<Reservation> getAllReservations() {
        List<Reservation> allReservations = new ArrayList<>();
        String req = "SELECT * FROM reservation";

        try (PreparedStatement pstmt = cnx.prepareStatement(req);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Reservation reservation = new Reservation(
                        rs.getInt("id"),
                        rs.getInt("local_id"),
                        rs.getInt("utilisateur_id"),
                        rs.getDate("date").toLocalDate(),
                        rs.getTime("heureDebut").toLocalTime(),
                        rs.getTime("heureFin").toLocalTime(),
                        rs.getBoolean("isConfirmee")
                );
                allReservations.add(reservation);
            }

        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de la récupération de toutes les réservations : " + ex.getMessage());
        }

        return allReservations;
    }
}
