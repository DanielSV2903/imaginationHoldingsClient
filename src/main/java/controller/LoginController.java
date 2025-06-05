package controller;

import com.imaginationHoldings.domain.Guest;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

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
        try {
            // Si es un número, intenta interpretarlo como ID de huésped
            int id = Integer.parseInt(username);

            Socket socket = new Socket(MainViewController.SERVER_IP, MainViewController.PORT); // IP y puerto del servidor
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Request request = new Request(Protocol.GET_ALL_GUESTS);
            out.writeObject(request);
            out.flush();

            List<Guest> guests=(List<Guest>)  in.readObject();
            for (Guest guest : guests){
                if (guest.getId() == id) {
                    role = UserRole.CLIENT;
                    userName = String.valueOf(guest.getId()); // guarda el ID como nombre
                }
            }
            loadClientView();

        } catch (NumberFormatException e) {
            // no es un ID, es un username
            switch (username) {
                case "admin" -> role = UserRole.ADMIN;
                case "front desk" -> role = UserRole.FRONTDESK;
                default -> {
                    showError("Usuario inválido.");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // redirigir según rol
        switch (role) {
            case ADMIN -> {} // aún no implementado
            case CLIENT -> loadClientView();
            case FRONTDESK -> loadFrontDeskView();
        }
    }


    public static String getUserName() {
        return userName;
    }

    public static UserRole getRole() {
        return role;
    }
    private void loadClientView() {
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

    private void loadFrontDeskView() {
        try {
            Stage stage = (Stage) bp.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("front-desk.fxml"));
            Scene newScene = new Scene(loader.load());
            stage.setScene(newScene);
            stage.setTitle("Front Desk View");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}