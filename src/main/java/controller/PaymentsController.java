package controller;

public class PaymentsController
{
    @javafx.fxml.FXML
    private TableColumn colAmount;
    @javafx.fxml.FXML
    private TableColumn colMethod;
    @javafx.fxml.FXML
    private ComboBox hotelComboBox;
    @javafx.fxml.FXML
    private TextField paymentAmountTextField;
    @javafx.fxml.FXML
    private ComboBox clientComboBox;
    @javafx.fxml.FXML
    private ComboBox paymentMethodComboBox;
    @javafx.fxml.FXML
    private DatePicker paymentDatePicker;
    @javafx.fxml.FXML
    private TableColumn colDate;
    @javafx.fxml.FXML
    private TableView paymentTableView;
    @javafx.fxml.FXML
    private TableColumn colCustomer;

    @javafx.fxml.FXML
    public void initialize() {
    }

    @javafx.fxml.FXML
    public void registerOnAction(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void cancelOnAction(ActionEvent actionEvent) {
    }
}