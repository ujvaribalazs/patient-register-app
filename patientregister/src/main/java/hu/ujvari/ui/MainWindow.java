package hu.ujvari.ui;

import java.util.Arrays;
import java.util.List;

import hu.ujvari.data.Examination;
import hu.ujvari.data.Patient;
import hu.ujvari.data.User;
import hu.ujvari.service.AuthService;
import hu.ujvari.service.PatientService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*
 * MainWindow osztály:
 * - Felelős a fő JavaFX ablak felépítéséért és megjelenítéséért.
 * - AuthService segítségével ellenőrzi a bejelentkezett felhasználót.
 * - Menüsorban biztosít lehetőségeket (kilépés, betegek listázása, vizsgálatok, EKG, kijelentkezés).
 * - Középen egy TableView mutatja a betegek listáját, alul státuszsor a felhasználó adataival.
 * - A show(Stage) hívásakor inicializálja a UI-t, majd loadPatients() segítségével betölti és megjeleníti a
 *   betegek adatait a PatientService hívásain keresztül.
 */

public class MainWindow {

    private final AuthService authService;
    private PatientService patientService;

    private final TableView<Patient> patientTable = new TableView<>();
    private final ObservableList<Patient> patientData = FXCollections.observableArrayList();

    public MainWindow(AuthService authService) {
        this.authService = authService;
    }

    public void show(Stage stage) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.start(stage);
            return;
        }

        stage.setTitle("Betegnyilvántartó Rendszer - " + currentUser.getFullName());

        BorderPane borderPane = new BorderPane();

        MenuBar menuBar = createMenuBar(stage);
        borderPane.setTop(menuBar);

        VBox patientListBox = createPatientListView();
        borderPane.setCenter(patientListBox);

        HBox statusBar = createStatusBar(currentUser);
        borderPane.setBottom(statusBar);

        Scene scene = new Scene(borderPane, 800, 600);
        stage.setScene(scene);
        stage.show();

        loadPatients();
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("Fájl");
        MenuItem exitItem = new MenuItem("Kilépés");
        exitItem.setOnAction(e -> stage.close());
        fileMenu.getItems().add(exitItem);

        Menu patientMenu = new Menu("Betegek");
        MenuItem listPatientsItem = new MenuItem("Betegek listázása");
        listPatientsItem.setOnAction(e -> loadPatients());
        patientMenu.getItems().add(listPatientsItem);

        MenuItem viewPatientItem = new MenuItem("Beteg adatainak megtekintése");
        viewPatientItem.setOnAction(e -> {
            Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
            if (selectedPatient != null) {
                viewPatientDetails(selectedPatient);
            } else {
                showAlert("Nincs kiválasztva beteg", "Kérem, válasszon ki egy beteget a listából.");
            }
        });
        patientMenu.getItems().add(viewPatientItem);

        Menu examMenu = new Menu("Vizsgálatok");
        MenuItem viewExamsItem = new MenuItem("Vizsgálatok megtekintése");
        viewExamsItem.setOnAction(e -> {
            Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
            if (selectedPatient != null) {
                try {
                    List<Examination> vizsgalatok = patientService.getExaminationsByPatientId(selectedPatient.getPatientId());
                    ExaminationListWindow examWindow = new ExaminationListWindow(vizsgalatok);
                    examWindow.show();
                } catch (SecurityException ex) {
                    showAlert("Hiba", "Nem sikerült betölteni a vizsgálatokat: " + ex.getMessage());
                }
            } else {
                showAlert("Nincs kiválasztva beteg", "Kérem, válasszon ki egy beteget a listából.");
            }
        });
        examMenu.getItems().add(viewExamsItem);

        Menu ekgMenu = new Menu("EKG");
        MenuItem viewEkgItem = new MenuItem("EKG adatok megtekintése");
        viewEkgItem.setOnAction(e -> {
            Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
            if (selectedPatient != null) {
                try {
                    hu.ujvari.data.EkgData ekgData = patientService.getLatestEkgForPatient(selectedPatient.getPatientId());
                    if (ekgData != null) {
                        EkgViewWindow ekgWindow = new EkgViewWindow(ekgData);
                        ekgWindow.show();
                    } else {
                        showAlert("Nincs EKG adat", "Ehhez a beteghez nem található EKG adat.");
                    }
                } catch (Exception exx) {
                    showAlert("Hiba", "Nem sikerült betölteni az EKG adatokat: " + exx.getMessage());
                }
                
            } else {
                showAlert("Nincs kiválasztva beteg", "Kérem, válasszon ki egy beteget a listából.");
            }
        });
        ekgMenu.getItems().add(viewEkgItem);

        Menu userMenu = new Menu("Felhasználó");
        MenuItem logoutItem = new MenuItem("Kijelentkezés");
        logoutItem.setOnAction(e -> {
            authService.logout();
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.start(stage);
        });
        userMenu.getItems().add(logoutItem);

        if (authService.hasPermission("ADD_EXAMINATION")) {
            MenuItem addExamItem = new MenuItem("Új vizsgálat hozzáadása");
            addExamItem.setOnAction(e -> {
                Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
                if (selectedPatient != null) {
                    ExaminationFormWindow formWindow = new ExaminationFormWindow(selectedPatient, patientService);
                    formWindow.show();
                } else {
                    showAlert("Nincs kiválasztva beteg", "Kérem, válasszon ki egy beteget a listából.");
                }
            });
            examMenu.getItems().add(addExamItem);
        }
        


        menuBar.getMenus().addAll(fileMenu, patientMenu, examMenu, ekgMenu, userMenu);

        return menuBar;
    }

    private VBox createPatientListView() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        Label title = new Label("Betegek listája");

        patientTable.setEditable(false);

        TableColumn<Patient, String> idCol = new TableColumn<>("Azonosító");
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPatientId()));

        TableColumn<Patient, String> nameCol = new TableColumn<>("Név");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        TableColumn<Patient, String> tajCol = new TableColumn<>("TAJ szám");
        tajCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTajSzam()));

        patientTable.getColumns().addAll(Arrays.asList(idCol, nameCol, tajCol));
        patientTable.setItems(patientData);

        Button detailsButton = new Button("Részletek");
        detailsButton.setOnAction(e -> {
            Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
            if (selectedPatient != null) {
                viewPatientDetails(selectedPatient);
            } else {
                showAlert("Nincs kiválasztva beteg", "Kérem, válasszon ki egy beteget a listából.");
            }
        });

        vbox.getChildren().addAll(title, patientTable, detailsButton);

        return vbox;
    }

    private HBox createStatusBar(User currentUser) {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5));

        Label userLabel = new Label("Bejelentkezett felhasználó: " + currentUser.getFullName());
        Label roleLabel = new Label(" | Szerepkör: " + String.join(", ", currentUser.getRoles()));

        statusBar.getChildren().addAll(userLabel, roleLabel);

        return statusBar;
    }

    private void loadPatients() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            patientData.clear();
            patientData.addAll(patients);
        } catch (SecurityException e) {
            showAlert("Hozzáférés megtagadva", e.getMessage());
        } catch (Exception e) {
            showAlert("Hiba", "Nem sikerült betölteni a betegek listáját: " + e.getMessage());
        }
    }

    private void viewPatientDetails(Patient patient) {
        try {
            Patient detailedPatient = patientService.getPatientById(patient.getPatientId());

            if (detailedPatient != null) {
                PatientDetailsWindow detailsWindow = new PatientDetailsWindow(detailedPatient, patientService);
                detailsWindow.show();
            } else {
                showAlert("Hiba", "Nem sikerült betölteni a beteg adatait.");
            }
        } catch (SecurityException e) {
            showAlert("Hozzáférés megtagadva", e.getMessage());
        } catch (Exception e) {
            showAlert("Hiba", "Nem sikerült betölteni a beteg adatait: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }
}