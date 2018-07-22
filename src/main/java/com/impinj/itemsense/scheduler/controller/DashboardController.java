package com.impinj.itemsense.scheduler.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.impinj.itemsense.scheduler.App;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DashboardController{
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="btnStart"
    private Button btnStart; // Value injected by FXMLLoader

    @FXML
    void btnStart_OnAction(ActionEvent event) {
    	App.startJobs();
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert btnStart != null : "fx:id=\"btnStart\" was not injected: check your FXML file 'Dashboard.fxml'.";
    }
}
