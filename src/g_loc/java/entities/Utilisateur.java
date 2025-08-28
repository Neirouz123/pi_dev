package entities;

public class Utilisateur {
private int id;
private String username;
private int role;

    public Utilisateur(int id, String username,int role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
    public Utilisateur(int id, String username) {
        this.id = id;
        this.username = username;
    }
    public Utilisateur() {}

    public int getRole() {return role;}

    public int getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
}
