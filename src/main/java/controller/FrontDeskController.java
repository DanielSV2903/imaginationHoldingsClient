package controller;

import com.imaginationholdingsclient.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class FrontDeskController {
    @javafx.fxml.FXML
    private BorderPane bp;


    @javafx.fxml.FXML
    public void addGuestOnAction(ActionEvent actionEvent) {
        try {
            // Carga el archivo FXML
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("userInfo.fxml"));
            Parent root = fxmlLoader.load();

            // Crea un nuevo stage (ventana)
            Stage popupStage = new Stage();

            // Opcional: configura el tipo de ventana (por ejemplo modal)
            popupStage.initModality(Modality.APPLICATION_MODAL); // bloquea la ventana padre hasta cerrar esta

            // Opcional: ponle título
            popupStage.setTitle("Mi ventana popup");

            // Asocia la escena con el stage
            popupStage.setScene(new Scene(root));

            // Muestra la ventana (showAndWait es modal)
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void addRoomOnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("roomsManagment.fxml"));
            Parent view = fxmlLoader.load();
            bp.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void reservationManagerOnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("reservationsManagment.fxml"));
            Parent view = fxmlLoader.load();
            bp.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void addHotelOnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("register.fxml"));
            Parent view = fxmlLoader.load();
            bp.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void checkAvailabilityOnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("availability.fxml"));
            Parent view = fxmlLoader.load();
            bp.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void removeHotelOnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("hotelManagment.fxml"));
            Parent view = fxmlLoader.load();
            bp.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void deleteRooms(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void editHotelOnAction(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void removeGuestOnAction(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void editGuestOnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("manageGuest.fxml"));
            Parent view = fxmlLoader.load();
            bp.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
