package controller;

import com.imaginationHoldings.domain.Booking;
import com.imaginationHoldings.domain.Guest;
import com.imaginationHoldings.domain.RoomType;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import javafx.beans.property.SimpleStringProperty;
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

public class GuestManagementController {
    @javafx.fxml.FXML
    private TableColumn<Guest,String> ageCol;
    @javafx.fxml.FXML
    private TableColumn<Guest,String> bDateCol;
    @javafx.fxml.FXML
    private TableColumn<Guest,String> idCol;
    @javafx.fxml.FXML
    private TableColumn<Guest,String> nameCol;
    @javafx.fxml.FXML
    private TableView<Guest> roomsTableVIew;
    @javafx.fxml.FXML
    private TableColumn lastNameCol;
    private final String SERVER_IP="10.59.18.238";
    private Socket socket;
    private ObjectOutputStream objectOutput;
    private ObjectInputStream objectInput;
    private List<Guest> guests;

    @javafx.fxml.FXML
    public void initialize() {
        guests = new ArrayList<>();
        try {
            socket = new Socket(MainViewController.SERVER_IP, MainViewController.PORT);
            objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.flush(); // fuerza el encabezado del stream
            objectInput = new ObjectInputStream(socket.getInputStream());
//            Request request = new Request(Protocol.GET_ALL_GUESTS);
//            objectOutput.writeObject(request);
//            objectOutput.flush();
//            guests= (List<Guest>) objectInput.readObject();
            Guest g1 =new Guest("Daniel","Sanchez","M",119310249,"29/03/2005");
            Guest g2 =new Guest("Lucia","Ramirez","F",119870249,"11/01/2000");
            Guest g3 =new Guest("Juan","Perèz","M",789310249,"30/11/2004");
            guests.add(g1);
            guests.add(g2);
            guests.add(g3);

            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
            bDateCol.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getBirthDate().toString()));
            roomsTableVIew.getItems().addAll(guests);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void updateTview()  {
        this.roomsTableVIew.getItems().clear();
//        Request request=new Request(Protocol.GET_GUESTS);
//        objectOutput.writeObject(request);
//        objectOutput.flush();
//        bookings=(List<Booking>) objectInput.readObject();
        for(Guest guest : guests){
            this.roomsTableVIew.getItems().add(guest);
        }
    }

    @javafx.fxml.FXML
    public void editRoomOnAction(ActionEvent actionEvent) {
        Guest selected=roomsTableVIew.getSelectionModel().getSelectedItem();
        Alert alert =new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Edit Guest");
        GridPane gp=new GridPane();
        DatePicker datePicker=new DatePicker();
        TextField tfName=new TextField();
        tfName.setPromptText("Name");
        TextField tfLname=new TextField();
        tfLname.setPromptText("Last Name");
        ComboBox<String> comboBox=new ComboBox<>();
        comboBox.getItems().add("M");
        comboBox.getItems().add("F");
        gp.add(tfName,1,0);
        gp.add(tfLname,1,2);
        gp.add(comboBox,1,3);
        gp.add(datePicker,1,4);
        guests.remove(selected);
        selected.setFirstName(tfName.getText());
        selected.setLastName(tfLname.getText());
        if (datePicker.getEditor().getText().isEmpty())
            selected.setBirthDate(datePicker.getValue().toString());
        else selected.setBirthDate(datePicker.getEditor().getText());
        if (!comboBox.getSelectionModel().isEmpty())
            selected.setGender(comboBox.getSelectionModel().getSelectedItem());
        guests.add(selected);
        updateTview();
    }

    @javafx.fxml.FXML
    public void deleteRoomOnAction(ActionEvent actionEvent) {
        Guest selected=roomsTableVIew.getSelectionModel().getSelectedItem();
        guests.remove(selected);
        updateTview();
    }
}
