package controller;


import com.imaginationHoldings.protocol.Protocol;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainViewController {

    @FXML
    private TextArea outputArea;
    @FXML
    public void handleGetAllHotels() {
        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            writer.println(Protocol.GET_ALL_HOTELS);
            String response;
            while ((response = reader.readLine()) != null) {
                outputArea.appendText(response + "\n");
            }

        } catch (IOException e) {
            outputArea.appendText("Error al conectar: " + e.getMessage());
        }
    }
}
