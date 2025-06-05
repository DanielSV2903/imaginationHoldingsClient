package controller;
import com.imaginationHoldings.domain.Booking;
import com.imaginationHoldings.domain.Guest;
import com.imaginationHoldings.domain.Room;
import com.imaginationHoldings.domain.StayPeriod;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;

import com.imaginationHoldings.protocol.Response;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
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
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private List<Booking> userBookings;

    public void setGuest(Guest guest) {
        this.guest = guest;
        loadBookingsForGuest();
    }

    private void loadBookingsForGuest() {
        try {
            socket = new Socket(MainViewController.SERVER_IP, MainViewController.PORT); // o tu IP y puerto
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            Request request = new Request(Protocol.GET_BOOKINGS);
            out.writeObject(request);
            out.flush();

            List<Booking> allBookings = (List<Booking>) in.readObject();
            userBookings = allBookings.stream()
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
        Booking selected=this.bookingsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Seleccione una reservacion primero.");
            alert.showAndWait();
            return;
        }
        GridPane gp = new GridPane();
        TextField tfGuestAmount = new TextField(String.valueOf(selected.getGuestAmount()));
        TextField tfID = new TextField(String.valueOf(selected.getGuest().getId()));
        tfID.setEditable(false);
        TextField tfRoom = new TextField(String.valueOf(selected.getRoom().getRoomNumber()));
        DatePicker checkInDP = new DatePicker();
        DatePicker checkOutDP = new DatePicker();
        if (selected.getStayPeriod() != null){
            checkInDP.setValue(selected.getStayPeriod().getCheckInDate());
            checkOutDP.setValue(selected.getStayPeriod().getCheckOutDate());
        }

        gp.setHgap(10);
        gp.setVgap(10);
        gp.add(new Label("Nombre:"), 0, 0); gp.add(tfID, 1, 0);
        gp.add(new Label("cantdiad de huespedes:"), 0, 1); gp.add(tfGuestAmount, 1, 1);
        gp.add(new Label("Cuarto:"), 0, 2); gp.add(tfRoom, 1, 2);
        gp.add(new Label("Check in:"), 0, 3); gp.add(checkInDP, 1, 3);
        gp.add(new Label("Check in:"), 0, 3); gp.add(checkOutDP, 1, 4);


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Editar reservacion");
        alert.setHeaderText("¿Desea guardar los cambios?");
        alert.getDialogPane().setContent(gp);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (tfID.getText().isEmpty() || tfGuestAmount.getText().isEmpty() || checkInDP.getValue() == null
                        ||checkInDP.getValue() == null) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Todos los campos son obligatorios.");
                    errorAlert.showAndWait();
                    return;
                }
                LocalDate checkIn = checkInDP.getValue();
                LocalDate checkOut = checkOutDP.getValue();
                selected.setGuestAmount(Integer.parseInt(tfGuestAmount.getText()));
                selected.setRoom(new Room(Integer.parseInt(tfRoom.getText())));
                selected.setStayPeriod(new StayPeriod(checkIn, checkOut));
            }
        });
        Request request=new Request(Protocol.EDIT_RESERVATION,selected);
        try {
            out.writeObject(request);
            out.flush();
            Object rawResponse=in.readObject();
            Response response=(Response) rawResponse;
            if (response.getCommand().equals(Response.BOOKING_DONE)){
                alert=new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Booking Status");
                alert.setContentText("Booking updated successfully.");
                alert.showAndWait();
            }else {
                alert=new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Guest Status");
                alert.setContentText("Booking was not updated");
                alert.showAndWait();
            }
            updateTview();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void deleteReservationOnAction(ActionEvent actionEvent) {
        Booking booking = bookingsTable.getSelectionModel().getSelectedItem();
        Request request=new Request(Protocol.CANCEL_RESERVATION,booking);
        try {
            out.writeObject(request);
            out.flush();
            Object raw=in.readObject();
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
    private void updateTview() throws IOException, ClassNotFoundException {
        this.bookingsTable.getItems().clear();
        Request request=new Request(Protocol.GET_BOOKINGS);
        out.writeObject(request);
        out.flush();
        List<Booking> allBookings = (List<Booking>) in.readObject();
        userBookings = allBookings.stream()
                .filter(b -> b.getGuest().getId() == guest.getId())
                .collect(Collectors.toList());
        for (Booking booking:userBookings)
            bookingsTable.getItems().add(booking);
    }
}