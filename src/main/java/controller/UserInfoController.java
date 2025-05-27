package controller;

import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDate;

public class UserInfoController
{
    @javafx.fxml.FXML
    private TextField lastNameTextField;
    @javafx.fxml.FXML
    private DatePicker birthDatePicker;
    @javafx.fxml.FXML
    private TextField iDTextFIeld;
    @javafx.fxml.FXML
    private TextField nameTextField;
    @javafx.fxml.FXML
    private ComboBox genderChoiceBox;

    @javafx.fxml.FXML
    public void initialize() {
        genderChoiceBox.getItems().addAll("Male", "Female","Other");
    }

    @javafx.fxml.FXML
    public void confirmButtonOnAction(ActionEvent actionEvent) {
        if (!validarEntradas())
            return;


        try {
            int id = Integer.parseInt(iDTextFIeld.getText());
            String name = nameTextField.getText();
            String lastName = lastNameTextField.getText();
            String birth = "";

            if (birthDatePicker.getValue() != null)
                birth = birthDatePicker.getValue().toString();
            if (!birth.isEmpty())
                birth = birthDatePicker.getEditor().getText();
            String gender = genderChoiceBox.getSelectionModel().getSelectedItem().toString();

            Socket socket = new Socket("localhost", 5000);

            // Establecer timeout para evitar bloqueos indefinidos
            socket.setSoTimeout(5000); // 5 segundos de timeout

            try {
                ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
                objectOutput.flush(); // fuerza el encabezado del stream
                ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());

                // Enviar comando al servidor
                String command = String.format("ADD_GUEST|%s|%s|%s|%d|%s", name, lastName, gender, id, birth);
                Request request = new Request(Protocol.ADD_GUEST, command);

                objectOutput.writeObject(request);
                objectOutput.flush();

                // Leer respuesta del servidor
                Object rawResponse = objectInput.readObject();
                Response response = (Response) rawResponse;
                    System.out.println("Servidor: " + response.getCommand());

                if (response.getCommand().equals("GUEST_REGISTERED")) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Huésped Registrado");
                    alert.setHeaderText("Huésped Registrado Exitosamente");
                    alert.setContentText("El huésped " + name + " " + lastName + " ha sido registrado correctamente.");
                    alert.showAndWait();

                    cleanTextFields();
                } else
                    mostrarAlerta("Error al registrar el huesped"+ response.getCommand());

                socket.close();
            } catch (java.net.SocketTimeoutException e) {
                mostrarAlerta("Error de conexión: El servidor no respondió a tiempo. Intente nuevamente.");
                System.out.println("El servidor no respondió a tiempo");
                e.printStackTrace();

            } catch (ClassNotFoundException e) {
                mostrarAlerta("Error de comunicación: Respuesta del servidor no válida.");
                e.printStackTrace();
            } catch (Exception e) {
                mostrarAlerta("Error inesperado: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error: El ID debe ser un número válido.");
        } catch (UnknownHostException e) {
            mostrarAlerta("Error de conexión: No se pudo encontrar el servidor.");
            e.printStackTrace();
        } catch (SocketException e) {
            mostrarAlerta("Error de conexión: Problema de red.");
            e.printStackTrace();
        } catch (IOException e) {
            mostrarAlerta("Error de conexión: No se pudo conectar al servidor.");
            e.printStackTrace();
        } catch (Exception e) {
            mostrarAlerta("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void cancelOnAction(ActionEvent actionEvent) {
        cleanTextFields();
    }

    public void cleanTextFields() {
        lastNameTextField.clear();
        birthDatePicker.setValue(null);
        birthDatePicker.getEditor().clear();
        genderChoiceBox.getSelectionModel().clearSelection();
        nameTextField.clear();
        iDTextFIeld.clear();
    }

    private static void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Error de validación");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private boolean validarEntradas() {
        if (iDTextFIeld.getText().trim().isEmpty()) {
            mostrarAlerta("La identificación del huésped es obligatoria");
            iDTextFIeld.requestFocus();
            return false;
        }

        try {
            int id = Integer.parseInt(iDTextFIeld.getText().trim());
            if (iDTextFIeld.getText().trim().length() < 9) {
                mostrarAlerta("La identificación del huésped debe contener al menos 9 caracteres");
                iDTextFIeld.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("El ID del huésped debe ser un número válido");
            iDTextFIeld.requestFocus();
            return false;
        }

        if (nameTextField.getText().trim().isEmpty()) {
            mostrarAlerta("El nombre es obligatorio");
            nameTextField.requestFocus();
            return false;
        }

        if (nameTextField.getText().trim().replaceAll("\\s+", "").isEmpty()) {
            mostrarAlerta("El nombre no puede contener solo espacios");
            nameTextField.requestFocus();
            return false;
        }

        if (lastNameTextField.getText().trim().isEmpty()) {
            mostrarAlerta("El apellido es obligatorio");
            lastNameTextField.requestFocus();
            return false;
        }

        if (lastNameTextField.getText().trim().replaceAll("\\s+", "").isEmpty()) {
            mostrarAlerta("El apellido no puede contener solo espacios.");
            lastNameTextField.requestFocus();
            return false;
        }

        if (genderChoiceBox.getSelectionModel().getSelectedItem() == null) {
            mostrarAlerta("Debe seleccionar un género");
            genderChoiceBox.requestFocus();
            return false;
        }

        if (birthDatePicker.getValue() == null) {
            mostrarAlerta("La selección de la fecha de nacimiento es obligatoria");
            birthDatePicker.requestFocus();
            return false;
        }

        if (birthDatePicker.getValue().isAfter(LocalDate.now())) {
            mostrarAlerta("La fecha de nacimiento no puede ser posterior a la fecha actual");
            birthDatePicker.requestFocus();
            return false;
        }

        LocalDate fechaNacimiento = birthDatePicker.getValue();
        LocalDate fechaMinima = LocalDate.now().minusYears(18);
        if (fechaNacimiento.isAfter(fechaMinima)) {
            mostrarAlerta("El huésped debe ser mayor de 18 años");
            birthDatePicker.requestFocus();
            return false;
        }

        LocalDate fechaMaxima = LocalDate.now().minusYears(120);
        if (fechaNacimiento.isBefore(fechaMaxima)) {
            mostrarAlerta("Por favor, verifique la fecha de nacimiento. La edad no puede exceder los 120 años");
            birthDatePicker.requestFocus();
            return false;
        }

        return true;
    }
}