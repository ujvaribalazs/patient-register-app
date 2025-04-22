package hu.ujvari;

import hu.ujvari.auth.LdapConnector;
import hu.ujvari.db.existdb.EkgDbConnector;
import hu.ujvari.db.mongodb.PatientDbConnector;
import hu.ujvari.service.AuthService;
import hu.ujvari.service.PatientService;
import hu.ujvari.ui.LoginWindow;
import javafx.application.Application;

public class PatientRegisterApplication {
    
    public static void main(String[] args) {
        // Kapcsolatok inicializálása
        LdapConnector ldapConnector = new LdapConnector("localhost", 389, "dc=medicalpractice,dc=com");
        PatientDbConnector patientDbConnector = new PatientDbConnector("mongodb://localhost:27017", "patientdb");
        EkgDbConnector ekgDbConnector = new EkgDbConnector("xmldb:exist://localhost:8080/exist/xmlrpc", "admin", "", "/db/ekgData");
        
         // Szolgáltatások példányosítása
        AuthService authService = new AuthService(ldapConnector);
        PatientService patientService = new PatientService(patientDbConnector, authService, ekgDbConnector);

        // LoginWindow példányosítása és beállítása
        // A service‐példányok „átadása” a LoginWindow osztálynak
        LoginWindow.setStaticAuthService(authService);
        LoginWindow.setStaticPatientService(patientService);
        LoginWindow.setStaticLdapConnector(ldapConnector);
        // A JavaFX runtime indítása, ami létrehozza a LoginWindow példányt
        Application.launch(LoginWindow.class); 
        
        // GUI inicializálása és indítása
        LoginWindow.launch(LoginWindow.class, args);
    }
}