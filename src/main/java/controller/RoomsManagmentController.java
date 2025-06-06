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
import javafx.scene.layout.GridPane;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomsManagmentController
{
    @FXML
    private ComboBox<Hotel> roomRegistrationHotelComboBox;
    @FXML
    private TextField roomIdTextField;
    @FXML
    private ComboBox<RoomType> roomTypeComboBox;
    @FXML
    private TextField bedAmountTextField;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private TableView<Room> roomsTableVIew;
    @FXML
    private TableColumn<Room, String> roomTypeCol;
    @FXML
    private TableColumn<Room, Integer> idCol;
    @FXML
    private TableColumn<Room, String> hotelCol;
    @FXML
    private TableColumn<Room, String> locationCol;
    @FXML
    private TextField filterHotelRoom;
    @FXML
    private TextField filterHotel;

    private HotelServiceData hotelServiceData;
    private List<Room> rooms;
    private List<Hotel> hotels;
    private Socket socket;
    private ObjectOutputStream objectOutput;
    private ObjectInputStream objectInput;
    private final String SERVER_IP = "10.59.18.238";

    @FXML
    public void initialize() {
        hotels = new ArrayList<>();
        rooms = new ArrayList<>();
        try {
            socket = new Socket(MainViewController.SERVER_IP, MainViewController.PORT);
            objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.flush(); // fuerza el encabezado del stream
            objectInput = new ObjectInputStream(socket.getInputStream());

            // Solicitar hoteles
            Request request = new Request(Protocol.GET_ALL_HOTELS);
            objectOutput.writeObject(request);
            objectOutput.flush();

            hotels = (List<Hotel>) objectInput.readObject();
            System.out.println("Recibido hoteles: " + hotels.size());

            // Solicitar habitaciones
            request = new Request(Protocol.GET_ALL_ROOMS);
            objectOutput.writeObject(request);
            objectOutput.flush();

            rooms = (List<Room>) objectInput.readObject();
            System.out.println("Recibido habitaciones: " + rooms.size());

            // Poblar el combo de hoteles
            for (Hotel h : hotels) {
                //selectHotelComboBox.getItems().add(h);
                roomRegistrationHotelComboBox.getItems().add(h);
            }

            // Poblar el combo de tipos de habitación
            for (RoomType roomType : RoomType.values()) {
                roomTypeComboBox.getItems().add(roomType);
            }

            // Configuración de la tabla de habitaciones
            locationCol.setCellValueFactory(celLData -> new SimpleStringProperty(celLData.getValue().getLocation()));
            hotelCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHotel().getName()));
            roomTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomType().getDescription()));
            idCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
            roomsTableVIew.getItems().addAll(rooms);

            // Filtros de texto
            filterHotel.textProperty().addListener((observable, oldValue, newValue) -> filterHotels(newValue));
            filterHotelRoom.textProperty().addListener((observable, oldValue, newValue) -> filterRoomsByHotel(newValue));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Filtro para el ComboBox de Hoteles
    private void filterHotels(String filterText) {
        //selectHotelComboBox.getItems().clear();
        roomRegistrationHotelComboBox.getItems().clear();

        if (filterText == null || filterText.isEmpty()) {
            // Si el filtro está vacío, mostramos todos los hoteles
          //  selectHotelComboBox.getItems().addAll(hotels);
            roomRegistrationHotelComboBox.getItems().addAll(hotels);
        } else {
            String lowerCaseFilter = filterText.toLowerCase();
            for (Hotel hotel : hotels) {
                if (hotel.getName().toLowerCase().contains(lowerCaseFilter) || hotel.getAddress().toLowerCase().contains(lowerCaseFilter)) {
            //        selectHotelComboBox.getItems().add(hotel);
                    roomRegistrationHotelComboBox.getItems().add(hotel);
                }
            }
        }
    }

    private void filterRoomsByHotel(String filterText) {
        roomsTableVIew.getItems().clear(); // Limpiar la tabla antes de agregar los elementos filtrados

        // Si el filtro está vacío, agregar todas las habitaciones
        if (filterText == null || filterText.isEmpty()) {
            roomsTableVIew.getItems().addAll(rooms);
        } else {
            String lowerCaseFilter = filterText.toLowerCase(); // Convertir el texto del filtro a minúsculas para comparación insensible a mayúsculas

            // Filtrar las habitaciones basadas en el nombre del hotel
            for (Room room : rooms) {
                if (room.getHotel().getName().toLowerCase().contains(lowerCaseFilter)) {
                    roomsTableVIew.getItems().add(room); // Agregar las habitaciones cuyo hotel coincide con el filtro
                }
            }
        }
    }


    // Acción para cancelar
    @FXML
    public void cancelOnAction(ActionEvent actionEvent) {
    }

    // Acción para eliminar habitación
    @FXML
    public void deleteRoomOnAction(ActionEvent actionEvent) {
        Room selectedRoom = roomsTableVIew.getSelectionModel().getSelectedItem();

        if (selectedRoom == null) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("No Selection");
            warning.setHeaderText(null);
            warning.setContentText("Please select a room to delete.");
            warning.showAndWait();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Room");
        confirmation.setHeaderText("Are you sure?");
        confirmation.setContentText("Do you want to delete the following room?\n" + selectedRoom);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Request request = new Request(Protocol.DELETE_ROOM, selectedRoom);
                objectOutput.writeObject(request);
                objectOutput.flush();

                Object rawResponse = objectInput.readObject();
                if (rawResponse instanceof Response response) {
                    if ("ROOM_DELETED".equals(response.getCommand())) {
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Success");
                        success.setHeaderText(null);
                        success.setContentText("Room deleted successfully.");
                        success.showAndWait();
                        updateTview();
                    } else {
                        Alert failure = new Alert(Alert.AlertType.ERROR);
                        failure.setTitle("Error");
                        failure.setHeaderText("Deletion Failed");
                        failure.setContentText("Could not delete the room.");
                        failure.showAndWait();
                    }
                } else {
                    throw new IOException("Unexpected server response.");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Communication Error");
                error.setHeaderText("An error occurred");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        } else {
            roomsTableVIew.getSelectionModel().clearSelection();
        }
    }

    // Actualiza la vista de la tabla de habitaciones
    private void updateTview() throws IOException, ClassNotFoundException {
        this.roomsTableVIew.getItems().clear();
        Request request = new Request(Protocol.GET_ALL_ROOMS);
        objectOutput.writeObject(request);
        objectOutput.flush();
        rooms = (List<Room>) objectInput.readObject();
        this.roomsTableVIew.getItems().addAll(rooms);
    }

    // Acción para registrar habitación
    @FXML
    public void registerRoomOnAction(ActionEvent actionEvent) {
        int id = Integer.parseInt(roomIdTextField.getText());
        RoomType roomType = roomTypeComboBox.getSelectionModel().getSelectedItem();
        descriptionTextField.setText(roomType.getDescription());
        String address = bedAmountTextField.getText();
        Hotel hotel = roomRegistrationHotelComboBox.getSelectionModel().getSelectedItem();
        Room room = new Room(id, roomType, hotel, address);
        try {
            objectOutput.flush(); // fuerza el encabezado del stream

            Request request = new Request(Protocol.REGISTER_ROOM, room);
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

            rawResponse = objectInput.readObject();
            if (rawResponse instanceof Response response) {
                rooms = (List<Room>) response.getData();
            }
            updateTview();
        } catch (Exception e) {
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

    @Deprecated
    public void filterRoomTView(ActionEvent actionEvent) {
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
    public void editRoomOnAction(ActionEvent actionEvent) {
        Room selectedRoom = roomsTableVIew.getSelectionModel().getSelectedItem();

        if (selectedRoom == null) {
            Alert noSelectionAlert = new Alert(Alert.AlertType.WARNING);
            noSelectionAlert.setTitle("No Selection");
            noSelectionAlert.setHeaderText(null);
            noSelectionAlert.setContentText("Please select a room to edit.");
            noSelectionAlert.showAndWait();
            return;
        }

        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle("Edit Room");
        dialog.setHeaderText("Modify room details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Campos editables
        TextField locationField = new TextField(selectedRoom.getLocation());
        ChoiceBox<RoomType> typeChoiceBox = new ChoiceBox<>();
        typeChoiceBox.getItems().addAll(RoomType.values());
        typeChoiceBox.setValue(selectedRoom.getRoomType());

        ChoiceBox<Boolean> availabilityChoiceBox = new ChoiceBox<>();
        availabilityChoiceBox.getItems().addAll(true, false);
        availabilityChoiceBox.setValue(selectedRoom.isAvailable());

        grid.add(new Label("Location:"), 0, 0);
        grid.add(locationField, 1, 0);
        grid.add(new Label("Room Type:"), 0, 1);
        grid.add(typeChoiceBox, 1, 1);
        grid.add(new Label("Available:"), 0, 2);
        grid.add(availabilityChoiceBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selectedRoom.setLocation(locationField.getText());
                selectedRoom.setRoomType(typeChoiceBox.getValue());
                selectedRoom.setAvailability(availabilityChoiceBox.getValue());
                return selectedRoom;
            }
            return null;
        });

        Optional<Room> result = dialog.showAndWait();

        result.ifPresent(editedRoom -> {
            try {
                Request request = new Request(Protocol.EDIT_ROOM, editedRoom);
                objectOutput.writeObject(request);
                objectOutput.flush();

                Object responseObj = objectInput.readObject();
                Response response=(Response) responseObj;
                if (response.getCommand().equals(Response.ROOM_UPDATED)) {
                    Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
                    confirmation.setTitle("Success");
                    confirmation.setHeaderText(null);
                    confirmation.setContentText("Room updated successfully.");
                    confirmation.showAndWait();
                    updateTview();
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Update Failed");
                    error.setHeaderText(null);
                    error.setContentText("Failed to update the room.");
                    error.showAndWait();
                    updateTview();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("An error occurred");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        });
    }
//        Room room=roomsTableVIew.getSelectionModel().getSelectedItem();
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Editar Hotel");
//        GridPane gridPane=new GridPane();
//        gridPane.add(new Label("Type"),0,0);
//        gridPane.add(new Label("Availability"),0,1);
//        gridPane.add(new Label("Location"),0,2);
//        TextField tfLocation=new TextField();
//        TextField tfAddress=new TextField();
//        ChoiceBox<RoomType> typeChoiceBox=new ChoiceBox<>();
//        for (RoomType type : RoomType.values()) {
//            typeChoiceBox.getItems().add(type);
//        }
//        ChoiceBox<Boolean> availabilityChoiceBox=new ChoiceBox();
//        availabilityChoiceBox.getItems().add(true);
//        availabilityChoiceBox.getItems().add(false);
//        gridPane.add(typeChoiceBox,1,0);
//        gridPane.add(availabilityChoiceBox,1,1);
//        gridPane.add(tfLocation,1,2);
//        alert.getDialogPane().setContent(gridPane);
//        alert.showAndWait();
//        RoomType roomType=typeChoiceBox.getSelectionModel().getSelectedItem();
//        boolean availability=availabilityChoiceBox.getSelectionModel().getSelectedItem();
//        String address=tfAddress.getText();
//        room.setAvailability(availability);
//        room.setLocation(address);
//        room.setRoomType(roomType);
//        Request getRoomsRequest = new Request(Protocol.EDIT_ROOM,room);
//        try {
//            objectOutput.writeObject(getRoomsRequest);
//            objectOutput.flush();
//            Response response= (Response) objectInput.readObject();
//            if (response.getCommand().equals("ROOM_UPDATED")){
//                Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
//                alert2.setTitle("Edit Room");
//                alert2.setHeaderText(null);
//                alert2.setContentText("Room updated successfully.");
//                alert2.showAndWait();
//
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
