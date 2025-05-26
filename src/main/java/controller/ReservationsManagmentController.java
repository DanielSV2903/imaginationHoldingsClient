package controller;

import com.imaginationHoldings.domain.RoomType;
import com.imaginationHoldings.domain.StayPeriod;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.*;
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
    private ComboBox<RoomType> roomTypeComboBox;
    @javafx.fxml.FXML
    private TextField guestAmountTextField;
    @javafx.fxml.FXML
    private TableView reservationsTableView;
    @javafx.fxml.FXML
    private DatePicker exitDatePicker;

    @javafx.fxml.FXML
    public void initialize() {
        for (RoomType roomType : RoomType.values()) {
            roomTypeComboBox.getItems().add(roomType);
        }
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
        String roomType = roomTypeComboBox.getValue().getDescription();
        int guestsAmount = Integer.parseInt(guestAmountTextField.getText());

        Socket socket = new Socket("localhost", 5000);
            ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.flush(); // fuerza el encabezado del stream
            ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());

        String command = String.format("RESERVE_ROOM|%s|%s|%s|%s|%d|%s|%d", guestName[0],guestName[1], entryDate, checkOutDate,id,roomType,guestsAmount);
            Request request = new Request(Protocol.RESERVE_ROOM,command);
        objectOutput.writeObject(request);

        Object rawresponse = objectInput.readObject();
        Response response = (Response) rawresponse;
            System.out.println("Servidor: " + response.getCommand());
        socket.close();
    }catch (Exception e){
            e.printStackTrace();
        }
    }
}