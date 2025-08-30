package services;

import entities.local;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {

    // Ajouter un élément
    void ajouter(T t);

    // Modifier un élément
    void modifier(T t);

    // Supprimer un élément par ID
    void supprimer(int id);

    double getPrixById(int localId) throws SQLException;

    // Obtenir un élément par ID
    T getOne(T t);

    // Obtenir tous les éléments
    List<T> getAll(T t);

    // Trier les éléments selon un critère et ordre
    List<T> trierLocaux(String critere, boolean ascendant);

    // Rechercher des éléments par mot-clé
    List<T> rechercherLocal(String keyword);

    local rechercherLocalParId(int id);
    // Vérifier si un local est disponible pour une date et une heure
    boolean estDisponible(int idLocal, String date, String heure);
}
