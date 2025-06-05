package controller;
import com.imaginationHoldings.domain.Booking;
import com.imaginationHoldings.domain.Guest;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class MyBookingsController {

    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> bookingIdCol;
    @FXML private TableColumn<Booking, String> roomCol;
    @FXML private TableColumn<Booking, String> checkInCol;
    @FXML private TableColumn<Booking, String> checkOutCol;

    private Guest guest;
    @FXML
    private TableColumn<Booking,String> guestCol;
    @FXML
    private TableColumn<Booking,String> hotelCol;

    public void setGuest(Guest guest) {
        this.guest = guest;
        loadBookingsForGuest();
    }

    private void loadBookingsForGuest() {
        try {
            Socket socket = new Socket("localhost", 5000); // o tu IP y puerto
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Request request = new Request(Protocol.GET_BOOKINGS);
            out.writeObject(request);
            out.flush();

            List<Booking> allBookings = (List<Booking>) in.readObject();
            List<Booking> userBookings = allBookings.stream()
                    .filter(b -> b.getGuest().getId() == guest.getId())
                    .collect(Collectors.toList());

            // Configura columnas si no lo has hecho en FXML
            bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            roomCol.setCellValueFactory(data -> data.getValue().getRoom() != null ?
                    new javafx.beans.property.SimpleStringProperty(data.getValue().getRoom().getRoomNumber() + "") :
                    new javafx.beans.property.SimpleStringProperty("N/A"));
            checkInCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStayPeriod().getCheckInDate().toString()));
            checkOutCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStayPeriod().getCheckOutDate().toString()));
            guestCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGuest().getId() + ""));
            hotelCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoom().getHotel().getId()+""));

            bookingsTable.getItems().addAll(userBookings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void editOnAction(ActionEvent actionEvent) {
    }

    @FXML
    public void deleteReservationOnAction(ActionEvent actionEvent) {
    }
}