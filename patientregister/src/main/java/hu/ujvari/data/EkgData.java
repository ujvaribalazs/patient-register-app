package hu.ujvari.data;

import java.util.ArrayList;
import java.util.List;

public class EkgData {
    private String ekgId;
    private String patientId;
    private String betegID;
    private String recordDate;
    private String recordedByUserId;
    private String xmlContent;  // Az EKG adatok XML formátumban
    
    // EKG jel adatok
    private List<Signal> signals = new ArrayList<>();
    
    // Getterek és setterek
    public String getEkgId() {
        return ekgId;
    }

    public void setEkgId(String ekgId) {
        this.ekgId = ekgId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getBetegID() {
        return betegID;
    }

    public void setBetegID(String betegID) {
        this.betegID = betegID;
    }

    public String getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate;
    }

    public String getRecordedByUserId() {
        return recordedByUserId;
    }

    public void setRecordedByUserId(String recordedByUserId) {
        this.recordedByUserId = recordedByUserId;
    }

    public String getXmlContent() {
        return xmlContent;
    }

    public void setXmlContent(String xmlContent) {
        this.xmlContent = xmlContent;
    }
    
    public List<Signal> getSignals() {
        return signals;
    }
    
    public void setSignals(List<Signal> signals) {
        this.signals = signals;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EKG adatok:\n");
        sb.append("  ID: ").append(ekgId).append("\n");
        sb.append("  Beteg ID: ").append(patientId).append("\n");
        sb.append("  Dátum: ").append(recordDate).append("\n");
        sb.append("  Rögzítette: ").append(recordedByUserId).append("\n");
        sb.append("  Elvezetések száma: ").append(signals.size()).append("\n");
        
        // Ha vannak elvezetések, kiírjuk az elsőt példaként
        if (!signals.isEmpty()) {
            Signal firstSignal = signals.get(0);
            sb.append("\nPélda elvezetés: ").append(firstSignal.getName()).append("\n");
            sb.append("  Értékek száma: ").append(firstSignal.getValues().size()).append("\n");
            sb.append("  Skála: ").append(firstSignal.getScaleVal()).append(" ").append(firstSignal.getScaleUnit()).append("\n");
        }
        
        return sb.toString();
    }
    
    // Belső osztály a jel tárolására
    public static class Signal {
        private String name;
        private List<Double> values;
        private double originVal;
        private String originUnit;
        private double scaleVal;
        private String scaleUnit;
        
        public Signal(String name, List<Double> values, double originVal, String originUnit, double scaleVal, String scaleUnit) {
            this.name = name;
            this.values = values;
            this.originVal = originVal;
            this.originUnit = originUnit;
            this.scaleVal = scaleVal;
            this.scaleUnit = scaleUnit;
        }
        
        // Getterek és setterek
        public String getName() {
            return name;
        }
        
        public List<Double> getValues() {
            return values;
        }
        
        public double getOriginVal() {
            return originVal;
        }
        
        public String getOriginUnit() {
            return originUnit;
        }
        
        public double getScaleVal() {
            return scaleVal;
        }
        
        public String getScaleUnit() {
            return scaleUnit;
        }
    }
}