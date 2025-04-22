package hu.ujvari.data;

import java.util.ArrayList;
import java.util.List;

public class Patient {
    private String id; // MongoDB _id
    private String betegID;
    private String patientId; //eXistDB azonosító
    private String name;
    
    // Alapvető törzsadatok (leggyakrabban használt mezők)
    private String anyjaNeve;
    private String szuletesiHely;
    private String szuletesiIdo;
    private String tajSzam;
    private String nem;
    
    // Komplex adatszerkezetek listákként
    private List<Betegseg> kronikusBetegsegek;
    private List<Gyogyszer> rendszeresGyogyszerek;
    
    // Kórelőzmény
    private String korelozmenySzoveg;
    private String elsoLatogatasIdopontja;
    private String utolsoFrissitesIdopontja;
    
    // Konstruktorok
    public Patient() {
        this.kronikusBetegsegek = new ArrayList<>();
        this.rendszeresGyogyszerek = new ArrayList<>();
    }
    
    
    
    // Segédosztályok a listák elemeihez
    public static class Betegseg {
        private String diagnozisKod;
        private String diagnozisNev;
        private String diagnozisIdopont;
        public Betegseg(String diagnozisKod, String diagnozisNev, String diagnozisIdopont) {
            this.diagnozisKod = diagnozisKod;
            this.diagnozisNev = diagnozisNev;
            this.diagnozisIdopont = diagnozisIdopont;
        }
        public String getDiagnozisKod() {
            return diagnozisKod;
        }
        public void setDiagnozisKod(String diagnozisKod) {
            this.diagnozisKod = diagnozisKod;
        }
        public String getDiagnozisNev() {
            return diagnozisNev;
        }
        public void setDiagnozisNev(String diagnozisNev) {
            this.diagnozisNev = diagnozisNev;
        }
        public String getDiagnozisIdopont() {
            return diagnozisIdopont;
        }
        public void setDiagnozisIdopont(String diagnozisIdopont) {
            this.diagnozisIdopont = diagnozisIdopont;
        }
        
        
    }
    
    public static class Gyogyszer {
        private String nev;
        private String adagolas;
        private String gyakorisag;
        private String kezdesIdopontja;
        public Gyogyszer(String nev, String adagolas, String gyakorisag, String kezdesIdopontja) {
            this.nev = nev;
            this.adagolas = adagolas;
            this.gyakorisag = gyakorisag;
            this.kezdesIdopontja = kezdesIdopontja;
        }
        public String getNev() {
            return nev;
        }
        public void setNev(String nev) {
            this.nev = nev;
        }
        public String getAdagolas() {
            return adagolas;
        }
        public void setAdagolas(String adagolas) {
            this.adagolas = adagolas;
        }
        public String getGyakorisag() {
            return gyakorisag;
        }
        public void setGyakorisag(String gyakorisag) {
            this.gyakorisag = gyakorisag;
        }
        public String getKezdesIdopontja() {
            return kezdesIdopontja;
        }
        public void setKezdesIdopontja(String kezdesIdopontja) {
            this.kezdesIdopontja = kezdesIdopontja;
        }
        
      
    }
    
    // Az egész MongoDB dokumentum visszaadása String formában
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Beteg adatok:\n");
        sb.append("  ID: ").append(betegID).append("\n");
        sb.append("  Név: ").append(name).append("\n");
        sb.append("  Születési hely, idő: ").append(szuletesiHely).append(", ").append(szuletesiIdo).append("\n");
        sb.append("  TAJ szám: ").append(tajSzam).append("\n");
        sb.append("  Nem: ").append(nem).append("\n\n");
        
        sb.append("Krónikus betegségek:\n");
        for (Betegseg b : kronikusBetegsegek) {
            sb.append("  - ").append(b.diagnozisNev).append(" (").append(b.diagnozisKod).append("), ");
            sb.append("diagnosztizálva: ").append(b.diagnozisIdopont).append("\n");
        }
        sb.append("\n");
        
        sb.append("Rendszeres gyógyszerek:\n");
        for (Gyogyszer gy : rendszeresGyogyszerek) {
            sb.append("  - ").append(gy.nev).append(", ");
            sb.append(gy.adagolas).append(", ").append(gy.gyakorisag).append("\n");
        }
        sb.append("\n");
        
        sb.append("Kórelőzmény: ").append(korelozmenySzoveg).append("\n");
        
        return sb.toString();
    }

    public List<Betegseg> getKronikusBetegsegek() {
        return kronikusBetegsegek;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBetegID() {
        return betegID;
    }

    public void setBetegID(String betegID) {
        this.betegID = betegID;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTajSzam() {
        return tajSzam;
    }

    public void setTajSzam(String tajSzam) {
        this.tajSzam = tajSzam;
    }

    public String getAnyjaNeve() {
        return anyjaNeve;
    }

    public void setAnyjaNeve(String anyjaNeve) {
        this.anyjaNeve = anyjaNeve;
    }

    public String getSzuletesiHely() {
        return szuletesiHely;
    }

    public void setSzuletesiHely(String szuletesiHely) {
        this.szuletesiHely = szuletesiHely;
    }

    public String getSzuletesiIdo() {
        return szuletesiIdo;
    }

    public void setSzuletesiIdo(String szuletesiIdo) {
        this.szuletesiIdo = szuletesiIdo;
    }

    public String getNem() {
        return nem;
    }

    public void setNem(String nem) {
        this.nem = nem;
    }

    public void setKronikusBetegsegek(List<Betegseg> kronikusBetegsegek) {
        this.kronikusBetegsegek = kronikusBetegsegek;
    }

    public List<Gyogyszer> getRendszeresGyogyszerek() {
        return rendszeresGyogyszerek;
    }

    public void setRendszeresGyogyszerek(List<Gyogyszer> rendszeresGyogyszerek) {
        this.rendszeresGyogyszerek = rendszeresGyogyszerek;
    }

    public String getKorelozmenySzoveg() {
        return korelozmenySzoveg;
    }

    public void setKorelozmenySzoveg(String korelozmenySzoveg) {
        this.korelozmenySzoveg = korelozmenySzoveg;
    }

    public String getElsoLatogatasIdopontja() {
        return elsoLatogatasIdopontja;
    }

    public void setElsoLatogatasIdopontja(String elsoLatogatasIdopontja) {
        this.elsoLatogatasIdopontja = elsoLatogatasIdopontja;
    }

    public String getUtolsoFrissitesIdopontja() {
        return utolsoFrissitesIdopontja;
    }

    public void setUtolsoFrissitesIdopontja(String utolsoFrissitesIdopontja) {
        this.utolsoFrissitesIdopontja = utolsoFrissitesIdopontja;
    }
}