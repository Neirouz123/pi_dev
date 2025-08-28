package services;
import entities.local;
import tools.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceLocal implements IService<local> {
    private Connection cnx;

    public ServiceLocal() {
        this.cnx = DataSource.getInstance().getConnection();
    }

    // ✅ Ajouter un local
    @Override
    public void ajouter(local local) {
        Connection cnx = null;
        try {
            cnx = DataSource.getInstance().getConnection();
            cnx.setAutoCommit(false); // Début transaction

            // Insertion du local
            String reqLocal = "INSERT INTO local (nom, adresse, capacite, disponible, prix, description) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = cnx.prepareStatement(reqLocal, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, local.getNom());
            pstmt.setString(2, local.getAdresse());
            pstmt.setInt(3, local.getCapacite());
            pstmt.setBoolean(4, local.isDisponible());
            pstmt.setDouble(5, local.getPrix());
            pstmt.setString(6, local.getDescription());

            pstmt.executeUpdate();

            // Récupération de l'ID généré
            ResultSet rs = pstmt.getGeneratedKeys();
            int localId = 0;
            if(rs.next()) {
                localId = rs.getInt(1);
            }

            // Insertion des images
            String reqImage = "INSERT INTO local_image (local_id, image_data) VALUES (?, ?)";
            PreparedStatement pstmtImage = cnx.prepareStatement(reqImage);

            for(byte[] image : local.getImages()) {
                pstmtImage.setInt(1, localId);
                pstmtImage.setBytes(2, image);
                pstmtImage.addBatch();
            }

            pstmtImage.executeBatch();
            cnx.commit(); // Validation transaction

        } catch (SQLException ex) {
            try {
                if(cnx != null) cnx.rollback();
            } catch (SQLException e) {
                System.out.println("Erreur rollback: " + e.getMessage());
            }
            System.out.println("Erreur lors de l'ajout: " + ex.getMessage());
        } finally {
            try {
                if(cnx != null) cnx.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Erreur reset auto-commit: " + e.getMessage());
            }
        }
    }

    // ✅ Modifier un local
    @Override
    public void modifier(local local) {
        Connection cnx = null;
        PreparedStatement pstmt = null;
        try {
            cnx = DataSource.getInstance().getConnection();
            cnx.setAutoCommit(false); // Début de la transaction

            // Mise à jour du local
            String req = "UPDATE `local` SET `nom` = ?, `adresse` = ?, `capacite` = ?, `disponible` = ?, `prix` = ?, `description` = ? WHERE `id` = ?";
            pstmt = cnx.prepareStatement(req);
            pstmt.setString(1, local.getNom());
            pstmt.setString(2, local.getAdresse());
            pstmt.setInt(3, local.getCapacite());
            pstmt.setBoolean(4, local.isDisponible());
            pstmt.setDouble(5, local.getPrix());
            pstmt.setString(6, local.getDescription());
            pstmt.setInt(7, local.getId());

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Local mis à jour avec succès !");
            } else {
                System.out.println("⚠️ Aucun local trouvé avec cet ID !");
            }

            // Mise à jour des images (si nécessaire)
            if (local.getImages() != null && !local.getImages().isEmpty()) {
                String reqImage = "DELETE FROM `local_image` WHERE `local_id` = ?";
                pstmt = cnx.prepareStatement(reqImage);
                pstmt.setInt(1, local.getId());
                pstmt.executeUpdate();

                String reqInsertImage = "INSERT INTO `local_image` (`local_id`, `image_data`) VALUES (?, ?)";
                for (byte[] image : local.getImages()) {
                    pstmt = cnx.prepareStatement(reqInsertImage);
                    pstmt.setInt(1, local.getId());
                    pstmt.setBytes(2, image);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            cnx.commit(); // Validation de la transaction

        } catch (SQLException ex) {
            try {
                if (cnx != null) {
                    cnx.rollback();
                }
            } catch (SQLException e) {
                System.out.println("Erreur rollback : " + e.getMessage());
            }
            System.out.println("❌ Erreur lors de la modification : " + ex.getMessage());
        } finally {
            try {
                if (cnx != null) {
                    cnx.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.out.println("Erreur lors de la réinitialisation du auto-commit : " + e.getMessage());
            }
        }
    }


    // ✅ Supprimer un local par ID
    @Override
    public void supprimer(int id) {
        Connection cnx = null;
        PreparedStatement pstmt = null;
        try {
            cnx = DataSource.getInstance().getConnection();
            cnx.setAutoCommit(false); // Début de la transaction

            // Supprimer les images associées au local
            String reqImage = "DELETE FROM `local_image` WHERE `local_id` = ?";
            pstmt = cnx.prepareStatement(reqImage);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();

            // Supprimer le local
            String req = "DELETE FROM `local` WHERE `id` = ?";
            pstmt = cnx.prepareStatement(req);
            pstmt.setInt(1, id);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("✅ Local supprimé avec succès !");
            } else {
                System.out.println("⚠️ Aucun local trouvé avec cet ID !");
            }

            cnx.commit(); // Validation de la transaction

        } catch (SQLException ex) {
            try {
                if (cnx != null) {
                    cnx.rollback();
                }
            } catch (SQLException e) {
                System.out.println("Erreur rollback : " + e.getMessage());
            }
            System.out.println("❌ Erreur lors de la suppression : " + ex.getMessage());
        } finally {
            try {
                if (cnx != null) {
                    cnx.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.out.println("Erreur lors de la réinitialisation du auto-commit : " + e.getMessage());
            }
        }
    }

    @Override
    public double getPrixById(int localId) throws SQLException {
        String query = "SELECT prix FROM local WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, localId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("prix");
                } else {
                    throw new SQLException("Aucun local trouvé avec l'ID: " + localId);
                }
            }
        }
    }
    // ✅ Obtenir un local par ID
    @Override
    public local getOne(local local) {
        String query = "SELECT * FROM `local` WHERE `id` = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, local.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Retrieve local data
                    int id = rs.getInt("id");
                    String nom = rs.getString("nom");
                    String adresse = rs.getString("adresse");
                    int capacite = rs.getInt("capacite");
                    boolean disponible = rs.getBoolean("disponible");
                    double prix = rs.getDouble("prix");
                    String description = rs.getString("description");

                    // Optionally handle image data (consider retrieving image as a list of byte arrays)
                    byte[] image = rs.getBytes("image");  // You might want to handle the image appropriately

                    // Return the local object
                    return new local(id, capacite, disponible, prix, image != null ? List.of(image) : new ArrayList<>(), nom, description, adresse);
                } else {
                    System.out.println("⚠️ Aucun local trouvé avec cet ID !");
                    return null;
                }
            }
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de la récupération du local : " + ex.getMessage());
            return null;
        }
    }

    // ✅ Obtenir tous les locaux
    @Override
    public List<local> getAll(local local) {
        List<local> locaux = new ArrayList<>();
        String query = "SELECT * FROM local";
                      
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                int localId = rs.getInt("id");
                
                // Create local object without images first
                local loc = new local(
                    localId,
                    rs.getInt("capacite"),
                    rs.getBoolean("disponible"),
                    rs.getDouble("prix"),
                    new ArrayList<>(), // Empty images list initially
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getString("adresse")
                );
                
                // Now load images for this local from local_image table
                String imageQuery = "SELECT image_data FROM local_image WHERE local_id = ?";
                try (PreparedStatement imgStmt = cnx.prepareStatement(imageQuery)) {
                    imgStmt.setInt(1, localId);
                    try (ResultSet imgRs = imgStmt.executeQuery()) {
                        List<byte[]> images = new ArrayList<>();
                        while (imgRs.next()) {
                            byte[] imageData = imgRs.getBytes("image_data");
                            if (imageData != null) {
                                images.add(imageData);
                            }
                        }
                        loc.setImages(images);
                    }
                }
                
                locaux.add(loc);
            }
        } catch (SQLException ex) {
            System.out.println("❌ Error getting locals: " + ex.getMessage());
            ex.printStackTrace();
        }
        return locaux;
    }

    // ✅ Méthode pour trier les locaux
    @Override
    public List<local> trierLocaux(String critere, boolean ascendant) {
        List<local> locaux = new ArrayList<>();
        String ordre = ascendant ? "ASC" : "DESC"; // Determine if sorting is ascending or descending

        // Validate the sorting criterion to avoid SQL injection
        if (!critere.equals("id") && !critere.equals("prix") && !critere.equals("capacite") && !critere.equals("nom") 
            && !critere.equals("disponible")) {
            System.out.println("⚠️ Critère de tri invalide !");
            return locaux; // Return empty list if the sorting criterion is invalid
        }

        String query = "SELECT * FROM `local` ORDER BY " + critere + " " + ordre;
        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(query)) {

            while (rs.next()) {
                // Retrieve local data
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String adresse = rs.getString("adresse");
                int capacite = rs.getInt("capacite");
                boolean disponible = rs.getBoolean("disponible");
                double prix = rs.getDouble("prix");
                String description = rs.getString("description");

                // Create local object without images first
                local loc = new local(
                    id,
                    capacite,
                    disponible,
                    prix,
                    new ArrayList<>(), // Empty images list initially
                    nom,
                    description,
                    adresse
                );
                
                // Now load images for this local from local_image table
                String imageQuery = "SELECT image_data FROM local_image WHERE local_id = ?";
                try (PreparedStatement imgStmt = cnx.prepareStatement(imageQuery)) {
                    imgStmt.setInt(1, id);
                    try (ResultSet imgRs = imgStmt.executeQuery()) {
                        List<byte[]> images = new ArrayList<>();
                        while (imgRs.next()) {
                            byte[] imageData = imgRs.getBytes("image_data");
                            if (imageData != null) {
                                images.add(imageData);
                            }
                        }
                        loc.setImages(images);
                    }
                }
                
                locaux.add(loc);
            }
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors du tri des locaux : " + ex.getMessage());
        }

        return locaux;
    }

    // ✅ Méthode pour rechercher des locaux par nom ou adresse
    @Override
    public List<local> rechercherLocal(String keyword) {
        List<local> locaux = new ArrayList<>();
        String query = "SELECT * FROM `local` WHERE nom LIKE ? OR adresse LIKE ?";

        // Use '%' for partial matching to handle cases where user types a keyword
        String search = "%" + keyword + "%";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, search);
            pst.setString(2, search);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    // Retrieve local data
                    int id = rs.getInt("id");
                    String nom = rs.getString("nom");
                    String adresse = rs.getString("adresse");
                    int capacite = rs.getInt("capacite");
                    boolean disponible = rs.getBoolean("disponible");
                    double prix = rs.getDouble("prix");
                    String description = rs.getString("description");

                    // Create local object without images first
                    local loc = new local(
                        id,
                        capacite,
                        disponible,
                        prix,
                        new ArrayList<>(), // Empty images list initially
                        nom,
                        description,
                        adresse
                    );
                    
                    // Now load images for this local from local_image table
                    String imageQuery = "SELECT image_data FROM local_image WHERE local_id = ?";
                    try (PreparedStatement imgStmt = cnx.prepareStatement(imageQuery)) {
                        imgStmt.setInt(1, id);
                        try (ResultSet imgRs = imgStmt.executeQuery()) {
                            List<byte[]> images = new ArrayList<>();
                            while (imgRs.next()) {
                                byte[] imageData = imgRs.getBytes("image_data");
                                if (imageData != null) {
                                    images.add(imageData);
                                }
                            }
                            loc.setImages(images);
                        }
                    }
                    
                    locaux.add(loc);
                }
            }
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de la recherche des locaux : " + ex.getMessage());
        }

        return locaux;
    }
    @Override
    public local rechercherLocalParId(int id) {
        local localTrouve = null;
        String query = "SELECT * FROM `local` WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Récupérer les données du local
                    String nom = rs.getString("nom");
                    String adresse = rs.getString("adresse");
                    int capacite = rs.getInt("capacite");
                    boolean disponible = rs.getBoolean("disponible");
                    double prix = rs.getDouble("prix");
                    String description = rs.getString("description");

                    // Create local object without images first
                    localTrouve = new local(
                            id,
                            capacite,
                            disponible,
                            prix,
                            new ArrayList<>(), // Empty images list initially
                            nom,
                            description,
                            adresse
                    );
                    
                    // Now load images for this local from local_image table
                    String imageQuery = "SELECT image_data FROM local_image WHERE local_id = ?";
                    try (PreparedStatement imgStmt = cnx.prepareStatement(imageQuery)) {
                        imgStmt.setInt(1, id);
                        try (ResultSet imgRs = imgStmt.executeQuery()) {
                            List<byte[]> images = new ArrayList<>();
                            while (imgRs.next()) {
                                byte[] imageData = imgRs.getBytes("image_data");
                                if (imageData != null) {
                                    images.add(imageData);
                                }
                            }
                            localTrouve.setImages(images);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de la recherche du local par ID : " + ex.getMessage());
        }

        return localTrouve;
    }


    @Override
    public boolean estDisponible(int idLocal, String date, String heure) {
        boolean disponible = true; // Par défaut, on suppose qu'il est dispo

        try {
            String req = "SELECT COUNT(*) FROM reservation_local WHERE id_local = ? AND date = ? AND heure = ?";
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, idLocal);
            pst.setString(2, date);
            pst.setString(3, heure);

            ResultSet rs = pst.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                disponible = false; // Si une réservation existe, le local est occupé
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la vérification de disponibilité : " + ex.getMessage());
        }

        return disponible;
    }

}
