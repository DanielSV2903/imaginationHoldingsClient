package controller;

import com.imaginationHoldings.domain.Hotel;
import com.imaginationHoldings.domain.Room;
import com.imaginationHoldings.domain.RoomType;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeleteRoomController
{
    @javafx.fxml.FXML
    private TableColumn<Room,String> roomTypeCol;
    @javafx.fxml.FXML
    private TableColumn<Room,Integer> idCol;
    @javafx.fxml.FXML
    private ComboBox<Hotel> selectHotelComboBox;
    @javafx.fxml.FXML
    private TableColumn<Room,String> hotelCol;
    @javafx.fxml.FXML
    private TableView<Room> roomsTableVIew;
    private Socket socket;
    private ObjectOutputStream objectOutput;
    private ObjectInputStream objectInput;
    private List<Hotel> hotels;
    private List<Room> rooms;
    @javafx.fxml.FXML
    private BorderPane bp;
    @javafx.fxml.FXML
    private TableColumn<Room,String> locationCol;

    @javafx.fxml.FXML
    public void initialize() {
        hotels = new ArrayList<>();
        rooms = new ArrayList<>();
        try {
            socket = new Socket(MainViewController.SERVER_IP, MainViewController.PORT);
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

            locationCol.setCellValueFactory(celLData->
                    new SimpleStringProperty(celLData.getValue().getLocation()));

            hotelCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getHotel().getName()));

            roomTypeCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getRoomType().getDescription()));

            idCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
            roomsTableVIew.getItems().addAll(rooms);
            for (Hotel hotel : hotels) {
                selectHotelComboBox.getItems().add(hotel);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void filterRoomTView(ActionEvent actionEvent) {
        Hotel hotel= selectHotelComboBox.getSelectionModel().getSelectedItem();
        if (hotel != null) {
            this.roomsTableVIew.getItems().clear();
            for (Room room : rooms) {
                if (room.getHotel().getId()==hotel.getId()) {
                    this.roomsTableVIew.getItems().add(room);
                }
            }
        }
    }

    @javafx.fxml.FXML
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
                if (responseObj instanceof Response response && "ROOM_UPDATED".equals(response.getCommand())) {
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

    @javafx.fxml.FXML
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

    private void updateTview() throws IOException, ClassNotFoundException {
        this.roomsTableVIew.getItems().clear();
        Request request=new Request(Protocol.GET_ALL_ROOMS);
        objectOutput.writeObject(request);
        objectOutput.flush();
        rooms=(List<Room>) objectInput.readObject();
        this.roomsTableVIew.getItems().addAll(rooms);
    }
}