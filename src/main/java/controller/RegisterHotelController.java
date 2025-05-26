package controller;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String command = String.format("REGISTER_HOTEL|%d|%s|%s", hotelId, hotelName, address);
            writer.println(command);

            String response = reader.readLine();
            System.out.println("Servidor: " + response);

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void cancelOnAction(ActionEvent actionEvent) {

    }
}