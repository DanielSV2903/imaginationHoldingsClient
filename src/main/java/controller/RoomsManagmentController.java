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
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

public class RoomsManagmentController
{
    @FXML
    private TextField pricePerNightTextField;
    @FXML
    private TextField roomCapacityTextField;
    @FXML
    private ComboBox<Hotel> roomRegistrationHotelComboBox;
    @FXML
    private TextField roomIdTextField;
    @FXML
    private ComboBox<RoomType> roomTypeComboBox;
    @FXML
    private TextField bedAmountTextField;
    @FXML
    private ComboBox<Hotel> selectHotelComboBox;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private TableView<Room> roomsTableVIew;
    private HotelServiceData hotelServiceData;
    private  List<Room> rooms;
    private List<Hotel> hotels;
    private Socket socket;
    private  ObjectOutputStream objectOutput;
    private ObjectInputStream objectInput;
    @FXML
    private TableColumn<Room,String> roomTypeCol;
    @FXML
    private TableColumn<Room,Integer> idCol;
    @FXML
    private TableColumn<Room,String> hotelCol;

    @FXML
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
            for (Hotel h:hotels) {
                selectHotelComboBox.getItems().add(h);
                roomRegistrationHotelComboBox.getItems().add(h);
            }

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

    @FXML
    public void cancelOnAction(ActionEvent actionEvent) {
        cleanFields();
    }

    @FXML
    public void deleteRoomOnAction(ActionEvent actionEvent) {
        try {
        Room toDelete =roomsTableVIew.getSelectionModel().getSelectedItem();
        Request request=new Request(Protocol.DELETE_ROOM,toDelete);
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Room");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to delete this room?\n"+toDelete.toString());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get()==ButtonType.OK) {
            objectOutput.writeObject(request);
            objectOutput.flush();

            Object rawResponse=objectInput.readObject();
            Response response=(Response) rawResponse;
            System.out.println("Servidor: "+response.getCommand());
            if (response.getCommand().equals("ROOM_DELETED")) {
                alert=new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Delete Room");
                alert.setHeaderText(null);
                alert.setContentText("Room Deleted!");
                alert.showAndWait();
                updateTview();
            }
        }else this.roomsTableVIew.getSelectionModel().clearSelection();
    } catch (IOException | ClassNotFoundException e) {
    throw new RuntimeException(e);
}
    }

    private void updateTview() throws IOException, ClassNotFoundException {
        this.roomsTableVIew.getItems().clear();
        Request request=new Request(Protocol.GET_ALL_ROOMS);
        objectOutput.writeObject(request);
        objectOutput.flush();
        rooms=(List<Room>) objectInput.readObject();
        this.roomsTableVIew.getItems().addAll(rooms);
    }
    private void filterTView(){
        Hotel hotel= roomRegistrationHotelComboBox.getSelectionModel().getSelectedItem();
        if (hotel != null) {
            this.roomsTableVIew.getItems().clear();
        for (Room room : rooms) {
            if (room.getHotel().getId()==hotel.getId()) {
                this.roomsTableVIew.getItems().add(room);
            }
        }
        }
    }

    @FXML
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
            updateTview();
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

    @FXML
    public void filterRoomTView(ActionEvent actionEvent) {
        filterTView();
    }

    private void cleanFields() {
        roomIdTextField.clear();
        roomTypeComboBox.getSelectionModel().clearSelection();
        roomRegistrationHotelComboBox.getSelectionModel().clearSelection();
        bedAmountTextField.clear();
        pricePerNightTextField.clear();
        descriptionTextField.clear();
        roomCapacityTextField.clear();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private boolean validarEntradas() {
        if (roomIdTextField.getText().trim().isEmpty()) {
            mostrarAlerta("El ID de la habitación es obligatorio");
            roomIdTextField.requestFocus();
            return false;
        }

        try {
            int roomId = Integer.parseInt(roomIdTextField.getText().trim());
            if (roomId <= 0) {
                mostrarAlerta("El ID de la habitación debe ser un número positivo");
                roomIdTextField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("El ID de la habitación debe ser un número válido");
            roomIdTextField.requestFocus();
            return false;
        }

        if (roomTypeComboBox.getSelectionModel().getSelectedItem() == null) {
            mostrarAlerta("Debe seleccionar un tipo de habitación");
            roomTypeComboBox.requestFocus();
            return false;
        }

        try {
            int bedAmount = Integer.parseInt(bedAmountTextField.getText().trim());
            if (bedAmount <= 0) {
                mostrarAlerta("La cantidad de camas debe ser un número positivo");
                bedAmountTextField.requestFocus();
                return false;
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("La cantidad de camas debe ser un número válido");
            bedAmountTextField.requestFocus();
            return false;
        }

        if (roomRegistrationHotelComboBox.getSelectionModel().getSelectedItem() == null) {
            mostrarAlerta("Debe seleccionar un hotel para la habitación");
            roomRegistrationHotelComboBox.requestFocus();
            return false;
        }

        if (!pricePerNightTextField.getText().trim().isEmpty()) {
            try {
                double price = Double.parseDouble(pricePerNightTextField.getText().trim());
                if (price < 0) {
                    mostrarAlerta("El precio por noche no puede ser negativo");
                    pricePerNightTextField.requestFocus();
                    return false;
                }

            } catch (NumberFormatException e) {
                mostrarAlerta("El precio por noche debe ser un número válido");
                pricePerNightTextField.requestFocus();
                return false;
            }
        }

        if (rooms != null) {
            Hotel selectedHotel = roomRegistrationHotelComboBox.getSelectionModel().getSelectedItem();
            int roomId = Integer.parseInt(roomIdTextField.getText().trim());

            for (Room existingRoom : rooms) {
                if (existingRoom.getHotel().equals(selectedHotel) && existingRoom.getRoomNumber() == roomId) {
                    mostrarAlerta("Ya existe una habitación con este ID en el hotel seleccionado");
                    roomIdTextField.requestFocus();
                    return false;
                }
            }
        }

        return true;
    }

}
