package hu.ujvari.service;

import hu.ujvari.auth.LdapConnector;
import hu.ujvari.data.User;

public class AuthService {
    private LdapConnector ldapConnector;
    private User currentUser;
    
    public AuthService(LdapConnector ldapConnector) {
        this.ldapConnector = ldapConnector;
    }
    
    /**
     * Felhasználó bejelentkeztetése
     * @return true, ha sikeres a bejelentkezés
     */
    public boolean login(String username, String password) {
        boolean authenticated = ldapConnector.authenticate(username, password);
        
        if (authenticated) {
            // Felhasználói adatok és jogosultságok lekérdezése
            currentUser = ldapConnector.getUserByUsername(username);
            System.out.println("Sikeres bejelentkezés: " + currentUser.getFullName());
            return true;
        } else {
            System.out.println("Sikertelen bejelentkezés: " + username);
            return false;
        }
    }
    
    /**
     * Az aktuálisan bejelentkezett felhasználó lekérdezése
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Kijelentkezés
     */
    public void logout() {
        currentUser = null;
        System.out.println("Felhasználó kijelentkezett");
    }
    
    /**
     * Ellenőrzi, hogy a felhasználó rendelkezik-e egy adott jogosultsággal
     */
    public boolean hasPermission(String permission) {
        if (currentUser == null) {
            return false;
        }
        
        return currentUser.hasPermission(permission);
    }
    
    /**
     * Ellenőrzi, hogy a felhasználó hozzáférhet-e egy adott beteg adataihoz
     */
    public boolean hasAccessToPatient(String patientId) {
        if (currentUser == null) {
            return false;
        }
        
        return ldapConnector.hasAccessToPatient(currentUser, patientId);
    }
}