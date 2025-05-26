module com.imaginationholdingsclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires imaginationHoldingsLibrary;

    opens com.imaginationholdingsclient to javafx.fxml;
    exports com.imaginationholdingsclient;
    exports controller;
    opens controller to javafx.fxml;
}