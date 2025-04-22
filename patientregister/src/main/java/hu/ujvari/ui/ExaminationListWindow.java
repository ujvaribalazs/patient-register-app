package hu.ujvari.ui;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import hu.ujvari.data.Examination;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class ExaminationListWindow {
    private final List<Examination> examinations;

    public ExaminationListWindow(List<Examination> examinations) {
        this.examinations = examinations;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Vizsgálatok listája");

        BorderPane root = new BorderPane();
        TableView<Examination> table = new TableView<>();

        // Dátum oszlop: Date → String formázva
        TableColumn<Examination, String> dateCol = new TableColumn<>("Dátum");
        dateCol.setCellValueFactory(e -> {
            Date vizsDate = e.getValue().getVizsgalatIdopontja();
            String formatted = "";
            if (vizsDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                formatted = sdf.format(vizsDate);
            }
            return new SimpleStringProperty(formatted);
        });

        TableColumn<Examination, String> typeCol = new TableColumn<>("Típus");
        typeCol.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getVizsgalatTipusa()));

        TableColumn<Examination, String> doctorCol = new TableColumn<>("Orvos ID");
        doctorCol.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getOrvosID()));

        
        table.getColumns().addAll(Arrays.asList(dateCol, typeCol, doctorCol));
       
        table.getItems().addAll(examinations);

        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                detailsArea.setText(newSel.toString());
            }
        });

     
    // 2) Fix magasság a táblázatnak (csak 2-3 sor!):
    table.setPrefHeight(120);
    table.setMaxHeight(120);

   
    // minimális magasság:
    detailsArea.setPrefHeight(300);
    // a VBox-ban felfele nőhet:
    VBox.setVgrow(detailsArea, Priority.ALWAYS);

    // 4) A VBox továbbra is, de most a detailsArea kitölti a maradékot
    VBox vbox = new VBox(10, table, new Label("Részletek:"), detailsArea);
    vbox.setPadding(new Insets(10));
    root.setCenter(vbox);

    // 5) Induló ablakméret
    Scene scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.show();
    }
}
