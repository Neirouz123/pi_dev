package entities;

import java.util.ArrayList;
import java.util.List;

public class local {
    private int id;
    private int capacite;
    private boolean disponible;
    private double prix;
    private List<byte[]> images = new ArrayList<>();
    private String nom;
    private String description;
    private String adresse;

    // ✅ Constructeur complet
    public local(int id,  int capacite, boolean disponible, double prix, List<byte[]> images, String nom, String description, String adresse) {
        this.id = id;
        this.capacite = capacite;
        this.disponible = disponible;
        this.prix = prix;
        this.images = images;
        this.nom = nom;
        this.description = description;
        this.adresse = adresse;
    }

    // ✅ Constructeur sans ID (pour l'ajout)
    public local( int capacite, boolean disponible, double prix, List<byte[]> images, String nom, String description, String adresse) {
        this(0, capacite, disponible, prix, images, nom, description, adresse);
    }

    public local(String nom, String adresse, int capacite, byte[] imageData, String date) {
    }

    public local() {

    }

    // ✅ Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }



    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public List<byte[]> getImages() { return images; }
    public void setImages(List<byte[]> images) { this.images = images; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    // ✅ Méthode toString() pour affichage
    @Override
    public String toString() {
        return "Local{" +
                "id=" + id +
                ", capacite=" + capacite +
                ", disponible=" + disponible +
                ", prix=" + prix +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", adresse='" + adresse + '\'' +
                '}';
    }
}
