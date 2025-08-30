package controller;

import entities.Utilisateur;

public class UserSession {
    private static Utilisateur currentUser;

    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isClient() {
        return currentUser != null && "CLIENT".equalsIgnoreCase(currentUser.getRole());
    }

    public static void logout() {
        currentUser = null;
    }
}