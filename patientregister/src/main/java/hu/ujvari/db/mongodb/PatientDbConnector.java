package hu.ujvari.db.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import hu.ujvari.data.Examination;
import hu.ujvari.data.Patient;

public class PatientDbConnector {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> patientsCollection;
    private final MongoCollection<Document> examinationsCollection;
    
    /**
     * PatientDbConnector konstruktor
     * 
     * @param connectionString MongoDB kapcsolati string (pl. "mongodb://localhost:27017")
     * @param databaseName Adatbázis neve
     */
    public PatientDbConnector(String connectionString, String databaseName) {
        this.mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase(databaseName);
        this.patientsCollection = database.getCollection("patients");
        this.examinationsCollection = database.getCollection("examinations");
        
        System.out.println("MongoDB kapcsolat létrehozva: " + databaseName);
    }
    
    /**
     * Az összes beteg lekérdezése
     */
    public List<Patient> getAllPatients() {
        System.out.println("→ getAllPatients() called");
        List<Patient> patients = new ArrayList<>();
        
        for (Document doc : patientsCollection.find()) {
            System.out.println(doc.toJson());
            patients.add(documentToPatient(doc));
        }
        
        return patients;
    }
    
    /**
     * Egy beteg lekérdezése az azonosítója alapján
     */
    public Patient getPatientById(String patientId) {
        Document doc = patientsCollection.find(Filters.eq("patientId", patientId)).first();
        
        if (doc == null) {
            System.out.println("Nem található beteg ezzel az azonosítóval: " + patientId);
            return null;
        }
        
        return documentToPatient(doc);
    }
    
    /**
     * Egy beteg lekérdezése a BetegID alapján
     */
    public Patient getPatientByBetegId(String betegId) {
        Document doc = patientsCollection.find(Filters.eq("BetegID", betegId)).first();
        
        if (doc == null) {
            System.out.println("Nem található beteg ezzel az azonosítóval: " + betegId);
            return null;
        }
        
        return documentToPatient(doc);
    }
    
    /**
     * Egy beteg vizsgálatainak lekérdezése a patientId alapján
     */
    public List<Examination> getExaminationsByPatientId(String patientId) {
        List<Examination> examinations = new ArrayList<>();
        
        for (Document doc : examinationsCollection.find(Filters.eq("patientId", patientId))) {
            examinations.add(documentToExamination(doc));
        }
        
        return examinations;
    }
    
    /**
     * Egy vizsgálat lekérdezése azonosító alapján
     */
    public Examination getExaminationById(String examinationId) {
        Document doc = examinationsCollection.find(Filters.eq("_id", new ObjectId(examinationId))).first();
        
        if (doc == null) {
            System.out.println("Nem található vizsgálat ezzel az azonosítóval: " + examinationId);
            return null;
        }
        
        return documentToExamination(doc);
    }
    
    /**
     * Beteg adatainak mentése vagy frissítése
     */
    public void savePatient(Patient patient) {
        Document doc = patientToDocument(patient);
        
        if (patient.getId() != null && !patient.getId().isEmpty()) {
            // Frissítés
            patientsCollection.replaceOne(
                    Filters.eq("_id", new ObjectId(patient.getId())),
                    doc
            );
        } else {
            // Új beteg létrehozása
            patientsCollection.insertOne(doc);
        }
    }
    
    /**
     * Vizsgálat adatainak mentése vagy frissítése
     */
    public void saveExamination(Examination examination) {
        Document doc = examinationToDocument(examination);
        
        if (examination.getId() != null && !examination.getId().isEmpty()) {
            // Frissítés
            examinationsCollection.replaceOne(
                    Filters.eq("_id", new ObjectId(examination.getId())),
                    doc
            );
        } else {
            // Új vizsgálat létrehozása
            examinationsCollection.insertOne(doc);
        }
    }
    
    /**
     * MongoDB Document átalakítása Patient objektummá
     */
    private Patient documentToPatient(Document doc) {
        Patient patient = new Patient();
        
        if (doc.getObjectId("_id") != null) {
            patient.setId(doc.getObjectId("_id").toString());
        }
        
        patient.setBetegID(doc.getString("BetegID"));
        patient.setPatientId(doc.getString("patientId"));
        patient.setName(doc.getString("name"));
        
        // TorzsInformaciok kinyerése
        Document torzsInfo = (Document) doc.get("TorzsInformaciok");
        if (torzsInfo != null) {
            patient.setAnyjaNeve(torzsInfo.getString("AnyjaNeve"));
            patient.setSzuletesiHely(torzsInfo.getString("SzuletesiHely"));
            patient.setSzuletesiIdo(torzsInfo.getString("SzuletesiIdo"));
            patient.setTajSzam(torzsInfo.getString("TAJSzam"));
            patient.setNem(torzsInfo.getString("Nem"));
        }
        
        // Krónikus betegségek kinyerése
        Document kronikusBetegsegek = (Document) doc.get("KronikusBetegsegek");
        if (kronikusBetegsegek != null) {
            List<Document> betegsegList = (List<Document>) kronikusBetegsegek.get("Betegseg");
            if (betegsegList != null) {
                for (Document betegseg : betegsegList) {
                    // Használjuk a konstruktort a setter metódusok helyett
                    Patient.Betegseg b = new Patient.Betegseg(
                        betegseg.getString("DiagnozisKod"),
                        betegseg.getString("DiagnozisNev"),
                        betegseg.getString("DiagnozisIdopont")
                    );
                    patient.getKronikusBetegsegek().add(b);
                }
            }
        }
        
        // Rendszeres gyógyszerek kinyerése
        Document rendszeresGyogyszerek = (Document) doc.get("RendszeresGyogyszerek");
        if (rendszeresGyogyszerek != null) {
            List<Document> gyogyszerList = (List<Document>) rendszeresGyogyszerek.get("Gyogyszer");
            if (gyogyszerList != null) {
                for (Document gyogyszer : gyogyszerList) {
                    // Használjuk a konstruktort a setter metódusok helyett
                    Patient.Gyogyszer gy = new Patient.Gyogyszer(
                        gyogyszer.getString("Nev"),
                        gyogyszer.getString("Adagolas"),
                        gyogyszer.getString("Gyakorisag"),
                        gyogyszer.getString("KezdesIdopontja")
                    );
                    patient.getRendszeresGyogyszerek().add(gy);
                }
            }
        }
        
        // Kórelőzmény kinyerése
        Document korelozmenySzovegesen = (Document) doc.get("KorelozmenySzovegesen");
        if (korelozmenySzovegesen != null) {
            patient.setKorelozmenySzoveg(korelozmenySzovegesen.getString("Osszefoglalo"));
            patient.setElsoLatogatasIdopontja(korelozmenySzovegesen.getString("ElsoLatogatasIdopontja"));
            patient.setUtolsoFrissitesIdopontja(korelozmenySzovegesen.getString("UtolsoFrissitesIdopontja"));
        }
        
        return patient;
    }
    
    /**
     * MongoDB Document átalakítása Examination objektummá
     */
    private Examination documentToExamination(Document doc) {
        Examination examination = new Examination();
        
        if (doc.getObjectId("_id") != null) {
            examination.setId(doc.getObjectId("_id").toString());
        }
        
        examination.setBetegID(doc.getString("BetegID"));
        examination.setPatientId(doc.getString("patientId"));
        examination.setVizsgalatIdopontja(doc.getString("VizsgalatIdopontja"));
        examination.setOrvosID(doc.getString("OrvosID"));
        examination.setVizsgalatTipusa(doc.getString("VizsgalatTipusa"));
        
        // Status adatok kinyerése
        Document status = (Document) doc.get("Status");
        if (status != null) {
            examination.setKardiovaszkularis(status.getString("KardiovaszkulárisStatus"));
            examination.setPulmonalis(status.getString("PulmonalisStatus"));
            examination.setAbdominalis(status.getString("AbdominalisStatus"));
            examination.setMozgasszervi(status.getString("MozgasszerviStatus"));
            examination.setEgyebMegallapitasok(status.getString("EgyebMegallapitasok"));
        }
        
        // Diagnózisok kinyerése
        Document diagnozis = (Document) doc.get("Diagnozis");
        if (diagnozis != null) {
            List<Document> diagnozisLista = (List<Document>) diagnozis.get("MegallapitottBetegseg");
            if (diagnozisLista != null) {
                for (Document d : diagnozisLista) {
                    Examination.Diagnozis diag = new Examination.Diagnozis();
                    diag.setDiagnozisKod(d.getString("DiagnozisKod"));
                    diag.setDiagnozisNev(d.getString("DiagnozisNev"));
                    diag.setLeiras(d.getString("Leiras"));
                    examination.getDiagnozisok().add(diag);
                }
            }
        }
        
        // Terápia és gyógyszerek kinyerése
        Document terapia = (Document) doc.get("Terapia");
        if (terapia != null) {
            List<Document> gyogyszerList = (List<Document>) terapia.get("Gyogyszer");
            if (gyogyszerList != null) {
                for (Document gy : gyogyszerList) {
                    Examination.Gyogyszer gyogyszer = new Examination.Gyogyszer();
                    gyogyszer.setNev(gy.getString("Nev"));
                    gyogyszer.setAdagolas(gy.getString("Adagolas"));
                    gyogyszer.setGyakorisag(gy.getString("Gyakorisag"));
                    gyogyszer.setKezdesIdopontja(gy.getString("KezdesIdopontja"));
                    gyogyszer.setUtasitasok(gy.getString("Utasitasok"));
                    examination.getGyogyszerek().add(gyogyszer);
                }
            }
            
            examination.setEgyebKezelesek(terapia.getString("EgyebKezelesek"));
        }
        
        // Tevékenységek kinyerése
        Document tevekenysegek = (Document) doc.get("Tevekenysegek");
        if (tevekenysegek != null) {
            List<Document> tevekenysegList = (List<Document>) tevekenysegek.get("Tevekenyseg");
            if (tevekenysegList != null) {
                for (Document t : tevekenysegList) {
                    Examination.Tevekenyseg tevekenyseg = new Examination.Tevekenyseg();
                    tevekenyseg.setTevekenysegTipusa(t.getString("TevekenysegTipusa"));
                    tevekenyseg.setLeiras(t.getString("Leiras"));
                    tevekenyseg.setEredmeny(t.getString("Eredmeny"));
                    examination.getTevekenysegek().add(tevekenyseg);
                }
            }
        }
        
        return examination;
    }
    
    /**
     * Patient objektum átalakítása MongoDB Document-té
     * (Egyszerűsített verzió, csak a mentéshez szükséges mezőkkel)
     */
    private Document patientToDocument(Patient patient) {
        Document doc = new Document();
        
        // Csak akkor állítjuk be az ID-t, ha már létezik
        if (patient.getId() != null && !patient.getId().isEmpty()) {
            doc.append("_id", new ObjectId(patient.getId()));
        }
        
        doc.append("BetegID", patient.getBetegID())
           .append("patientId", patient.getPatientId())
           .append("name", patient.getName());
        
        // Törzsadatok
        Document torzsInfo = new Document()
                .append("Nev", patient.getName())
                .append("AnyjaNeve", patient.getAnyjaNeve())
                .append("SzuletesiHely", patient.getSzuletesiHely())
                .append("SzuletesiIdo", patient.getSzuletesiIdo())
                .append("TAJSzam", patient.getTajSzam())
                .append("Nem", patient.getNem());
        
        doc.append("TorzsInformaciok", torzsInfo);
        
        // Krónikus betegségek
        List<Document> betegsegDocList = new ArrayList<>();
        for (Patient.Betegseg b : patient.getKronikusBetegsegek()) {
            Document betegsegDoc = new Document()
                    .append("DiagnozisKod", b.getDiagnozisKod())
                    .append("DiagnozisNev", b.getDiagnozisNev())
                    .append("DiagnozisIdopont", b.getDiagnozisIdopont());
            betegsegDocList.add(betegsegDoc);
        }
        
        doc.append("KronikusBetegsegek", new Document("Betegseg", betegsegDocList));
        
        // Rendszeres gyógyszerek
        List<Document> gyogyszerDocList = new ArrayList<>();
        for (Patient.Gyogyszer gy : patient.getRendszeresGyogyszerek()) {
            Document gyogyszerDoc = new Document()
                    .append("Nev", gy.getNev())
                    .append("Adagolas", gy.getAdagolas())
                    .append("Gyakorisag", gy.getGyakorisag())
                    .append("KezdesIdopontja", gy.getKezdesIdopontja());
            gyogyszerDocList.add(gyogyszerDoc);
        }
        
        doc.append("RendszeresGyogyszerek", new Document("Gyogyszer", gyogyszerDocList));
        
        // Kórelőzmény
        Document korelozmenySzovegesen = new Document()
                .append("Osszefoglalo", patient.getKorelozmenySzoveg())
                .append("ElsoLatogatasIdopontja", patient.getElsoLatogatasIdopontja())
                .append("UtolsoFrissitesIdopontja", patient.getUtolsoFrissitesIdopontja());
        
        doc.append("KorelozmenySzovegesen", korelozmenySzovegesen);
        
        return doc;
    }
    
    /**
     * Examination objektum átalakítása MongoDB Document-té
     * (Egyszerűsített verzió, csak a mentéshez szükséges mezőkkel)
     */
    private Document examinationToDocument(Examination examination) {
        Document doc = new Document();
        
        // Csak akkor állítjuk be az ID-t, ha már létezik
        if (examination.getId() != null && ObjectId.isValid(examination.getId())) {
            doc.append("_id", new ObjectId(examination.getId()));
        }
        
    
        
        doc.append("BetegID", examination.getBetegID())
           .append("patientId", examination.getPatientId())
           .append("VizsgalatIdopontja", examination.getVizsgalatIdopontja())
           .append("OrvosID", examination.getOrvosID())
           .append("VizsgalatTipusa", examination.getVizsgalatTipusa());
        
        // Status
        Document status = new Document()
                .append("KardiovaszkulárisStatus", examination.getKardiovaszkularis())
                .append("PulmonalisStatus", examination.getPulmonalis())
                .append("AbdominalisStatus", examination.getAbdominalis())
                .append("MozgasszerviStatus", examination.getMozgasszervi())
                .append("EgyebMegallapitasok", examination.getEgyebMegallapitasok());
        
        doc.append("Status", status);
        
        // Diagnózisok
        List<Document> diagnozisDocList = new ArrayList<>();
        for (Examination.Diagnozis d : examination.getDiagnozisok()) {
            Document diagnozisDoc = new Document()
                    .append("DiagnozisKod", d.getDiagnozisKod())
                    .append("DiagnozisNev", d.getDiagnozisNev())
                    .append("Leiras", d.getLeiras());
            diagnozisDocList.add(diagnozisDoc);
        }
        
        doc.append("Diagnozis", new Document("MegallapitottBetegseg", diagnozisDocList));
        
        // Terápia és gyógyszerek
        List<Document> gyogyszerDocList = new ArrayList<>();
        for (Examination.Gyogyszer gy : examination.getGyogyszerek()) {
            Document gyogyszerDoc = new Document()
                    .append("Nev", gy.getNev())
                    .append("Adagolas", gy.getAdagolas())
                    .append("Gyakorisag", gy.getGyakorisag())
                    .append("KezdesIdopontja", gy.getKezdesIdopontja())
                    .append("Utasitasok", gy.getUtasitasok());
            gyogyszerDocList.add(gyogyszerDoc);
        }
        
        Document terapia = new Document()
                .append("Gyogyszer", gyogyszerDocList)
                .append("EgyebKezelesek", examination.getEgyebKezelesek());
        
        doc.append("Terapia", terapia);
        
        // Tevékenységek
        List<Document> tevekenysegDocList = new ArrayList<>();
        for (Examination.Tevekenyseg t : examination.getTevekenysegek()) {
            Document tevekenysegDoc = new Document()
                    .append("TevekenysegTipusa", t.getTevekenysegTipusa())
                    .append("Leiras", t.getLeiras())
                    .append("Eredmeny", t.getEredmeny());
            tevekenysegDocList.add(tevekenysegDoc);
        }
        
        doc.append("Tevekenysegek", new Document("Tevekenyseg", tevekenysegDocList));
        
        return doc;
    }
    
    /**
     * MongoDB kapcsolat lezárása
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB kapcsolat lezárva");
        }
    }
}