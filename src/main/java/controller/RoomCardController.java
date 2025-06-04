package controller;

import com.imaginationHoldings.domain.Hotel;
import com.imaginationHoldings.domain.Room;
import com.imaginationholdingsclient.MainApp;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

public class RoomCardController
{
    @javafx.fxml.FXML
    private VBox cardRoot;
    @javafx.fxml.FXML
    private Label roomNumber;
    @javafx.fxml.FXML
    private ImageView hotelImage;
    @javafx.fxml.FXML
    private Label roomLocation;
    @javafx.fxml.FXML
    private Label roomType;
    private Room room;
    private Runnable onClickHandler;
    @javafx.fxml.FXML
    private Label roomAvailability;


    @javafx.fxml.FXML
    public void initialize() {
    }
    public void setHotel(Room room, Runnable onClickHandler) {
        this.room = room;
        this.onClickHandler = onClickHandler;
        roomNumber.setText(String.valueOf(room.getRoomNumber()));
        roomType.setText(room.getType());
        roomLocation.setText(room.getLocation());
        if (room.isAvailable()) {
            roomAvailability.setTextFill(Paint.valueOf("Green"));
            roomAvailability.setText("Disponible");
        }else {
            roomAvailability.setTextFill(Paint.valueOf("Red"));
            roomAvailability.setText("Ocupado");}

        hotelImage.setImage(new Image(MainApp.class.getResourceAsStream("/images/roomDef.png")));

        cardRoot.setOnMouseClicked(e -> {
            if (this.onClickHandler != null) this.onClickHandler.run();
        });
    }
}