package controller;

import com.imaginationHoldings.domain.Hotel;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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
        try {
        if (this.hotelComboBox.getSelectionModel().getSelectedItem() != null) {
            Hotel hotel = this.hotelComboBox.getSelectionModel().getSelectedItem();
            Request request = new Request(Protocol.DELETE_HOTEL,hotel.getId());
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to delete this hotel?");
                alert.showAndWait();
            objectOutput.writeObject(request);
            Object rawResponse=objectInput.readObject();
            Response response=(Response)rawResponse;
            if (response.getCommand().equals("HOTEL_DELETED")) {
                alert=new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText("Hotel deleted");
                alert.showAndWait();
            }
            System.out.println("Servidor: "+response.getCommand());
        }else if (hotelTableView.getSelectionModel().getSelectedItem() != null&&this.hotelComboBox.getSelectionModel().getSelectedItem()==null) {
            Hotel hotel = this.hotelComboBox.getSelectionModel().getSelectedItem();
            Request request = new Request(Protocol.DELETE_HOTEL,hotel.getId());
            objectOutput.writeObject(request);
            Object rawResponse=objectInput.readObject();
            Response response=(Response)rawResponse;
            System.out.println("Servidor: "+response.getCommand());
        }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}