package hu.ujvari.ui;

import java.util.List;

import hu.ujvari.data.Patient;
import hu.ujvari.service.PatientService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PatientDetailsWindow {
    private final Patient patient;
    public PatientDetailsWindow(Patient patient, PatientService patientService) {
        this.patient = patient;
    }

    public void show() {
    Stage stage = new Stage();
    stage.setTitle("Beteg részletes adatai - " + patient.getName());

    VBox root = new VBox(10);
    root.setPadding(new Insets(15));

    // Alapadatok
    root.getChildren().addAll(
        new Label("Név: " + patient.getName()),
        new Label("Beteg ID: " + patient.getPatientId()),
        new Label("TAJ szám: " + patient.getTajSzam()),
        new Label("Születési hely: " + patient.getSzuletesiHely()),
        new Label("Születési idő: " + patient.getSzuletesiIdo()),
        new Label("Anyja neve: " + patient.getAnyjaNeve()),
        new Label("Nem: " + patient.getNem())
    );

    // Krónikus betegségek
    List<Patient.Betegseg> betegsegek = patient.getKronikusBetegsegek();
    if (betegsegek != null && !betegsegek.isEmpty()) {
        root.getChildren().add(new Label("Krónikus betegségek:"));
        for (Patient.Betegseg b : betegsegek) {
            root.getChildren().add(new Label(" - " + b.getDiagnozisNev() + " (" + b.getDiagnozisKod() + "), diagnózis ideje: " + b.getDiagnozisIdopont()));
        }
    }

    // Gyógyszerek
    List<Patient.Gyogyszer> gyogyszerek = patient.getRendszeresGyogyszerek();
    if (gyogyszerek != null && !gyogyszerek.isEmpty()) {
        root.getChildren().add(new Label("Rendszeres gyógyszerek:"));
        for (Patient.Gyogyszer gy : gyogyszerek) {
            root.getChildren().add(new Label(" - " + gy.getNev() + " (" + gy.getAdagolas() + ", " + gy.getGyakorisag() + "), kezdés: " + gy.getKezdesIdopontja()));
        }
    }

    // Kórelőzmény
    root.getChildren().addAll(
        new Label("Kórelőzmény:"),
        new Label(patient.getKorelozmenySzoveg()),
        new Label("Első látogatás: " + patient.getElsoLatogatasIdopontja()),
        new Label("Utolsó frissítés: " + patient.getUtolsoFrissitesIdopontja())
    );

    Scene scene = new Scene(root, 600, 600);
    stage.setScene(scene);
    stage.show();
}

}
