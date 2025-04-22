package hu.ujvari.service;

import hu.ujvari.auth.LdapConnector;
import hu.ujvari.data.User;

/**
 * Az AuthService osztály felelős a felhasználói hitelesítésért és jogosultságkezelésért.
 * 
 * Az osztály az LDAP-on keresztüli autentikációt valósítja meg, és kezeli az aktuálisan
 * bejelentkezett felhasználó adatait, jogosultságait. Biztosítja a be- és kijelentkezési
 * funkcionalitást, valamint lehetővé teszi a felhasználói jogosultságok és a betegadatokhoz
 * való hozzáférés ellenőrzését.
 * 
 * Az osztály főbb funkciói:
 * - Felhasználó hitelesítése LDAP-on keresztül
 * - Aktuális felhasználó kezelése
 * - Jogosultságok ellenőrzése
 * - Betegadatokhoz való hozzáférés ellenőrzése
 * 
 */

public class AuthService {
    private final LdapConnector ldapConnector;
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
    public boolean hasAccessToPatient(User user, String patientId) {
        if (user.isDoctor()) return true;

        if ((user.isNurse() || user.isAssistant()) && user.hasPermission("VIEW_PATIENT")) {
            return true;
        }

        if (user.getRoles().contains("PATIENT")) {
            return user.getUserId().equals(patientId);
        }

        
        

        return false;
    }
}