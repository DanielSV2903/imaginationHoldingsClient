package controller;

import com.imaginationHoldings.domain.Hotel;
import com.imaginationHoldings.domain.Room;
import com.imaginationHoldings.domain.RoomType;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

public class RoomsAvailabilityController
{
    @javafx.fxml.FXML
    private ComboBox<Hotel> hotelComboBox;
    @javafx.fxml.FXML
    private DatePicker entryDatePicker;
    @javafx.fxml.FXML
    private TableColumn<Room,String> colType;
    @javafx.fxml.FXML
    private ComboBox<RoomType> roomTypeComboBox;
    @javafx.fxml.FXML
    private TableColumn<Room,String> colPrice;
    @javafx.fxml.FXML
    private TableColumn<Room,String> colHotel;
    @javafx.fxml.FXML
    private DatePicker exitDatePicker;
    @javafx.fxml.FXML
    private TableColumn<Room,Integer> colRoomId;
    @javafx.fxml.FXML
    private TableColumn<Room,String> colStatus;
    private List<Room> rooms;
    private List<Hotel> hotels;
    private Socket socket;
    private ObjectOutputStream objectOutput;
    private ObjectInputStream objectInput;
    @javafx.fxml.FXML
    private TableView<Room> tView;
    private final String SERVER_IP="10.59.18.238";

    @javafx.fxml.FXML
    public void initialize() {
        try {
            socket = new Socket(MainViewController.SERVER_IP, MainViewController.PORT);
            objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.flush(); // fuerza el encabezado del stream
            objectInput = new ObjectInputStream(socket.getInputStream());

            // Solicitar hoteles
            String command=String.format("GET_ALL_HOTELS|");
            Request request = new Request(Protocol.GET_ALL_HOTELS,command);
            objectOutput.writeObject(request);
            objectOutput.flush();

            hotels = (List<Hotel>) objectInput.readObject();
            System.out.println("Recibido hoteles: " + hotels.size());

            // Solicitar habitaciones
            request = new Request(Protocol.GET_ALL_ROOMS,"GET_ALL_ROOMS|");
            objectOutput.writeObject(request);
            objectOutput.flush();

            rooms = (List<Room>) objectInput.readObject();
            System.out.println("Recibido habitaciones: " + rooms.size());
            this.hotelComboBox.getItems().addAll(hotels);
            for (RoomType roomType: RoomType.values()) {
                this.roomTypeComboBox.getItems().add(roomType);
            }
            this.colHotel.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getHotel().getName()));
            this.colRoomId.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
            this.colStatus.setCellValueFactory(cellData->
                    new SimpleStringProperty(cellData.getValue().isAvailable()?"Available":"Ocuppied"));
            this.colType.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getRoomType().getDescription()));
            this.tView.getItems().addAll(rooms);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void cancelOnAction(ActionEvent actionEvent) {
        cleanFields();
    }

    @javafx.fxml.FXML
    public void checkAvailability(ActionEvent actionEvent) {
        if (!validarEntradas())
            return;

        try {

        } catch (Exception e){
            mostrarAlerta("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }


    }

    private void cleanFields(){
        hotelComboBox.getSelectionModel().clearSelection();
        roomTypeComboBox.getSelectionModel().clearSelection();
        entryDatePicker.setValue(null);
        exitDatePicker.setValue(null);
    }

    private static void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error de Validación");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private boolean validarEntradas() {
        if (hotelComboBox.getSelectionModel().getSelectedItem() == null) {
            mostrarAlerta("Debe seleccionar un hotel para consultar disponibilidad");
            hotelComboBox.requestFocus();
            return false;
        }

        if (entryDatePicker.getValue() == null) {
            mostrarAlerta("Debe seleccionar una fecha de entrada");
            entryDatePicker.requestFocus();
            return false;
        }

        if (exitDatePicker.getValue() == null) {
            mostrarAlerta("Debe seleccionar una fecha de salida");
            exitDatePicker.requestFocus();
            return false;
        }

        LocalDate entryDate = entryDatePicker.getValue();
        LocalDate exitDate = exitDatePicker.getValue();
        LocalDate today = LocalDate.now();

        if (entryDate.isBefore(today)) {
            mostrarAlerta("La fecha de entrada no puede ser antes que la fecha actual");
            entryDatePicker.requestFocus();
            return false;
        }

        if (exitDate.isBefore(entryDate) || exitDate.isEqual(entryDate)) {
            mostrarAlerta("La fecha de salida debe ser después a la fecha de entrada");
            exitDatePicker.requestFocus();
            return false;
        }

        return true;
    }
}