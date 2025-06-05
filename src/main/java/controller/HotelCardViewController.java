package controller;

import com.imaginationHoldings.domain.Hotel;
import com.imaginationHoldings.domain.Room;
import com.imaginationHoldings.domain.StayPeriod;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import com.imaginationholdingsclient.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

public class HotelCardViewController
{
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private List<Room> rooms;
    @javafx.fxml.FXML
    private TextField guestNameTextField;
    @javafx.fxml.FXML
    private FlowPane flowPane;
    @javafx.fxml.FXML
    private DatePicker entryDatePicker;
    @javafx.fxml.FXML
    private Text hotelLabel;
    @javafx.fxml.FXML
    private TextField guestMailTextField;
    @javafx.fxml.FXML
    private ComboBox<Room> roomTypeComboBox;
    @javafx.fxml.FXML
    private TextField guestAmountTextField;
    @javafx.fxml.FXML
    private DatePicker exitDatePicker;
    @javafx.fxml.FXML
    private ComboBox<Hotel> hotelCBox;
    private Hotel hotel;

    //TODO
    @javafx.fxml.FXML
    public void initialize() {

    }
    public void initData() {
        try {
            socket = new Socket(MainViewController.SERVER_IP, MainViewController.PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            out.flush();

            Request request = new Request(Protocol.GET_ALL_ROOMS);
            out.writeObject(request);
            out.flush();
            rooms = (List<Room>) in.readObject();

            roomTypeComboBox.getItems().clear();
            for (Room room : rooms) {
                if (room.getHotel().getId() == hotel.getId()) {
                    roomTypeComboBox.getItems().add(room);
                }
            }

            hotelCBox.getItems().clear();
            hotelCBox.getItems().add(hotel);

            loadRooms(rooms.stream()
                    .filter(r -> r.getHotel().getId() == hotel.getId())
                    .toList());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



    public void loadRooms(List<Room> rooms) {
        flowPane.getChildren().clear();
        for (Room room : rooms) {
            try {
                FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("roomCard.fxml"));
                VBox card = loader.load();
                RoomCardController controller = loader.getController();
                controller.setHotel(room, () -> selectRoom(room));
                flowPane.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void selectRoom(Room room) {
        this.roomTypeComboBox.getSelectionModel().select(room);
    }

    @javafx.fxml.FXML
    public void checkAvailabilityOnAction(ActionEvent actionEvent) {
        boolean availability=false;
        LocalDate checkIn= entryDatePicker.getValue();
        LocalDate checkOut= exitDatePicker.getValue();
        StayPeriod stayPeriod=new StayPeriod(checkIn,checkOut);
        Object[] data=new Object[3];
        data[0]=stayPeriod;
        data[1]=roomTypeComboBox.getSelectionModel().getSelectedItem().getRoomNumber();
        data[2]=hotel.getId();
        Request request=new Request(Protocol.CHECK_AVAILABILITY_BY_STAY_PERIOD,data);
        try {
            out.writeObject(request);
            out.flush();
            availability=in.readBoolean();
        }catch (IOException e) {}
        Alert alert=new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Availability");
        alert.setHeaderText(null);
        if (availability) {
            alert.setContentText("Room Available for "+guestAmountTextField.getText()+" from "+checkIn+" to "+checkOut);
            alert.showAndWait();
        }else {
            alert.setContentText("Room Not Available for "+guestAmountTextField.getText()+" from "+checkIn+" to "+checkOut);
            alert.showAndWait();
        }
    }

    @javafx.fxml.FXML
    public void cancelOnAction(ActionEvent actionEvent) {
        guestNameTextField.clear();
        guestMailTextField.clear();
        guestAmountTextField.clear();
        entryDatePicker.setValue(null);
        exitDatePicker.setValue(null);
        roomTypeComboBox.getSelectionModel().clearSelection();
    }

    @javafx.fxml.FXML
    public void createReservationsOnAction(ActionEvent actionEvent) {
        try {
            String[] guestName = guestNameTextField.getText().split(" ");
            String entryDate =entryDatePicker.getValue().toString();
            int id = Integer.parseInt(guestMailTextField.getText());
            String checkOutDate = exitDatePicker.getValue().toString();
            int roomNumber = roomTypeComboBox.getValue().getRoomNumber();
            int guestsAmount = Integer.parseInt(guestAmountTextField.getText());
            int hotelID=hotel.getId();
            String command = String.format("2|%s|%s|%s|%s|%d|%d|%d|%d", guestName[0],guestName[1], entryDate, checkOutDate,id,roomNumber,guestsAmount,hotelID);
            Request request = new Request(Protocol.RESERVE_ROOM,command);
            out.writeObject(request);
            out.flush();
            Object rawresponse = in.readObject();
            Response response = (Response) rawresponse;
            if (response.getCommand().equals(Response.BOOKING_DONE)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText(null);
                alert.setContentText("La reservacion fue realizada exitosamente");
                alert.showAndWait();
            }else System.out.println("Servidor: "+response.getCommand());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }
}