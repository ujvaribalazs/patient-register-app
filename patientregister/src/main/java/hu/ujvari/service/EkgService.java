package hu.ujvari.service;

import hu.ujvari.auth.LdapConnector;
import hu.ujvari.data.EkgData;
import hu.ujvari.data.User;
import hu.ujvari.db.existdb.EkgDbConnector;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EkgService {
    private final EkgDbConnector ekgDbConnector;
    private final AuthService authService;
    private final LdapConnector ldapConnector;
    
    public EkgService(EkgDbConnector ekgDbConnector, AuthService authService, LdapConnector ldapConnector) {
        this.ekgDbConnector = ekgDbConnector;
        this.authService = authService;
        this.ldapConnector = ldapConnector;
    }
    
    /**
     * Egy beteg összes EKG adatának lekérdezése (jogosultság ellenőrzéssel)
     */
    public List<EkgData> getEkgsByPatientId(String patientId) throws SecurityException {
        User currentUser = authService.getCurrentUser();
        
        if (currentUser == null) {
            throw new SecurityException("Nincs bejelentkezett felhasználó!");
        }
        
        if (!currentUser.hasPermission("VIEW_EKG")) {
            throw new SecurityException("Nincs jogosultsága az EKG adatok megtekintéséhez!");
        }
        
        if (!ldapConnector.hasAccessToPatient(currentUser, patientId)) {
            throw new SecurityException("Nincs jogosultsága ennek a betegnek az EKG adataihoz!");
        }
        
        return ekgDbConnector.getEkgsByPatientId(patientId);
    }
    
    /**
     * Egy EKG adat lekérdezése azonosító alapján (jogosultság ellenőrzéssel)
     */
    public EkgData getEkgById(String ekgId) throws SecurityException {
        User currentUser = authService.getCurrentUser();
        
        if (currentUser == null) {
            throw new SecurityException("Nincs bejelentkezett felhasználó!");
        }
        
        if (!currentUser.hasPermission("VIEW_EKG")) {
            throw new SecurityException("Nincs jogosultsága az EKG adatok megtekintéséhez!");
        }
        
        EkgData ekgData = ekgDbConnector.getEkgById(ekgId);
        
        if (ekgData != null && !ldapConnector.hasAccessToPatient(currentUser, ekgData.getPatientId())) {
            throw new SecurityException("Nincs jogosultsága ennek a betegnek az EKG adataihoz!");
        }
        
        return ekgData;
    }
    
    /**
     * EKG adat mentése (jogosultság ellenőrzéssel)
     */
    public boolean saveEkgData(EkgData ekgData) throws SecurityException {
        User currentUser = authService.getCurrentUser();
        
        if (currentUser == null) {
            throw new SecurityException("Nincs bejelentkezett felhasználó!");
        }
        
        if (!currentUser.isDoctor() && !currentUser.isNurse()) {
            throw new SecurityException("Nincs jogosultsága EKG adatok mentéséhez!");
        }
        
        if (!ldapConnector.hasAccessToPatient(currentUser, ekgData.getPatientId())) {
            throw new SecurityException("Nincs jogosultsága ennek a betegnek az EKG adatait módosítani!");
        }
        
        // Ha új EKG-t hozunk létre
        if (ekgData.getEkgId() == null || ekgData.getEkgId().isEmpty()) {
            // Egyedi azonosító generálása
            ekgData.setEkgId("EKG_" + UUID.randomUUID().toString().substring(0, 8));
            
            // Aktuális dátum beállítása, ha még nincs
            if (ekgData.getRecordDate() == null || ekgData.getRecordDate().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                ekgData.setRecordDate(dateFormat.format(new Date()));
            }
        }
        
        // Beállítjuk, hogy ki rögzítette/módosította az EKG-t
        ekgData.setRecordedByUserId(currentUser.getUserId());
        
        return ekgDbConnector.saveEkgData(ekgData);
    }
    
    /**
     * Új üres EKG objektum létrehozása egy beteghez
     */
    public EkgData createNewEkgForPatient(String patientId, String betegID) {
        EkgData ekgData = new EkgData();
        ekgData.setPatientId(patientId);
        ekgData.setBetegID(betegID);
        
        // Aktuális dátum beállítása
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        ekgData.setRecordDate(dateFormat.format(new Date()));
        
        return ekgData;
    }
}