package hu.ujvari.ui;

import hu.ujvari.auth.LdapConnector;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PasswordChangeWindow {

    private final String username;
    private final LdapConnector ldapConnector;

    public PasswordChangeWindow(String username, LdapConnector ldapConnector) {
        this.username = username;
        this.ldapConnector = ldapConnector;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Jelszó módosítása");
        stage.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        PasswordField oldPasswordField = new PasswordField();
        PasswordField newPasswordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();

        grid.add(new Label("Jelenlegi jelszó:"), 0, 0);
        grid.add(oldPasswordField, 1, 0);
        grid.add(new Label("Új jelszó:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Új jelszó mégegyszer:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        Button changeButton = new Button("Jelszó módosítása");
        changeButton.setOnAction(e -> {
            String oldPass = oldPasswordField.getText();
            String newPass = newPasswordField.getText();
            String confirmPass = confirmPasswordField.getText();

            if (!newPass.equals(confirmPass)) {
                showAlert("Hiba", "Az új jelszavak nem egyeznek.");
                return;
            }

            boolean success = ldapConnector.changePassword(username, oldPass, newPass);
            if (success) {
                showAlert("Siker", "A jelszó sikeresen módosítva.");
                stage.close();
            } else {
                showAlert("Hiba", "Nem sikerült a jelszó módosítása. Ellenőrizze az adatokat.");
            }
        });

        grid.add(changeButton, 1, 3);

        Scene scene = new Scene(grid, 400, 250);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
