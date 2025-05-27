package controller;

import com.imaginationHoldings.domain.Hotel;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HotelManagmentController
{
    @javafx.fxml.FXML
    private TableView<Hotel> hotelTableView;
    @javafx.fxml.FXML
    private ComboBox<Hotel> hotelComboBox;
    private List<Hotel> hotels;
    @javafx.fxml.FXML
    private TableColumn<Hotel,Integer> idCol;
    @javafx.fxml.FXML
    private TableColumn<Hotel,String> nameCol;
    @javafx.fxml.FXML
    private TableColumn<Hotel,String> directionCol;
    private Socket socket;
    private  ObjectOutputStream objectOutput;
    private ObjectInputStream objectInput;

    @javafx.fxml.FXML
    public void initialize() {
        hotels=new ArrayList<>();
        try {
             socket = new Socket("localhost", 5000);
             objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.flush(); // fuerza el encabezado del stream
             objectInput = new ObjectInputStream(socket.getInputStream());
            // Enviar comando al servidor
            String command = String.format("ADD_GUEST|");
            Request request = new Request(Protocol.GET_ALL_HOTELS,command);
            objectOutput.writeObject(request);
            objectOutput.flush();
            hotels= (List<Hotel>) objectInput.readObject();

            for (Hotel hotel : hotels) {
                this.hotelComboBox.getItems().add(hotel);
            }
            this.idCol.setCellValueFactory(new PropertyValueFactory<Hotel,Integer>("id"));
            this.nameCol.setCellValueFactory(new PropertyValueFactory<Hotel,String>("name"));
            this.directionCol.setCellValueFactory(new PropertyValueFactory<Hotel,String>("address"));

            this.hotelTableView.getItems().addAll(hotels);



        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }

    @javafx.fxml.FXML
    public void cancelOnAction(ActionEvent actionEvent) {
        this.hotelComboBox.getSelectionModel().clearSelection();
        this.hotelTableView.getSelectionModel().clearSelection();
    }

    @javafx.fxml.FXML
    public void deleteHotelOnAction(ActionEvent actionEvent) {
            Hotel selectedHotel = hotelComboBox.getSelectionModel().getSelectedItem();

            if (selectedHotel == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Selection");
                alert.setHeaderText(null);
                alert.setContentText("Please select a hotel to delete.");
                alert.showAndWait();
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Hotel");
            confirmation.setHeaderText("Are you sure?");
            confirmation.setContentText("Do you want to delete this hotel?\n" + selectedHotel);

            if (confirmation.showAndWait().filter(response -> response == ButtonType.OK).isPresent()) {
                try {
                    Request request = new Request(Protocol.DELETE_HOTEL, selectedHotel.getId());
                    objectOutput.writeObject(request);
                    objectOutput.flush();

                    Object rawResponse = objectInput.readObject();
                    if (rawResponse instanceof Response response) {
                        if ("HOTEL_DELETED".equals(response.getCommand())) {
                            Alert info = new Alert(Alert.AlertType.INFORMATION);
                            info.setTitle("Deleted");
                            info.setHeaderText(null);
                            info.setContentText("Hotel deleted successfully.");
                            info.showAndWait();
                        } else {
                            Alert error = new Alert(Alert.AlertType.ERROR);
                            error.setTitle("Error");
                            error.setHeaderText("Deletion failed");
                            error.setContentText("Server response: " + response.getCommand());
                            error.showAndWait();
                        }
                    }
                    updateTV();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("An error occurred");
                    error.setContentText(e.getMessage());
                    error.showAndWait();
                }
            }
        }
    private void updateTV() throws IOException, ClassNotFoundException {
        this.hotelTableView.getItems().clear();
        this.hotelComboBox.getItems().clear();
        String command = String.format("ADD_GUEST|");
        Request request = new Request(Protocol.GET_ALL_HOTELS,command);
        objectOutput.writeObject(request);
        hotels= (List<Hotel>) objectInput.readObject();
        for (Hotel hotel : hotels) {
            this.hotelComboBox.getItems().add(hotel);
           this.hotelTableView.getItems().add(hotel);
        }
    }

    @javafx.fxml.FXML
    public void editOnAction(ActionEvent actionEvent) {
        Hotel selectedHotel = hotelComboBox.getSelectionModel().getSelectedItem();

        if (selectedHotel == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a hotel to edit.");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Hotel");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TextField tfName = new TextField(selectedHotel.getName());
        TextField tfAddress = new TextField(selectedHotel.getAddress());

        gridPane.add(new Label("Name:"), 0, 0);
        gridPane.add(tfName, 1, 0);
        gridPane.add(new Label("Address:"), 0, 1);
        gridPane.add(tfAddress, 1, 1);

        dialog.getDialogPane().setContent(gridPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String name = tfName.getText().trim();
                String address = tfAddress.getText().trim();

                if (name.isEmpty() || address.isEmpty()) {
                    Alert warning = new Alert(Alert.AlertType.WARNING);
                    warning.setTitle("Invalid Input");
                    warning.setHeaderText(null);
                    warning.setContentText("Name and address cannot be empty.");
                    warning.showAndWait();
                    return;
                }

                selectedHotel.setName(name);
                selectedHotel.setAddress(address);

                try {
                    Request request = new Request(Protocol.EDIT_HOTELS, selectedHotel);
                    objectOutput.writeObject(request);
                    objectOutput.flush();

                    Object rawResponse = objectInput.readObject();
                    if (rawResponse instanceof Response res && "HOTEL_UPDATED".equals(res.getCommand())) {
                        Alert info = new Alert(Alert.AlertType.INFORMATION);
                        info.setTitle("Success");
                        info.setHeaderText(null);
                        info.setContentText("Hotel updated successfully.");
                        info.showAndWait();
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Update Failed");
                        error.setHeaderText(null);
                        error.setContentText("Could not update hotel.");
                        error.showAndWait();
                    }

                    updateTV();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Communication Error");
                    error.setHeaderText("An error occurred");
                    error.setContentText(e.getMessage());
                    error.showAndWait();
                }
            }
        });
//        try {
//            if (this.hotelComboBox.getSelectionModel().getSelectedItem() != null) {
//                Hotel hotel = this.hotelComboBox.getSelectionModel().getSelectedItem();
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Editar Hotel");
//                GridPane gridPane=new GridPane();
//                gridPane.add(new Label("Name"),0,0);
//                gridPane.add(new Label("Address"),0,1);
//                TextField tfName=new TextField();
//                TextField tfAddress=new TextField();
//                gridPane.add(tfName,1,0);
//                gridPane.add(tfAddress,1,1);
//                alert.getDialogPane().setContent(gridPane);
//                alert.showAndWait();
//                String name=tfName.getText();
//                String address=tfAddress.getText();
//                if (!name.isEmpty() && !address.isEmpty()) {
//                    hotel.setName(name);
//                    hotel.setAddress(address);
//                }else if (!name.isEmpty() && address.isEmpty()) {
//                    hotel.setName(name);
//                }else if (!address.isEmpty()&&name.isEmpty()) {
//                    hotel.setAddress(address);
//                }
//
//                Request request = new Request(Protocol.EDIT_HOTELS,hotel);
//                objectOutput.writeObject(request);
//
//                Object rawResponse=objectInput.readObject();
//                Response response=(Response)rawResponse;
//                if (response.getCommand().equals("HOTEL_UPDATED")) {
//                    alert=new Alert(Alert.AlertType.INFORMATION);
//                    alert.setTitle("Information");
//                    alert.setHeaderText(null);
//                    alert.setContentText("Hotel updated successfully");
//                    alert.showAndWait();
//                }
//                System.out.println("Servidor: "+response.getCommand());
//                updateTV();
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
    }
}