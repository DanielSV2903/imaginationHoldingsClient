package controller;
import com.imaginationHoldings.domain.Hotel;
import com.imaginationholdingsclient.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class HotelCardController {

    @FXML private ImageView hotelImage;
    @FXML private Label hotelName;
    @FXML private Label hotelLocation;
    @FXML private VBox cardRoot;

    private Hotel hotel;
    private Runnable onClickHandler;

    public void setHotel(Hotel hotel, Runnable onClickHandler) {
        this.hotel = hotel;
        this.onClickHandler = onClickHandler;
        hotelName.setText(hotel.getName());
        hotelLocation.setText(hotel.getAddress());
        hotelImage.setImage(new Image(MainApp.class.getResourceAsStream("/images/1.png")));

        cardRoot.setOnMouseClicked(e -> {
            if (this.onClickHandler != null) this.onClickHandler.run();
        });
    }

//    public void setHotel(Hotel hotel, Runnable onClick) {
//        this.hotel = hotel;
//        hotelName.setText(hotel.getName());
//        hotelLocation.setText(hotel.getAddress());
//        hotelImage.setImage(new Image(getClass().getResourceAsStream("/images/1.png")));
//        cardRoot.setOnMouseClicked(e -> onClick.run());
//    }

    public Hotel getHotel() {
        return hotel;
    }
}