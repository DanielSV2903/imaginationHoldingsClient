package controller;

import com.imaginationHoldings.domain.Guest;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String command = String.format("ADD_GUEST|%s|%s|%s|%d|%s", name, lastName, gender,id,birth);

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