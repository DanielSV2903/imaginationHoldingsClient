package controller;

import com.imaginationholdingsclient.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class FrontDeskController {
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
    }
}
