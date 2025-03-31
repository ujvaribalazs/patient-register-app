package hu.ujvari.ui;

import java.util.List;

import hu.ujvari.data.EkgData;
import hu.ujvari.data.EkgData.Signal;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EkgViewWindow {
    private final EkgData ekgData;

    public EkgViewWindow(EkgData ekgData) {
        this.ekgData = ekgData;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("EKG megtekintése - Beteg: " + ekgData.getPatientId());

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Felső panel: metaadatok
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(10));
        infoBox.getChildren().addAll(
            new Label("EKG ID: " + ekgData.getEkgId()),
            new Label("Beteg ID: " + ekgData.getPatientId()),
            new Label("Rögzítés dátuma: " + ekgData.getRecordDate()),
            new Label("Rögzítette: " + ekgData.getRecordedByUserId()),
            new Label("Elvezetések száma: " + ekgData.getSignals().size())
        );
        root.setTop(infoBox);

        // Középső panel: grafikon
        if (!ekgData.getSignals().isEmpty()) {
            Signal signal = ekgData.getSignals().get(0); // első elvezetés
            LineChart<Number, Number> chart = createSignalChart(signal);
            root.setCenter(chart);
        } else {
            root.setCenter(new Label("Nincs elérhető EKG elvezetés az adatokban."));
        }

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private LineChart<Number, Number> createSignalChart(Signal signal) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Mintavételi index");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Feszültség (" + signal.getScaleUnit() + ")");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Elvezetés: " + signal.getName());
        chart.setCreateSymbols(false);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(signal.getName());

        List<Double> values = signal.getValues();
        int maxSamples = Math.min(1000, values.size()); // csak az első 1000 minta

        for (int i = 0; i < maxSamples; i++) {
            series.getData().add(new XYChart.Data<>(i, values.get(i)));
        }

        chart.getData().add(series);
        return chart;
    }
}
