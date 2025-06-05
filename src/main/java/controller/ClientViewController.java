package controller;

import com.imaginationHoldings.domain.Guest;
import com.imaginationHoldings.domain.Hotel;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationholdingsclient.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;


public class ClientViewController
{
    @javafx.fxml.FXML
    private Label welcomeLabel;
    @javafx.fxml.FXML
    private MenuItem myBookings;
    @javafx.fxml.FXML
    private MenuItem searchHotel;
    @javafx.fxml.FXML
    private BorderPane bp;
    @javafx.fxml.FXML
    private MenuItem bookRoom;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private List< Hotel > hotels;
    @javafx.fxml.FXML
    private FlowPane flowPane;

    @javafx.fxml.FXML
    public void initialize() {
        welcomeLabel.setText("Welcome : "+LoginController.getUserName());
        try {
            socket=new Socket(MainViewController.SERVER_IP,MainViewController.PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            out.flush();
            Request request=new Request(Protocol.GET_ALL_HOTELS);
            out.writeObject(request);
            out.flush();
            hotels=(List< Hotel >) in.readObject();
            loadHotels(hotels);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadHotels(List<Hotel> hotels) {
        flowPane.getChildren().clear();
        for (Hotel hotel : hotels) {
            try {
                FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("hotelCard.fxml"));
                VBox card = loader.load();
                HotelCardController controller = loader.getController();
                controller.setHotel(hotel, () -> openReservationView(hotel));
                flowPane.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
        private void openReservationView(Hotel hotel) {
            try {
                FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("hotelCardView.fxml"));
                Parent root = loader.load();
                HotelCardViewController controller = loader.getController();
                controller.setHotel(hotel);
                controller.initData();
                Stage stage = new Stage();
                stage.setTitle("Reservar en " + hotel.getName());
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    @javafx.fxml.FXML
    public void myBookingsOnAction(ActionEvent actionEvent) {
        Guest user=new Guest(Integer.parseInt(LoginController.getUserName()));
        openMyBookings(user);
    }
    private void openMyBookings(Guest guest){
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("myBookings.fxml"));
            Parent root = loader.load();
            MyBookingsController controller = loader.getController();
            controller.setGuest(guest);
            Stage stage = new Stage();
            stage.setTitle("Mis reservaciones");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void bookRoomOnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("clientBooking.fxml"));
            Parent root = loader.load();
            ClientBookingController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Reservar habitacion");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}