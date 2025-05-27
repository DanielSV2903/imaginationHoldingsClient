package controller;

import com.imaginationHoldings.domain.Booking;
import com.imaginationHoldings.domain.Room;
import com.imaginationHoldings.domain.RoomType;
import com.imaginationHoldings.domain.StayPeriod;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private TableView<Booking> reservationsTableView;
    @javafx.fxml.FXML
    private DatePicker exitDatePicker;
    @javafx.fxml.FXML
    private TableColumn<Booking,String> checkIn;
    @javafx.fxml.FXML
    private TableColumn<Booking,String> roomCol;
    @javafx.fxml.FXML
    private TableColumn<Booking,String> checkOut;
    @javafx.fxml.FXML
    private TableColumn<Booking,String> guestCol;
    private List<Booking> bookings;
    private Socket socket;
    private ObjectOutputStream objectOutput;
    private ObjectInputStream objectInput;

    @javafx.fxml.FXML
    public void initialize() {
        bookings = new ArrayList<>();
        try {
             socket = new Socket("localhost", 5000);
             objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.flush(); // fuerza el encabezado del stream
             objectInput = new ObjectInputStream(socket.getInputStream());
            Request request = new Request(Protocol.GET_BOOKINGS);
            objectOutput.writeObject(request);
            objectOutput.flush();
            bookings= (List<Booking>) objectInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (RoomType roomType : RoomType.values()) {
            roomTypeComboBox.getItems().add(roomType);
        }
        guestCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGuest().getId()+""));
        roomCol.setCellValueFactory(cellData ->new SimpleStringProperty(cellData.getValue().getRoom().getRoomNumber()+""));
        checkIn.setCellValueFactory(cellData ->new SimpleStringProperty(cellData.getValue().getStayPeriod().getCheckInDate().toString()));
        checkOut.setCellValueFactory(cellData ->new SimpleStringProperty(cellData.getValue().getStayPeriod().getCheckOutDate().toString()));

        reservationsTableView.getItems().addAll(bookings);
    }
    private void updateTview() throws IOException, ClassNotFoundException {
        this.reservationsTableView.getItems().clear();
        Request request=new Request(Protocol.GET_BOOKINGS);
        objectOutput.writeObject(request);
        objectOutput.flush();
        bookings=(List<Booking>) objectInput.readObject();
        for(Booking booking : bookings){
            this.reservationsTableView.getItems().add(booking);
        }
    }

    @javafx.fxml.FXML
    public void deleteReservationOnAction(ActionEvent actionEvent) {
        Booking booking = reservationsTableView.getSelectionModel().getSelectedItem();
        Request request=new Request(Protocol.CANCEL_RESERVATION,booking);
        try {
            objectOutput.writeObject(request);
            objectOutput.flush();
            Object raw=objectInput.readObject();
            Response response = (Response) raw;
            if (response.getCommand().equals("BOOKING_DELETED")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText(null);
                alert.setContentText("The Booking has been cancelled");
                alert.showAndWait();
            }else if (response.getCommand().equals("BOOKING_NOT_FOUND")) {
                System.out.println("Booking not found");
            }
            updateTview();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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
//
//         socket = new Socket("localhost", 5000);
//             objectOutput = new ObjectOutputStream(socket.getOutputStream());
//            objectOutput.flush(); // fuerza el encabezado del stream
//              objectInput = new ObjectInputStream(socket.getInputStream());

        String command = String.format("RESERVE_ROOM|%s|%s|%s|%s|%d|%s|%d", guestName[0],guestName[1], entryDate, checkOutDate,id,roomType,guestsAmount);
            Request request = new Request(Protocol.RESERVE_ROOM,command);
        objectOutput.writeObject(request);

        Object rawresponse = objectInput.readObject();
        Response response = (Response) rawresponse;
        if (response.getCommand().equals("BOOKING_DONE")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("La reservacion fue realizada exitosamente");
            alert.showAndWait();
        }
            updateTview();
    }catch (Exception e){
            e.printStackTrace();
        }
    }
}