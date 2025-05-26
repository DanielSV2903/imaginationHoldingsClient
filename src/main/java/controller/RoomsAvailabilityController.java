package controller;

import com.imaginationHoldings.domain.Hotel;
import com.imaginationHoldings.domain.Room;
import com.imaginationHoldings.domain.RoomType;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

    @javafx.fxml.FXML
    public void initialize() {
        try {
            socket = new Socket("localhost", 5000);
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
    }

    @javafx.fxml.FXML
    public void checkAvailability(ActionEvent actionEvent) {

    }
}