package com.impinj.itemsense.scheduler.controller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * @author jskinner CronMakerController is the control which corresponds to the
 *         CronMaker.fxml
 * 
 */
public class CronMakerController{

	private final String CRON_SITE_URL = "http://www.cronmaker.com/";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnClose;

    @FXML
    private WebView cronWebView;

    @FXML
    private Label connectionLbl;


    @FXML
    void initialize() {
        assert btnClose != null : "fx:id=\"btnClose\" was not injected: check your FXML file 'CronMaker.fxml'.";
        assert cronWebView != null : "fx:id=\"cronWebView\" was not injected: check your FXML file 'CronMaker.fxml'.";
        assert connectionLbl != null : "fx:id=\"connectionLbl\" was not injected: check your FXML file 'CronMaker.fxml'.";

		Platform.runLater(() -> {
			WebEngine webEngine = cronWebView.getEngine();
			webEngine.load(CRON_SITE_URL);
		});
		
    }

	public void btnClose_OnAction(ActionEvent event) {
		Stage stage = (Stage) btnClose.getScene().getWindow();
		stage.close();
	}
}
