package hu.ujvari.db.existdb;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exist.xmldb.EXistResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

import hu.ujvari.data.EkgData;



public class EkgDbConnector {
    private String driver = "org.exist.xmldb.DatabaseImpl";
    private String uri;
    private String username;
    private String password;
    private String collection;
    
    /**
     * EkgDbConnector konstruktor
     * 
     * @param uri eXist-DB URI (pl. "xmldb:exist://localhost:8080/exist/xmlrpc")
     * @param username Felhasználónév
     * @param password Jelszó
     * @param collection Gyűjtemény elérési útja (pl. "/db/ekgdata")
     */
    public EkgDbConnector(String uri, String username, String password, String collection) {
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.collection = collection;
        
        try {
            // XMLDB driver inicializálása
            Class<?> cl = Class.forName(driver);
            Database database = (Database) cl.newInstance();
            database.setProperty("create-database", "true");
            DatabaseManager.registerDatabase(database);
            System.out.println("eXist-DB kapcsolat létrehozva: " + uri + collection);
        } catch (Exception e) {
            System.err.println("Hiba az eXist-DB kapcsolat inicializálásakor: " + e.getMessage());
        }
    }
    
    /**
     * Egy beteg összes EKG adatának lekérdezése a patientId alapján
     * 
     * @param patientId Beteg azonosítója
     * @return Lista a beteg EKG adataival
     */
    public List<EkgData> getEkgsByPatientId(String patientId) {
        List<EkgData> ekgList = new ArrayList<>();
        
        try {
            // Kapcsolódás a gyűjteményhez
            Collection col = DatabaseManager.getCollection(uri + collection, username, password);
            XPathQueryService xpathService = (XPathQueryService) col.getService("XPathQueryService", "1.0");
            
            // XPath lekérdezés a beteg EKG-inak keresésére
            String xpathQuery = "//ekgData[patientId='" + patientId + "']";
            ResourceSet result = xpathService.query(xpathQuery);
            ResourceIterator iterator = result.getIterator();
            
            while (iterator.hasMoreResources()) {
                Resource res = iterator.nextResource();
                String xmlContent = (String) res.getContent();
                
                // XML feldolgozása és EkgData objektum létrehozása
                EkgData ekgData = parseEkgXml(xmlContent);
                ekgList.add(ekgData);
                
                ((EXistResource) res).freeResources();
            }
            
        } catch (Exception e) {
            System.err.println("Hiba az eXist-DB lekérdezésekor: " + e.getMessage());
        }
        System.out.println("Lekérdezett EKG-k száma: " + ekgList.size());

        return ekgList;
    }
    
    /**
     * Egy EKG adat lekérdezése azonosító alapján
     * 
     * @param ekgId EKG azonosítója
     * @return EkgData objektum vagy null, ha nem található
     */
    public EkgData getEkgById(String ekgId) {
        try {
            // Kapcsolódás a gyűjteményhez
            Collection col = DatabaseManager.getCollection(uri + collection, username, password);
            
            // XML erőforrás lekérése
            XMLResource document = (XMLResource) col.getResource(ekgId + ".xml");
            
            if (document != null) {
                String xmlContent = (String) document.getContent();
                return parseEkgXml(xmlContent);
            }
            
        } catch (Exception e) {
            System.err.println("Hiba az EKG lekérdezésekor: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * EKG adat mentése az eXist-DB-be
     * 
     * @param ekgData Mentendő EKG adat
     * @return true, ha sikeres volt a mentés
     */
    public boolean saveEkgData(EkgData ekgData) {
        try {
            // Kapcsolódás a gyűjteményhez
            Collection col = DatabaseManager.getCollection(uri + collection, username, password);
            
            // XML erőforrás létrehozása
            XMLResource document = (XMLResource) col.createResource(
                    ekgData.getEkgId() + ".xml", "XMLResource");
            
            // XML tartalom létrehozása az EkgData objektumból
            String xmlContent = createEkgXml(ekgData);
            document.setContent(xmlContent);
            
            // Dokumentum mentése
            col.storeResource(document);
            
            System.out.println("EKG adatok mentve: " + ekgData.getEkgId());
            return true;
            
        } catch (Exception e) {
            System.err.println("Hiba az EKG mentésekor: " + e.getMessage());
            return false;
        }
    }
    
        
    /**
     * EkgData objektum átalakítása XML-lé
     */
    private String createEkgXml(EkgData ekgData) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<ekgData>\n");
        xml.append("  <ekgId>").append(ekgData.getEkgId()).append("</ekgId>\n");
        xml.append("  <patientId>").append(ekgData.getPatientId()).append("</patientId>\n");
        
        if (ekgData.getBetegID() != null) {
            xml.append("  <betegID>").append(ekgData.getBetegID()).append("</betegID>\n");
        }
        
        if (ekgData.getRecordDate() != null) {
            xml.append("  <recordDate>").append(ekgData.getRecordDate()).append("</recordDate>\n");
        }
        
        if (ekgData.getRecordedByUserId() != null) {
            xml.append("  <recordedByUserId>").append(ekgData.getRecordedByUserId())
               .append("</recordedByUserId>\n");
        }
        
        // Itt tárolnánk az EKG adatokat, pl.:
        xml.append("  <ekgContent>\n");
        xml.append("    <!-- EKG adatok itt -->\n");
        xml.append("  </ekgContent>\n");
        
        xml.append("</ekgData>");
        return xml.toString();
    }

    /**
     * XML tartalomból kinyeri az EKG jeleket
     */
    private void extractSignalsFromXml(EkgData ekgData) {
        try {
            String xmlContent = ekgData.getXmlContent();
            
            // XML értelmezése
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xmlContent.getBytes());
            Document xmlDocument = dBuilder.parse(is);
            xmlDocument.getDocumentElement().normalize();
            
            // Elvezetések komponenseinek keresése
            NodeList components = xmlDocument.getElementsByTagName("component");
            List<EkgData.Signal> signals = new ArrayList<>();
            
            for (int i = 0; i < components.getLength(); i++) {
                Node node = components.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) continue;
                
                Element component = (Element) node;
                NodeList codeList = component.getElementsByTagName("code");
                
                if (codeList.getLength() > 0) {
                    Element codeElement = (Element) codeList.item(0);
                    String code = codeElement.getAttribute("code");
                    
                    if (code != null && code.startsWith("MDC_ECG_LEAD_")) {
                        // Ez egy elvezetés komponens
                        Element valueElement = (Element) component.getElementsByTagName("value").item(0);
                        if (valueElement == null) continue;
                        
                        Element originElement = (Element) valueElement.getElementsByTagName("origin").item(0);
                        Element scaleElement = (Element) valueElement.getElementsByTagName("scale").item(0);
                        Element digitsElement = (Element) valueElement.getElementsByTagName("digits").item(0);
                        
                        if (originElement == null || scaleElement == null || digitsElement == null) continue;
                        
                        double originVal = Double.parseDouble(originElement.getAttribute("value"));
                        String originUnit = originElement.getAttribute("unit");
                        
                        double scaleVal = Double.parseDouble(scaleElement.getAttribute("value"));
                        String scaleUnit = scaleElement.getAttribute("unit");
                        
                        String digitsText = digitsElement.getTextContent().trim();
                        List<Double> values = new ArrayList<>();
                        for (String token : digitsText.split("\\s+")) {
                            try {
                                values.add(Double.parseDouble(token));
                            } catch (NumberFormatException ignored) {}
                        }
                        
                        EkgData.Signal signal = new EkgData.Signal(code, values, originVal, originUnit, scaleVal, scaleUnit);
                        signals.add(signal);
                    }
                }
            }
            
            ekgData.setSignals(signals);
            System.out.println("Beolvasott elvezetések száma: " + signals.size());
            
        } catch (Exception e) {
            System.err.println("Hiba az EKG jelek kinyerésekor: " + e.getMessage());
        }
    }
    
    /**
     * XML tartalom feldolgozása EkgData objektummá
     */
    private EkgData parseEkgXml(String xmlContent) {
        EkgData ekgData = new EkgData();
        
        // Alapadatok kinyerése egyszerű string műveletekkel
        if (xmlContent.contains("<ekgId>") && xmlContent.contains("</ekgId>")) {
            int start = xmlContent.indexOf("<ekgId>") + 7;
            int end = xmlContent.indexOf("</ekgId>");
            ekgData.setEkgId(xmlContent.substring(start, end));
        }
        
        if (xmlContent.contains("<patientId>") && xmlContent.contains("</patientId>")) {
            int start = xmlContent.indexOf("<patientId>") + 11;
            int end = xmlContent.indexOf("</patientId>");
            ekgData.setPatientId(xmlContent.substring(start, end));
        }
        
        if (xmlContent.contains("<betegID>") && xmlContent.contains("</betegID>")) {
            int start = xmlContent.indexOf("<betegID>") + 9;
            int end = xmlContent.indexOf("</betegID>");
            ekgData.setBetegID(xmlContent.substring(start, end));
        }
        
        if (xmlContent.contains("<recordDate>") && xmlContent.contains("</recordDate>")) {
            int start = xmlContent.indexOf("<recordDate>") + 12;
            int end = xmlContent.indexOf("</recordDate>");
            ekgData.setRecordDate(xmlContent.substring(start, end));
        }
        
        if (xmlContent.contains("<recordedByUserId>") && xmlContent.contains("</recordedByUserId>")) {
            int start = xmlContent.indexOf("<recordedByUserId>") + 18;
            int end = xmlContent.indexOf("</recordedByUserId>");
            ekgData.setRecordedByUserId(xmlContent.substring(start, end));
        }
        
        // Az XML tartalom tárolása
        ekgData.setXmlContent(xmlContent);
        
        // EKG jelek kinyerése az XML-ből
        extractSignalsFromXml(ekgData);
        
        return ekgData;
    }
}