package hu.ujvari.ui;

import java.util.ArrayList;
import java.util.Date;

import hu.ujvari.data.Examination;
import hu.ujvari.data.Patient;
import hu.ujvari.service.PatientService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ExaminationFormWindow {

    private final Patient patient;
    private final PatientService patientService;

    public ExaminationFormWindow(Patient patient, PatientService patientService) {
        this.patient = patient;
        this.patientService = patientService;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Új vizsgálat rögzítése - " + patient.getName());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField vizsgalatTipusField = new TextField();
        TextField orvosIdField = new TextField();
        TextArea kardiovaszkularisArea = new TextArea();
        TextArea pulmonalisArea = new TextArea();
        TextArea abdominalisArea = new TextArea();
        TextArea mozgasszerviArea = new TextArea();
        TextArea egyebStatusArea = new TextArea();
        TextArea egyebKezelesekArea = new TextArea();

        grid.add(new Label("Vizsgálat típusa:"), 0, 0);
        grid.add(vizsgalatTipusField, 1, 0);

        grid.add(new Label("Orvos ID:"), 0, 1);
        grid.add(orvosIdField, 1, 1);

        grid.add(new Label("Kardiovaszkuláris státusz:"), 0, 2);
        grid.add(kardiovaszkularisArea, 1, 2);

        grid.add(new Label("Pulmonális státusz:"), 0, 3);
        grid.add(pulmonalisArea, 1, 3);

        grid.add(new Label("Abdominális státusz:"), 0, 4);
        grid.add(abdominalisArea, 1, 4);

        grid.add(new Label("Mozgásszervi státusz:"), 0, 5);
        grid.add(mozgasszerviArea, 1, 5);

        grid.add(new Label("Egyéb megállapítások:"), 0, 6);
        grid.add(egyebStatusArea, 1, 6);

        grid.add(new Label("Egyéb kezelések:"), 0, 7);
        grid.add(egyebKezelesekArea, 1, 7);

        Button saveButton = new Button("Mentés");
        saveButton.setOnAction(e -> {
            try {
                Examination exam = new Examination();
                exam.setPatientId(patient.getPatientId());
                exam.setBetegID(patient.getBetegID());
                exam.setVizsgalatTipusa(vizsgalatTipusField.getText());
                exam.setOrvosID(orvosIdField.getText());
                exam.setVizsgalatIdopontja(new Date());

                exam.setKardiovaszkularis(kardiovaszkularisArea.getText());
                exam.setPulmonalis(pulmonalisArea.getText());
                exam.setAbdominalis(abdominalisArea.getText());
                exam.setMozgasszervi(mozgasszerviArea.getText());
                exam.setEgyebMegallapitasok(egyebStatusArea.getText());
                exam.setEgyebKezelesek(egyebKezelesekArea.getText());

                exam.setDiagnozisok(new ArrayList<>()); 
                exam.setGyogyszerek(new ArrayList<>());
                exam.setTevekenysegek(new ArrayList<>());

                patientService.saveExamination(exam);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Siker");
                alert.setHeaderText(null);
                alert.setContentText("A vizsgálat sikeresen mentésre került.");
                alert.showAndWait();

                stage.close();
            } catch (SecurityException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Hiba");
                alert.setHeaderText("Nem sikerült a vizsgálat mentése");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));
        buttonBox.getChildren().add(saveButton);

        grid.add(buttonBox, 1, 8);

        Scene scene = new Scene(grid, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
}
