package hu.ujvari.ui;

import hu.ujvari.auth.LdapConnector;
import hu.ujvari.service.AuthService;
import hu.ujvari.service.PatientService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class LoginWindow extends Application {

    private static AuthService staticAuthService;
    private static PatientService staticPatientService;
    private static LdapConnector staticLdapConnector;

    private AuthService authService;
    private PatientService patientService;
    private LdapConnector ldapConnector;

    public static void setStaticAuthService(AuthService authService) {
        staticAuthService = authService;
    }

    public static void setStaticPatientService(PatientService patientService) {
        staticPatientService = patientService;
    }

    public static void setStaticLdapConnector(LdapConnector ldapConnector) {
        staticLdapConnector = ldapConnector;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void start(Stage primaryStage) {
        this.authService = staticAuthService;
        this.patientService = staticPatientService;
        this.ldapConnector = staticLdapConnector;

   

    
   
        

        // Grid layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        // Cím
        Text sceneTitle = new Text("Kérem, jelentkezzen be");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);
        
        // Felhasználónév mező
        Label userName = new Label("Felhasználónév:");
        grid.add(userName, 0, 1);
        
        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);
        
        // Jelszó mező
        Label pw = new Label("Jelszó:");
        grid.add(pw, 0, 2);
        
        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);
        
        // Bejelentkezés gomb
        Button btn = new Button("Bejelentkezés");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);

        Button changePasswordButton = new Button("Jelszó módosítása");
        changePasswordButton.setOnAction(e -> {
            String username = userTextField.getText();
            if (username == null || username.isEmpty()) {
                showAlert("Hiányzó adat", "Először adja meg a felhasználónevét!");
                return;
            }
        
            PasswordChangeWindow pwWindow = new PasswordChangeWindow(username, ldapConnector);
            pwWindow.show();
        });

        
        
        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(btn, changePasswordButton);
        grid.add(buttons, 1, 3);


        
        // Hibaüzenet helye
        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);
        
        // Gomb eseménykezelő
        btn.setOnAction(e -> {
            String username = userTextField.getText();
            String password = pwBox.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                actiontarget.setText("Kérem, adja meg a felhasználónevet és jelszót!");
                return;
            }
            
            try {
                // Itt használjuk az AuthService-t a bejelentkezéshez
                boolean success = authService.login(username, password);
                
                if (success) {
                    // Sikeres bejelentkezés esetén átirányítás a főablakra
                    MainWindow mainWindow = new MainWindow(authService);
                    mainWindow.setPatientService(patientService);
                    mainWindow.show(primaryStage);
                } else {
                    actiontarget.setText("Helytelen felhasználónév vagy jelszó!");
                }
            } catch (Exception ex) {
                actiontarget.setText("Hiba történt: " + ex.getMessage());
            }
        });

        
        
        Scene scene = new Scene(grid, 400, 275);
        primaryStage.setScene(scene);
        
        primaryStage.show();
    }
   
    
}