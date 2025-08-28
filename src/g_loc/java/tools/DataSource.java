
package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DataSource {
    private Connection cnx;
    private static DataSource instance;

    private String url = "jdbc:mysql://localhost:3306/locaux";
    private String user = "root";
    private String password = "";

    private DataSource(){
        try {
            cnx = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connected to DataBase !");
        } catch (SQLException ex) {
            System.out.println("❌ Erreur de connexion !");
            System.out.println(ex.getMessage());
        }
    }

    public static DataSource getInstance(){
        if(instance == null){
            instance = new DataSource();
        }
        return instance;
    }

    public Connection getConnection(){
        return this.cnx;
    }
}
