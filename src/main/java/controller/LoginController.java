package controller;

import com.imaginationHoldings.protocol.UserRole;
import com.imaginationholdingsclient.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController
{
    @javafx.fxml.FXML
    private TextField usernameField;
    @javafx.fxml.FXML
    private PasswordField passwordField;
    @javafx.fxml.FXML
    private BorderPane bp;
    private static String userName;
    private static UserRole role;

    @javafx.fxml.FXML
    public void initialize() {
    }

    @javafx.fxml.FXML
    public void loginOnAction(ActionEvent actionEvent) {
        String username = usernameField.getText();
        userName = username;
        String password = passwordField.getText();

        switch (username) {
            case "admin" -> role = UserRole.ADMIN;
            case "user" -> role = UserRole.CLIENT;
            case "front desk" -> role = UserRole.FRONTDESK;
            default -> role = null;
        }

        if (role != null && role.getPriority() == 1) {
            try {
                Stage stage = (Stage) bp.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("clientView.fxml"));
                Scene newScene = new Scene(loader.load());
                stage.setScene(newScene);
                stage.setTitle("Client View");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static String getUserName() {
        return userName;
    }

    public static UserRole getRole() {
        return role;
    }
}