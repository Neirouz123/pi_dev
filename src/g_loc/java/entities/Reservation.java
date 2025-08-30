package entities;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation {

    private int id;
    private double prix;
    private int local_id;  // Use camelCase for variable names
    private int utilisateur_id;  // Use camelCase for variable names
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private boolean isConfirmee;  // Using 'isConfirmed' instead of 'isConfirmee'

    // Constructor
    public Reservation( int id, int local_id, int utilisateur_id, LocalDate date, LocalTime heureDebut, LocalTime heureFin, boolean isConfirmee) {
        this.id = id;
        this.local_id = local_id;
        this.utilisateur_id = utilisateur_id;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.isConfirmee = isConfirmee;
    }
    // Add this constructor to Reservation.java

    // Constructor with price
    public Reservation(int id, int local_id, int utilisateur_id, LocalDate date, LocalTime heureDebut, LocalTime heureFin, boolean isConfirmee, double prix) {
        this.id = id;
        this.local_id = local_id;
        this.utilisateur_id = utilisateur_id;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.isConfirmee = isConfirmee;
        this.prix = prix;
    }

    public Reservation(int local, String date, double prix) {
        this.local_id = local;
        this.date = LocalDate.parse(date);
        this.prix = prix;
    }


    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
public void setConfirmee(boolean confirmee) {this.isConfirmee = confirmee;}
    public int getLocalId() {
        return this.local_id;
    }
    public double getPrix() {
        return prix;
    }
    public void setPrix(double prix) {
        this.prix = prix;
    }
    public void setLocalId(int localId) {
        this.local_id = localId;
    }

    public int getUtilisateurId() {
        return utilisateur_id;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateur_id = utilisateurId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public boolean isConfirmee() {
        return isConfirmee;
    }

    public void setConfirmed(boolean isConfirmee) {
        this.isConfirmee = isConfirmee;
    }

}
