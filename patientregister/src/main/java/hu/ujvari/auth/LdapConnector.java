package hu.ujvari.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection; //interfész
import org.apache.directory.ldap.client.api.LdapNetworkConnection; //az interfész implementációja

import hu.ujvari.data.User;

public class LdapConnector {
    private final String ldapHost;
    private final int ldapPort;
    private final String baseDn;
    private LdapConnection connection;

    public LdapConnector(String ldapHost, int ldapPort, String baseDn) {
        this.ldapHost = ldapHost;
        this.ldapPort = ldapPort;
        this.baseDn = baseDn;
    }

    private void connectAndBind() throws Exception {
        if (connection == null || !connection.isConnected()) {
            connection = new LdapNetworkConnection(ldapHost, ldapPort);
            connection.bind("cn=admin,dc=medicalpractice,dc=com", "secret");
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.unBind();
                connection.close();
            } catch (IOException | LdapException e) {
                System.err.println("Kapcsolat bezárása sikertelen: " + e.getMessage());
            }
        }
    }

    public boolean authenticate(String username, String password) {
        String userDn = getUserDnByUsername(username);
        System.out.println("Keresett felhasználó: " + username);
        System.out.println("Kapott DN: " + userDn);

        if (userDn == null) {
            System.err.println("Felhasználó nem található: " + username);
            return false;
        }

        try (LdapConnection tempConn = new LdapNetworkConnection(ldapHost, ldapPort)) {
            tempConn.bind(userDn, password);
            return tempConn.isAuthenticated();
        } catch (Exception e) {
            System.err.println("LDAP hitelesítési hiba: " + e.getMessage());
            return false;
        }
    }

    private String getUserDnByUsername(String username) {
        try {
            connectAndBind();
            String filter = "(cn=" + username + ")";
            EntryCursor cursor = connection.search(baseDn, filter, SearchScope.SUBTREE);

            while (cursor.next()) {
                Entry entry = cursor.get();
                return entry.getDn().toString();
            }
        } catch (Exception e) {
            System.err.println("Hiba a felhasználó keresése közben: " + e.getMessage());
        }
        return null;
    }

    public User getUserByUsername(String username) {
        User user = new User();
        user.setUsername(username);

        try {
            connectAndBind();
            String userDn = getUserDnByUsername(username);

            if (userDn == null) {
                System.err.println("Felhasználó nem található: " + username);
                return user;
            }

            
            Entry entry = connection.lookup(userDn);

            /*
             * az első (és ebben az esetben egyetlen) találatot adja vissza, mint Entry objektumot. 
             * Ebben benne vannak azok az attribútum‑érték párok, amiket alapértelmezés szerint a szerver küld 
             * (leggyakrabban minden, de van lehetőség csak bizonyos attribútumokat kérni).
             */

            if (entry != null) {
                // Betöltjük az LDAP-ból a patientId-t az employeeNumber mezőből
                Attribute patientIdAttr = entry.get("employeeNumber");
                if (patientIdAttr != null) {
                    user.setUserId(patientIdAttr.getString());
                    
                } else {
                    System.out.println("employeeNumber lekérése sikertelen");
                    user.setUserId(entry.get("cn").getString());
                }

                // Full name (displayName vagy cn)
                Attribute displayNameAttr = entry.get("displayName");
                if (displayNameAttr == null) {
                    displayNameAttr = entry.get("displayname");
                }

                String fullName = displayNameAttr != null
                        ? displayNameAttr.getString()
                        : (entry.get("cn") != null ? entry.get("cn").getString() : "Ismeretlen");

                user.setFullName(fullName);

                // Szerepkörök meghatározása az LDAP DN alapján
                Dn dn = new Dn(userDn);
                String ou = dn.size() > 1 ? dn.getRdn(1).getValue() : "";

                /* Alternatív, robusztusabb megoldás:
                 * String ou = "";
                *  for (Rdn rdn : dn) {
                *       if ("ou".equalsIgnoreCase(rdn.getType())) {
                *           ou = rdn.getValue();
                *           break;
                *       }
                *   }
                */

                List<String> roles = new ArrayList<>();
                if (ou.equalsIgnoreCase("doctors")) roles.add("DOCTOR");
                else if (ou.equalsIgnoreCase("nurses")) roles.add("NURSE");
                else if (ou.equalsIgnoreCase("assistants")) roles.add("ASSISTANT");
                else if (ou.equalsIgnoreCase("patients")) roles.add("PATIENT");

                user.setRoles(roles);
                setPermissionsBasedOnRoles(user);
            }

        } catch (Exception e) {
            System.err.println("LDAP felhasználó lekérdezési hiba: " + e.getMessage());
        }

        return user;
    }

    public boolean changePassword(String username, String currentPassword, String newPassword) {
        String userDn = getUserDnByUsername(username);
    
        if (userDn == null) {
            System.err.println("Nem található felhasználó: " + username);
            return false;
        }
    
        try {
            // Admin kapcsolatot használunk
            connectAndBind();
            
            // Ellenőrizzük a jelenlegi jelszót
            try (LdapConnection userConn = new LdapNetworkConnection(ldapHost, ldapPort)) {
                userConn.bind(userDn, currentPassword);
                if (!userConn.isAuthenticated()) {
                    System.err.println("Hibás jelenlegi jelszó.");
                    return false;
                }
            }
            
            // SHA hash készítése
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(newPassword.getBytes(StandardCharsets.UTF_8));
            String shaPassword = "{SHA}" + Base64.getEncoder().encodeToString(digest);
            
            // Módosítsuk a jelszót

            /*
            ldif forma:
            dn: uid=felhasznalo,ou=Users,dc=peldadomain,dc=hu
            changetype: modify
            replace: userPassword
            userPassword: újJelszó123
            */

            ModifyRequest modReq = new ModifyRequestImpl();
            modReq.setName(new Dn(userDn));
            modReq.replace("userPassword", shaPassword);
            
            connection.modify(modReq);
            System.out.println("Jelszó sikeresen megváltoztatva: " + shaPassword);
            return true;
        } catch (Exception e) {
            System.err.println("Jelszómódosítási hiba: " + e.getMessage());
            return false;
        }
    }

    
    // érdemes lett volna talán átteni az AuthService-be
    private void setPermissionsBasedOnRoles(User user) {
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("LOGIN", true);

        if (user.isDoctor()) {
            permissions.put("VIEW_PATIENT", true);
            permissions.put("EDIT_PATIENT", true);
            permissions.put("VIEW_EKG", true);
            permissions.put("ADD_EXAMINATION", true);
            permissions.put("EDIT_EXAMINATION", true);
        } else if (user.isNurse()) {
            permissions.put("VIEW_PATIENT", true);
            permissions.put("EDIT_PATIENT", false);
            permissions.put("VIEW_EKG", true);
            permissions.put("ADD_EXAMINATION", true);
            permissions.put("EDIT_EXAMINATION", false);
        } else if (user.isAssistant()) {
            permissions.put("VIEW_PATIENT", true);
            permissions.put("EDIT_PATIENT", true);
            permissions.put("VIEW_EKG", false);
            permissions.put("ADD_EXAMINATION", false);
            permissions.put("EDIT_EXAMINATION", false);
        } else if (user.getRoles().contains("PATIENT")) {
            permissions.put("VIEW_PATIENT", true);
            permissions.put("EDIT_PATIENT", false);
            permissions.put("VIEW_EKG", true);
            permissions.put("ADD_EXAMINATION", false);
            permissions.put("EDIT_EXAMINATION", false);
        }
        

        user.setPermissions(permissions);
    }

    
}
