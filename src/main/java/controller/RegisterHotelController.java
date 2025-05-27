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
        if (!validarCampos())
            return;


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

                cleanTextFields();
            } else
                mostrarAlerta("Error al registrar hotel"+ response.getCommand());


            System.out.println("Servidor: " + response.getCommand());
            socket.close();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error: El ID del hotel debe ser un número válido.");
        } catch (IOException e) {
            mostrarAlerta("Error de conexión: No se pudo conectar al servidor. Verifique que el servidor esté funcionando.");
        } catch (ClassNotFoundException e) {
            mostrarAlerta("Error de comunicación: Respuesta del servidor no válida.");
        } catch (Exception e) {
            mostrarAlerta("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cleanTextFields() {
        hotelNameTextField.clear();
        addressTextField.clear();
        hotelIdTextField.clear();
    }

    @javafx.fxml.FXML
    public void cancelOnAction(ActionEvent actionEvent) {
        cleanTextFields();

    }

    private static void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Error de validación");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private boolean validarCampos() {
        if (hotelIdTextField.getText().trim().isEmpty()) {
            mostrarAlerta("El ID del hotel es obligatorio.");
            hotelIdTextField.requestFocus();
            return false;
        }

        try {
            int hotelId = Integer.parseInt(hotelIdTextField.getText().trim());
            if (hotelId <= 0) {
                mostrarAlerta("El ID del hotel debe ser un número positivo.");
                hotelIdTextField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("El ID del hotel debe ser un número válido.");
            hotelIdTextField.requestFocus();
            return false;
        }

        if (hotelNameTextField.getText().trim().isEmpty()) {
            mostrarAlerta("El nombre del hotel es obligatorio.");
            hotelNameTextField.requestFocus();
            return false;
        }

        if (addressTextField.getText().trim().isEmpty()) {
            mostrarAlerta("La dirección del hotel es obligatoria.");
            addressTextField.requestFocus();
            return false;
        }

        // Validar que no contengan solo espacios
        if (hotelNameTextField.getText().trim().replaceAll("\\s+", "").isEmpty()) {
            mostrarAlerta("El nombre del hotel no puede contener solo espacios.");
            hotelNameTextField.requestFocus();
            return false;
        }

        if (addressTextField.getText().trim().replaceAll("\\s+", "").isEmpty()) {
            mostrarAlerta("La dirección no puede contener solo espacios.");
            addressTextField.requestFocus();
            return false;
        }

        return true;
    }
}