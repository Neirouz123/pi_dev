package services;

import entities.Reservation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface IReservation {
    void viderPanier(int userId) throws SQLException;

    void supprimerReservation(int reservationId) throws SQLException;
    
    void supprimerReservationParId(int reservationId);

    String getLocalNameByReservationId(int reservationId);
    public void updateReservation(Reservation reservation) ;

    void ajouterReservation(Reservation reservation);
    List<Reservation> getReservationsForLocal(int localId);
    boolean annulerReservation(int reservationId);
    boolean confirmerReservation(int reservationId);
    List<Reservation> getReservationsForUser(int utilisateurId);
    public List<Reservation> getAllReservations() ;
    public boolean isDateReserved(int localId, LocalDate date) ;
// Updated to int
}
