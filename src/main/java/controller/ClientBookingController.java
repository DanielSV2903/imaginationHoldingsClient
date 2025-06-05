package controller;


import com.imaginationHoldings.domain.*;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClientBookingController {
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
    private DatePicker exitDatePicker;
    private List<Booking> bookings;
    private List<Room> rooms;
    private Socket socket;
    private ObjectOutputStream objectOutput;
    private ObjectInputStream objectInput;
    private Hotel hotel;
    private List<Hotel> hotels;

    @javafx.fxml.FXML
    private ComboBox<Hotel> cBoxHotel;
    @javafx.fxml.FXML
    private BorderPane bp;

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

}

