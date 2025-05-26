package controller;

import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;

public class RegisterHotelController
{
    @javafx.fxml.FXML
    private TextField hotelNameTextField;
    @javafx.fxml.FXML
    private TextField addressTextField;
    @javafx.fxml.FXML
    private TextField hotelIdTextField;
    @javafx.fxml.FXML
    private TextField roomsNumberOnAction;

    @javafx.fxml.FXML
    public void initialize() {
    }

    @javafx.fxml.FXML
    public void createHotelOnAction(ActionEvent actionEvent) {
        try {
            int hotelId = Integer.parseInt(hotelIdTextField.getText());
            String hotelName = hotelNameTextField.getText();
            String address = addressTextField.getText();

            Socket socket = new Socket("localhost", 5000);
            ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.flush(); // fuerza el encabezado del stream
            ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());

            String command = String.format("REGISTER_HOTEL|%d|%s|%s", hotelId, hotelName, address);
            Request request = new Request(Protocol.REGISTER_HOTEL,command);
            objectOutput.writeObject(request);
            Object rawresponse = objectInput.readObject();

            Response response = (Response) rawresponse;
            if (response.getCommand().equals("HOTEL REGISTERED")){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Hotel Registered");
                alert.setHeaderText("Hotel Registered");
                alert.setContentText("Hotel Registered Successfully");
                alert.showAndWait();
            }
            System.out.println("Servidor: " + response.getCommand());


            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void cancelOnAction(ActionEvent actionEvent) {

    }
}