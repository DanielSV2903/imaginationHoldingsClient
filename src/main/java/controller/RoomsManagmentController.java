package controller;

import com.imaginationHoldings.data.HotelServiceData;
import com.imaginationHoldings.domain.Hotel;
import com.imaginationHoldings.domain.Room;
import com.imaginationHoldings.domain.RoomType;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class RoomsManagmentController
{
    @javafx.fxml.FXML
    private TextField pricePerNightTextField;
    @javafx.fxml.FXML
    private TextField roomCapacityTextField;
    @javafx.fxml.FXML
    private ComboBox<Hotel> roomRegistrationHotelComboBox;
    @javafx.fxml.FXML
    private TextField roomIdTextField;
    @javafx.fxml.FXML
    private ComboBox<RoomType> roomTypeComboBox;
    @javafx.fxml.FXML
    private TextField bedAmountTextField;
    @javafx.fxml.FXML
    private ComboBox<Hotel> selectHotelComboBox;
    @javafx.fxml.FXML
    private TextField descriptionTextField;
    @javafx.fxml.FXML
    private TableView<Room> roomsTableVIew;
    private HotelServiceData hotelServiceData;
    private  List<Room> rooms;
    private List<Hotel> hotels;
    private Socket socket;
    private  ObjectOutputStream objectOutput;
    private ObjectInputStream objectInput;
    @javafx.fxml.FXML
    private TableColumn<Room,String> roomTypeCol;
    @javafx.fxml.FXML
    private TableColumn<Room,Integer> idCol;
    @javafx.fxml.FXML
    private TableColumn<Room,String> hotelCol;

    @javafx.fxml.FXML
    public void initialize() {
        try {
            socket = new Socket("localhost", 5000);
            objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.flush(); // fuerza el encabezado del stream
            objectInput = new ObjectInputStream(socket.getInputStream());
            // Solicitar hoteles
            Request request=new Request(Protocol.GET_ALL_HOTELS);
            objectOutput.writeObject(request);
            objectOutput.flush();

            hotels = (List<Hotel>) objectInput.readObject();
            System.out.println("Recibido hoteles: " + hotels.size());

            // Solicitar habitaciones
            request=new Request(Protocol.GET_ALL_ROOMS);
            objectOutput.writeObject(request);
            objectOutput.flush();

            rooms = (List<Room>) objectInput.readObject();
            System.out.println("Recibido habitaciones: " + rooms.size());


            // Aquí puedes poblar el combo y la tabla si quieres:
            selectHotelComboBox.getItems().addAll(hotels);
            roomRegistrationHotelComboBox.getItems().addAll(hotels);

            for (RoomType roomType : RoomType.values()) {
                roomTypeComboBox.getItems().add(roomType);
            }
            hotelCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getHotel().getName()));

            roomTypeCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getRoomType().getDescription()));

            idCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
            roomsTableVIew.getItems().addAll(rooms);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void cancelOnAction(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void deleteRoomOnAction(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void registerRoomOnAction(ActionEvent actionEvent) {
        int id = Integer.parseInt(roomIdTextField.getText());
        RoomType roomType = roomTypeComboBox.getSelectionModel().getSelectedItem();
        descriptionTextField.setText(roomType.getDescription());
        roomCapacityTextField.setText(String.valueOf(roomType.getCapacity()));
        String address=bedAmountTextField.getText();
        Hotel hotel = roomRegistrationHotelComboBox.getSelectionModel().getSelectedItem();
        Room room=new Room(id,roomType,hotel,address);
        try {
            objectOutput.flush(); // fuerza el encabezado del stream

            Request request=new Request(Protocol.REGISTER_ROOM,room);

            objectOutput.writeObject(request);
            objectOutput.flush();

            Object rawResponse = objectInput.readObject();
            if (rawResponse instanceof Response response) {
                if ("HOTEL_NOT_FOUND".equals(response.getCommand())) {
                    System.out.println("El hotel no fue encontrado.");
                } else {
                    System.out.println("Habitación registrada con éxito.");
                }
            } else {
                System.out.println("Respuesta inesperada del servidor.");

            }
            Request getRoomsRequest = new Request(Protocol.GET_ALL_ROOMS);
            objectOutput.writeObject(getRoomsRequest);
            objectOutput.flush();

            rawResponse=objectInput.readObject();
            if (rawResponse instanceof Response response) {
                Response response1 = (Response) rawResponse;
                rooms=(List<Room>) response1.getData();
            }
            roomsTableVIew.getItems().setAll(rooms);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void closeConnection() {
        try {
            if (objectOutput != null) objectOutput.close();
            if (objectInput != null) objectInput.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
