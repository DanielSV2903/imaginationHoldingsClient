package controller;

import com.imaginationHoldings.domain.StayPeriod;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;

public class ReservationsManagmentController
{
    @javafx.fxml.FXML
    private TextField guestNameTextField;
    @javafx.fxml.FXML
    private DatePicker entryDatePicker;
    @javafx.fxml.FXML
    private TextField guestMailTextField;
    @javafx.fxml.FXML
    private ComboBox roomTypeComboBox;
    @javafx.fxml.FXML
    private TextField guestAmountTextField;
    @javafx.fxml.FXML
    private TableView reservationsTableView;
    @javafx.fxml.FXML
    private DatePicker exitDatePicker;

    @javafx.fxml.FXML
    public void initialize() {
    }

    @javafx.fxml.FXML
    public void deleteReservationOnAction(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void cancelOnAction(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void createReservationsOnAction(ActionEvent actionEvent) {
        try {
        String[] guestName = guestNameTextField.getText().split(" ");
        String entryDate =entryDatePicker.getValue().toString();
        int id = Integer.parseInt(guestMailTextField.getText());
        String checkOutDate = exitDatePicker.getValue().toString();
        String roomType = roomTypeComboBox.getValue().toString();

        Socket socket = new Socket("localhost", 5000);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String command = String.format("ADD_GUEST|%s|%s|%s|%s|%d|%s", guestName[0],guestName[1], entryDate, checkOutDate,id,roomType);

        writer.println(command);

        String response = reader.readLine();
        System.out.println("Servidor: " + response);

        socket.close();
    }catch (Exception e){
            e.printStackTrace();
        }
    }
}