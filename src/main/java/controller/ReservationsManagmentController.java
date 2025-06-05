package controller;

import com.imaginationHoldings.domain.*;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

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
    private List<Room> rooms;
    private Socket socket;
    private ObjectOutputStream objectOutput;
    private ObjectInputStream objectInput;
    private Hotel hotel;
    private List<Hotel> hotels;
    private final String SERVER_IP="10.59.18.238";
    @javafx.fxml.FXML
    private ComboBox<Hotel> cBoxHotel;
    @javafx.fxml.FXML
    private TableColumn<Booking,String> hotelCol;
    @javafx.fxml.FXML
    private TextField filterText;
    private FilteredList<Booking> filteredBookings;

    @javafx.fxml.FXML
    public void initialize() {
        bookings = new ArrayList<>();
        rooms = new ArrayList<>();
        hotels = new ArrayList<>();
        try {
             socket = new Socket(MainViewController.SERVER_IP, MainViewController.PORT);
             objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.flush(); // fuerza el encabezado del stream
             objectInput = new ObjectInputStream(socket.getInputStream());

            Request request = new Request(Protocol.GET_BOOKINGS);
            objectOutput.writeObject(request);
            objectOutput.flush();

            bookings= (List<Booking>) objectInput.readObject();

            request=new Request(Protocol.GET_ALL_HOTELS);
            objectOutput.writeObject(request);
            objectOutput.flush();
            hotels = (List<Hotel>) objectInput.readObject();
            request=new Request(Protocol.GET_ALL_ROOMS);
            objectOutput.writeObject(request);
            objectOutput.flush();
            rooms=(List<Room>) objectInput.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (RoomType roomType : RoomType.values()) {
            roomTypeComboBox.getItems().add(roomType);
        }
        for (Hotel hotel : hotels) {
            cBoxHotel.getItems().add(hotel);
        }
        guestCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGuest().getId()+""));
        roomCol.setCellValueFactory(cellData ->new SimpleStringProperty(cellData.getValue().getRoom().getRoomNumber()+""));
        checkIn.setCellValueFactory(cellData ->new SimpleStringProperty(cellData.getValue().getStayPeriod().getCheckInDate().toString()));
        checkOut.setCellValueFactory(cellData ->new SimpleStringProperty(cellData.getValue().getStayPeriod().getCheckOutDate().toString()));
        hotelCol.setCellValueFactory(cellData->new SimpleStringProperty(String.valueOf(cellData.getValue().getRoom().getHotel().getId())));

        filteredBookings = new FilteredList<>(FXCollections.observableArrayList(bookings), b -> true);

        filterText.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredBookings.setPredicate(booking -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return String.valueOf(booking.getGuest().getId()).contains(lowerCaseFilter)
                        || String.valueOf(booking.getRoom().getRoomNumber()).toLowerCase().contains(lowerCaseFilter)
                        || booking.getStayPeriod().getCheckInDate().toString().contains(lowerCaseFilter)
                        || booking.getStayPeriod().getCheckOutDate().toString().contains(lowerCaseFilter)
                        || String.valueOf(booking.getRoom().getHotel().getId()).contains(lowerCaseFilter);
            });
        });

        SortedList<Booking> sortedBookings = new SortedList<>(filteredBookings);
        sortedBookings.comparatorProperty().bind(reservationsTableView.comparatorProperty());

        reservationsTableView.setItems(sortedBookings);
    }
    private void updateTview() throws IOException, ClassNotFoundException {
        this.reservationsTableView.getItems().clear();
        Request request=new Request(Protocol.GET_BOOKINGS);
        objectOutput.writeObject(request);
        objectOutput.flush();
        bookings=(List<Booking>) objectInput.readObject();
//        bookings = (List<Booking>) objectInput.readObject();
        filteredBookings.setAll(bookings);

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
        cleanFields();
    }

    @javafx.fxml.FXML
    public void createReservationsOnAction(ActionEvent actionEvent) {
        if (!validarEntradas())
            return;

        try {
        String[] guestName = guestNameTextField.getText().split(" ");
        String entryDate =entryDatePicker.getValue().toString();
        int id = Integer.parseInt(guestMailTextField.getText());
        String checkOutDate = exitDatePicker.getValue().toString();
        String roomType = roomTypeComboBox.getValue().getDescription();
        int guestsAmount = Integer.parseInt(guestAmountTextField.getText());
        int hotelID=cBoxHotel.getSelectionModel().getSelectedItem().getId();
//
//         socket = new Socket("localhost", 5000);
//             objectOutput = new ObjectOutputStream(socket.getOutputStream());
//            objectOutput.flush(); // fuerza el encabezado del stream
//              objectInput = new ObjectInputStream(socket.getInputStream());

        String command = String.format("1|%s|%s|%s|%s|%d|%s|%d|%d", guestName[0],guestName[1], entryDate, checkOutDate,id,roomType,guestsAmount,hotelID);
            Request request = new Request(Protocol.RESERVE_ROOM,command);
        objectOutput.writeObject(request);
        objectOutput.flush();

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

    private void cleanFields() {
        guestNameTextField.clear();
        guestMailTextField.clear();
        guestAmountTextField.clear();
        entryDatePicker.setValue(null);
        exitDatePicker.setValue(null);
        roomTypeComboBox.getSelectionModel().clearSelection();
    }

    private static void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error de Validación");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private boolean validarEntradas() {
        if (guestNameTextField.getText().trim().isEmpty()) {
            mostrarAlerta("El nombre del huésped es obligatorio");
            guestNameTextField.requestFocus();
            return false;
        }

        if (guestAmountTextField.getText().trim().isEmpty()) {
            mostrarAlerta("La cantidad de huéspedes es obligatoria");
            guestAmountTextField.requestFocus();
            return false;
        }

        int guestAmount;
        try {
            guestAmount = Integer.parseInt(guestAmountTextField.getText().trim());
            if (guestAmount <= 0) {
                mostrarAlerta("La cantidad de huéspedes debe ser mayor que 0");
                guestAmountTextField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Ingrese un número válido para la cantidad de huéspedes");
            guestAmountTextField.requestFocus();
            return false;
        }

        if (entryDatePicker.getValue() == null) {
            mostrarAlerta("Seleccione la fecha de entrada.");
            entryDatePicker.requestFocus();
            return false;
        }

        if (exitDatePicker.getValue() == null) {
            mostrarAlerta("Seleccione la fecha de salida.");
            exitDatePicker.requestFocus();
            return false;
        }

        LocalDate entryDate = entryDatePicker.getValue();
        LocalDate exitDate = exitDatePicker.getValue();
        LocalDate today = LocalDate.now();

        if (entryDate.isBefore(today)) {
            mostrarAlerta("La fecha de entrada no puede ser anterior a hoy");
            entryDatePicker.requestFocus();
            return false;
        }

        if (!exitDate.isAfter(entryDate)) {
            mostrarAlerta("La fecha de salida debe ser posterior a la de entrada");
            exitDatePicker.requestFocus();
            return false;
        }

        RoomType roomType = roomTypeComboBox.getValue();
        if (roomType == null) {
            mostrarAlerta("Debe seleccionar un tipo de habitación");
            roomTypeComboBox.requestFocus();
            return false;
        }

        if (guestAmount > roomType.getCapacity()) {
            mostrarAlerta(String.format(
                    "La cantidad de huéspedes (%d) excede la capacidad del tipo de habitación (%d)",
                    guestAmount, roomType.getCapacity()));
            guestAmountTextField.requestFocus();
            return false;
        }

        return true;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    @javafx.fxml.FXML
    public void editOnAction(ActionEvent actionEvent) {
        Booking selected=this.reservationsTableView.getSelectionModel().getSelectedItem();

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
            objectOutput.writeObject(request);
            objectOutput.flush();
            Object rawResponse=objectInput.readObject();
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
}