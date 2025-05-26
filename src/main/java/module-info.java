module com.imaginationholdingsclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.imaginationholdingsclient to javafx.fxml;
    exports com.imaginationholdingsclient;
}