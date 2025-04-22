package hu.ujvari.service;

import java.util.ArrayList;
import java.util.List;

import hu.ujvari.data.EkgData;
import hu.ujvari.data.Examination;
import hu.ujvari.data.Patient;
import hu.ujvari.data.User;
import hu.ujvari.db.existdb.EkgDbConnector;
import hu.ujvari.db.mongodb.PatientDbConnector;

/*
 * PatientService
 *
 * Fő feladata:
 * - Bejelentkezett felhasználó hitelesítése és jogosultság-ellenőrzés (AuthService).
 * - Betegadatok és EKG-/vizsgálati adatok lekérdezése és mentése.
 * - Üzleti szabályok (pl. ki láthatja/mentheti az adatokat) érvényesítése.
 */

public class PatientService {
    
    private final PatientDbConnector patientDbConnector;
    private final AuthService authService;
    private final EkgDbConnector ekgDbConnector;
    
    public PatientService(PatientDbConnector patientDbConnector, AuthService authService, EkgDbConnector ekgDbConnector) {
       
        this.patientDbConnector = patientDbConnector;
        this.authService = authService;
        this.ekgDbConnector = ekgDbConnector;
    }
    
    
    /**
     * Az összes beteg lekérdezése (jogosultság ellenőrzéssel)
     */
    public List<Patient> getAllPatients() throws SecurityException {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            throw new SecurityException("Nincs bejelentkezett felhasználó!");
        }

        if (currentUser.getRoles().contains("PATIENT")) {
            // Csak saját magát kérdezheti le
            Patient self = patientDbConnector.getPatientById(currentUser.getUserId());
            List<Patient> selfList = new ArrayList<>();
            if (self != null) selfList.add(self);
            return selfList;
        }

        if (!currentUser.isDoctor() && !currentUser.isNurse() && !currentUser.isAssistant()) {
            throw new SecurityException("Nincs jogosultsága a betegek listázásához!");
        }

        return patientDbConnector.getAllPatients();
    }

    
    /**
     * Egy beteg adatainak lekérdezése azonosító alapján (jogosultság ellenőrzéssel)
     */
    public Patient getPatientById(String patientId) throws SecurityException {
        User currentUser = authService.getCurrentUser();
        
        if (currentUser == null) {
            throw new SecurityException("Nincs bejelentkezett felhasználó!");
        }
        
        if (!authService.hasAccessToPatient(currentUser, patientId)) {
            throw new SecurityException("Most sincs jogosultsága a beteg adataihoz!");
        }
        
        return patientDbConnector.getPatientById(patientId);
    }
    
    /**
     * Egy beteg adatainak lekérdezése BetegID alapján (jogosultság ellenőrzéssel)
     */
    public Patient getPatientByBetegId(String betegId) throws SecurityException {
        User currentUser = authService.getCurrentUser();
        
        if (currentUser == null) {
            throw new SecurityException("Nincs bejelentkezett felhasználó!");
        }
        
        // Először lekérdezzük a beteget, hogy megtudjuk a patientId-t
        Patient patient = patientDbConnector.getPatientByBetegId(betegId);
        
        if (patient == null) {
            return null;
        }
        
        // Ellenőrizzük a jogosultságot
        if (!authService.hasAccessToPatient(currentUser, patient.getPatientId())) {
            throw new SecurityException("Nincs jogosultsága a beteg adataihoz!");
        }
        
        return patient;
    }

    public EkgData getLatestEkgForPatient(String patientId) {
        List<EkgData> ekgList = ekgDbConnector.getEkgsByPatientId(patientId);
        if (ekgList != null && !ekgList.isEmpty()) {
            
            return ekgList.get(0);  // az első ekg a listából
        }
        return null;
    }

    
    /**
     * Egy beteg vizsgálatainak lekérdezése (jogosultság ellenőrzéssel)
     */
    public List<Examination> getExaminationsByPatientId(String patientId) throws SecurityException {
        User currentUser = authService.getCurrentUser();
        
        if (currentUser == null) {
            throw new SecurityException("Nincs bejelentkezett felhasználó!");
        }
        
        if (!authService.hasAccessToPatient(currentUser, patientId)) {
            throw new SecurityException("Nincs jogosultsága a beteg vizsgálataihoz!");
        }
        
        return patientDbConnector.getExaminationsByPatientId(patientId);
    }
    
    /**
     * Egy vizsgálat lekérdezése azonosító alapján (jogosultság ellenőrzéssel)
     */
    public Examination getExaminationById(String examinationId) throws SecurityException {
        User currentUser = authService.getCurrentUser();
        
        if (currentUser == null) {
            throw new SecurityException("Nincs bejelentkezett felhasználó!");
        }
        
        // Lekérdezzük a vizsgálatot
        Examination examination = patientDbConnector.getExaminationById(examinationId);
        
        if (examination == null) {
            return null;
        }
        
        // Ellenőrizzük a jogosultságot a beteg adatai alapján
        if (!authService.hasAccessToPatient(currentUser, examination.getPatientId())) {
            throw new SecurityException("Nincs jogosultsága ehhez a vizsgálathoz!");
        }
        
        return examination;
    }
    
    /**
     * Beteg adatainak mentése (jogosultság ellenőrzéssel)
     */
    public void savePatient(Patient patient) throws SecurityException {
        User currentUser = authService.getCurrentUser();
        
        if (currentUser == null) {
            throw new SecurityException("Nincs bejelentkezett felhasználó!");
        }
        
        if (!currentUser.hasPermission("EDIT_PATIENT")) {
            throw new SecurityException("Nincs jogosultsága a beteg adatainak szerkesztéséhez!");
        }
        
        patientDbConnector.savePatient(patient);
    }
    
    /**
     * Vizsgálat mentése (jogosultság ellenőrzéssel)
     */
    public void saveExamination(Examination examination) throws SecurityException {
        User currentUser = authService.getCurrentUser();
        
        if (currentUser == null) {
            throw new SecurityException("Nincs bejelentkezett felhasználó!");
        }
        
        if (!currentUser.hasPermission("ADD_EXAMINATION") && !currentUser.hasPermission("EDIT_EXAMINATION")) {
            throw new SecurityException("Nincs jogosultsága a vizsgálat adatainak kezeléséhez!");
        }
        
        // Ha létező vizsgálatot szerkeszt, ellenőrizzük a jogosultságokat
        if (examination.getId() != null && !examination.getId().isEmpty()) {
            Examination existingExamination = patientDbConnector.getExaminationById(examination.getId());
            
            if (existingExamination != null && 
                !authService.hasAccessToPatient(currentUser, existingExamination.getPatientId())) {
                throw new SecurityException("Nincs jogosultsága ehhez a vizsgálathoz!");
            }
            
            // Orvosok csak a saját vizsgálatukat szerkeszthetik, kivéve ha adminisztrátorok
            if (existingExamination != null
                && currentUser.isDoctor()
                && !currentUser.getRoles().contains("ADMIN")
                && !currentUser.getUserId().equals(existingExamination.getOrvosID())) {
                throw new SecurityException("Csak a saját vizsgálatait szerkesztheti!");
            }
        }
        
        patientDbConnector.saveExamination(examination);
    }
}