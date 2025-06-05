package controller;

import com.imaginationHoldings.domain.Guest;
import com.imaginationHoldings.protocol.Protocol;
import com.imaginationHoldings.protocol.Request;
import com.imaginationHoldings.protocol.Response;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
    private TableColumn<Guest, String> ageCol;
    @javafx.fxml.FXML
    private TableColumn<Guest, String> bDateCol;
    @javafx.fxml.FXML
    private TableColumn<Guest, String> idCol;
    @javafx.fxml.FXML
    private TableColumn<Guest, String> nameCol;
    @javafx.fxml.FXML
    private TableView<Guest> guestTableView;
    @javafx.fxml.FXML
    private TableColumn<Guest, String> lastNameCol;
    @javafx.fxml.FXML
    private TextField filterText;

    private Socket socket;
    private ObjectOutputStream objectOutput;
    private ObjectInputStream objectInput;
    private List<Guest> guests;

    private FilteredList<Guest> filteredGuests;
    private SortedList<Guest> sortedGuests;

    @javafx.fxml.FXML
    public void initialize() {
        guests = new ArrayList<>();
        try {
            socket = new Socket(MainViewController.SERVER_IP, MainViewController.PORT);
            objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.flush();
            objectInput = new ObjectInputStream(socket.getInputStream());

            Request request = new Request(Protocol.GET_ALL_GUESTS);
            objectOutput.writeObject(request);
            objectOutput.flush();
            guests = (List<Guest>) objectInput.readObject();

            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
            bDateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBirthDate().toString()));

            // Envolver en FilteredList
            filteredGuests = new FilteredList<>(FXCollections.observableArrayList(guests), p -> true);

            // Filtro por texto
            filterText.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredGuests.setPredicate(guest -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    return guest.getFirstName().toLowerCase().contains(lowerCaseFilter) ||
                            guest.getLastName().toLowerCase().contains(lowerCaseFilter);
                });
            });

            // Envolver en SortedList y enlazar con la TableView
            sortedGuests = new SortedList<>(filteredGuests);
            sortedGuests.comparatorProperty().bind(guestTableView.comparatorProperty());
            for (Guest g:sortedGuests){
                guestTableView.getItems().add(g);
            }


        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateTview() {
        this.guestTableView.getItems().clear();
        Request request = new Request(Protocol.GET_ALL_GUESTS);
        this.guestTableView.getItems().clear();
        try {
            objectOutput.writeObject(request);
            objectOutput.flush();
            guests = (List<Guest>) objectInput.readObject();
            //filteredGuests.getSource().setAll(guests);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        for(Guest guest : guests){
            this.guestTableView.getItems().add(guest);
        }
    }

    @javafx.fxml.FXML
    public void editRoomOnAction(ActionEvent actionEvent) {
        Guest selected = guestTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Seleccione un huésped primero.");
            alert.showAndWait();
            return;
        }

        GridPane gp = new GridPane();
        TextField tfName = new TextField(selected.getFirstName());
        TextField tfLname = new TextField(selected.getLastName());
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("M", "F");
        comboBox.setValue(selected.getGender());
        DatePicker datePicker = new DatePicker();
        if (selected.getBirthDate() != null)
            datePicker.setValue(selected.getBirthDate());

        gp.setHgap(10);
        gp.setVgap(10);
        gp.add(new Label("Nombre:"), 0, 0); gp.add(tfName, 1, 0);
        gp.add(new Label("Apellido:"), 0, 1); gp.add(tfLname, 1, 1);
        gp.add(new Label("Género:"), 0, 2); gp.add(comboBox, 1, 2);
        gp.add(new Label("Nacimiento:"), 0, 3); gp.add(datePicker, 1, 3);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Editar huésped");
        alert.setHeaderText("¿Desea guardar los cambios?");
        alert.getDialogPane().setContent(gp);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (tfName.getText().isEmpty() || tfLname.getText().isEmpty() || datePicker.getValue() == null || comboBox.getValue() == null) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Todos los campos son obligatorios.");
                    errorAlert.showAndWait();
                    return;
                }
                selected.setFirstName(tfName.getText());
                selected.setLastName(tfLname.getText());
                selected.setGender(comboBox.getValue());
                selected.setBirthDate(datePicker.getValue().toString());
            }
        });

        Request request = new Request(Protocol.EDIT_GUEST, selected);
        try {
            objectOutput.writeObject(request);
            objectOutput.flush();
            Object rawResponse = objectInput.readObject();
            Response response = (Response) rawResponse;

            Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
            resultAlert.setTitle("Guest Status");
            if (response.getCommand().equals(Response.GUEST_UPDATED)) {
                resultAlert.setContentText("Guest updated successfully.");
            } else {
                resultAlert.setContentText("Guest was not updated");
            }
            resultAlert.showAndWait();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        updateTview();
    }

    @javafx.fxml.FXML
    public void deleteRoomOnAction(ActionEvent actionEvent) {
        Guest selected = guestTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Seleccione un huésped primero.");
            alert.showAndWait();
            return;
        }

        Request request = new Request(Protocol.DELETE_GUEST, selected);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        try {
            objectOutput.writeObject(request);
            objectOutput.flush();
            Object rawResponse = objectInput.readObject();
            Response response = (Response) rawResponse;
            alert.setTitle("Guest Status");

            if (response.getCommand().equals(Response.GUEST_DELETED)) {
                alert.setContentText("Guest deleted successfully.");
            } else {
                alert.setContentText("Guest was not deleted");
            }
            alert.showAndWait();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        updateTview();
    }
}
