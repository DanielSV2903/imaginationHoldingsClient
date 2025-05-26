package controller;

import com.imaginationHoldings.domain.Guest;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;

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
    private TextField ageTextFIeld;
    @javafx.fxml.FXML
    private ComboBox genderChoiceBox;


    @javafx.fxml.FXML
    public void initialize() {
        genderChoiceBox.getItems().addAll("Male", "Female","Other");
    }

    @javafx.fxml.FXML
    public void confirmButtonOnAction(ActionEvent actionEvent) {
        try {
            int id = Integer.parseInt(iDTextFIeld.getText());
            String name = nameTextField.getText();
            String lastName =lastNameTextField.getText();
            String birth="";

            if (birthDatePicker.getValue() != null)
                birth=birthDatePicker.getValue().toString();
            if (!birth.isEmpty())
                birth=birthDatePicker.getEditor().getText();
            String gender= genderChoiceBox.getSelectionModel().getSelectedItem().toString();

            Socket socket = new Socket("localhost", 5000);
            ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.flush(); // fuerza el encabezado del stream
            ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());
            String command=String.format("ADD_GUEST|%s|%s|%s|%d|%s", name, lastName, gender,id,birth);
            Request request = new Request(Protocol.ADD_GUEST,command);

            objectOutput.writeObject(request);

            Object rawResponse = objectInput.readObject();
            if (rawResponse instanceof Response) {
                Response response = (Response) rawResponse;
                System.out.println("Servidor: " + response.getCommand());
            }

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void cancelOnAction(ActionEvent actionEvent) {
    }
}