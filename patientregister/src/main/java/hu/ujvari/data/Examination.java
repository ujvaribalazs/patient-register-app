package hu.ujvari.data;

import java.util.ArrayList;
import java.util.List;

public class Examination {
    private String id; // MongoDB _id
    private String betegID;
    private String patientId;
    private String vizsgalatIdopontja;
    private String orvosID;
    private String vizsgalatTipusa;
    
    // Status adatok (rövid változatban csak összesítés)
    private String kardiovaszkularis;
    private String pulmonalis;
    private String abdominalis;
    private String mozgasszervi;
    private String egyebMegallapitasok;
    
    // Diagnózisok és terápiák
    private List<Diagnozis> diagnozisok;
    private List<Gyogyszer> gyogyszerek;
    private String egyebKezelesek;
    
    // Tevékenységek
    private List<Tevekenyseg> tevekenysegek;
    
    // Konstruktor
    public Examination() {
        this.diagnozisok = new ArrayList<>();
        this.gyogyszerek = new ArrayList<>();
        this.tevekenysegek = new ArrayList<>();
    }
    
    // Getterek és setterek...
    
    public List<Diagnozis> getDiagnozisok() {
        return diagnozisok;
    }

    public void setDiagnozisok(List<Diagnozis> diagnozisok) {
        this.diagnozisok = diagnozisok;
    }

    public List<Gyogyszer> getGyogyszerek() {
        return gyogyszerek;
    }

    public void setGyogyszerek(List<Gyogyszer> gyogyszerek) {
        this.gyogyszerek = gyogyszerek;
    }

    public String getEgyebKezelesek() {
        return egyebKezelesek;
    }

    public void setEgyebKezelesek(String egyebKezelesek) {
        this.egyebKezelesek = egyebKezelesek;
    }

    public List<Tevekenyseg> getTevekenysegek() {
        return tevekenysegek;
    }

    public void setTevekenysegek(List<Tevekenyseg> tevekenysegek) {
        this.tevekenysegek = tevekenysegek;
    }

    // Segédosztályok a listák elemeihez
    public static class Diagnozis {
        private String diagnozisKod;
        private String diagnozisNev;
        private String leiras;
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
        public String getLeiras() {
            return leiras;
        }
        public void setLeiras(String leiras) {
            this.leiras = leiras;
        }
        
        // Getterek és setterek...
    }
    
    public static class Gyogyszer {
        private String nev;
        private String adagolas;
        private String gyakorisag;
        private String kezdesIdopontja;
        private String utasitasok;
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
        public String getUtasitasok() {
            return utasitasok;
        }
        public void setUtasitasok(String utasitasok) {
            this.utasitasok = utasitasok;
        }
        
        // Getterek és setterek...
    }
    
    public static class Tevekenyseg {
        private String tevekenysegTipusa;
        private String leiras;
        private String eredmeny;
        public String getTevekenysegTipusa() {
            return tevekenysegTipusa;
        }
        public void setTevekenysegTipusa(String tevekenysegTipusa) {
            this.tevekenysegTipusa = tevekenysegTipusa;
        }
        public String getLeiras() {
            return leiras;
        }
        public void setLeiras(String leiras) {
            this.leiras = leiras;
        }
        public String getEredmeny() {
            return eredmeny;
        }
        public void setEredmeny(String eredmeny) {
            this.eredmeny = eredmeny;
        }
        
        // Getterek és setterek...
    }
    
    // Az egész MongoDB dokumentum visszaadása String formában
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vizsgálati adatok:\n");
        sb.append("  Beteg ID: ").append(betegID).append("\n");
        sb.append("  Időpont: ").append(vizsgalatIdopontja).append("\n");
        sb.append("  Orvos ID: ").append(orvosID).append("\n");
        sb.append("  Vizsgálat típusa: ").append(vizsgalatTipusa).append("\n\n");
        
        sb.append("Státusz:\n");
        sb.append("  Kardiovaszkuláris: ").append(kardiovaszkularis).append("\n");
        sb.append("  Pulmonális: ").append(pulmonalis).append("\n");
        sb.append("  Abdominális: ").append(abdominalis).append("\n");
        sb.append("  Mozgásszervi: ").append(mozgasszervi).append("\n");
        sb.append("  Egyéb: ").append(egyebMegallapitasok).append("\n\n");
        
        sb.append("Diagnózisok:\n");
        for (Diagnozis d : diagnozisok) {
            sb.append("  - ").append(d.diagnozisNev).append(" (").append(d.diagnozisKod).append(")\n");
            sb.append("    ").append(d.leiras).append("\n");
        }
        sb.append("\n");
        
        sb.append("Gyógyszeres kezelés:\n");
        for (Gyogyszer gy : gyogyszerek) {
            sb.append("  - ").append(gy.nev).append(", ");
            sb.append(gy.adagolas).append(", ").append(gy.gyakorisag).append("\n");
            sb.append("    ").append(gy.utasitasok).append("\n");
        }
        sb.append("\n");
        
        sb.append("Tevékenységek:\n");
        for (Tevekenyseg t : tevekenysegek) {
            sb.append("  - ").append(t.tevekenysegTipusa).append(": ").append(t.leiras).append("\n");
            sb.append("    Eredmény: ").append(t.eredmeny).append("\n");
        }
        
        return sb.toString();
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

    public String getVizsgalatIdopontja() {
        return vizsgalatIdopontja;
    }

    public void setVizsgalatIdopontja(String vizsgalatIdopontja) {
        this.vizsgalatIdopontja = vizsgalatIdopontja;
    }

    public String getOrvosID() {
        return orvosID;
    }

    public void setOrvosID(String orvosID) {
        this.orvosID = orvosID;
    }

    public String getVizsgalatTipusa() {
        return vizsgalatTipusa;
    }

    public void setVizsgalatTipusa(String vizsgalatTipusa) {
        this.vizsgalatTipusa = vizsgalatTipusa;
    }

    public String getKardiovaszkularis() {
        return kardiovaszkularis;
    }

    public void setKardiovaszkularis(String kardiovaszkularis) {
        this.kardiovaszkularis = kardiovaszkularis;
    }

    public String getPulmonalis() {
        return pulmonalis;
    }

    public void setPulmonalis(String pulmonalis) {
        this.pulmonalis = pulmonalis;
    }

    public String getAbdominalis() {
        return abdominalis;
    }

    public void setAbdominalis(String abdominalis) {
        this.abdominalis = abdominalis;
    }

    public String getMozgasszervi() {
        return mozgasszervi;
    }

    public void setMozgasszervi(String mozgasszervi) {
        this.mozgasszervi = mozgasszervi;
    }

    public String getEgyebMegallapitasok() {
        return egyebMegallapitasok;
    }

    public void setEgyebMegallapitasok(String egyebMegallapitasok) {
        this.egyebMegallapitasok = egyebMegallapitasok;
    }
}