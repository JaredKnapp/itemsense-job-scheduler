package com.impinj.itemsense.scheduler.controller;

import java.io.IOException;
import java.net.URL;

import com.impinj.itemsense.scheduler.AppClient;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class NavPaneController {
	@FXML // fx:id="showDashboard"
	private Button btnShowDashboard;

	@FXML // fx:id="showConfiguration"
	private Button btnShowConfiguration;

	@FXML // fx:id="exitApplication"
	private Button btnExitApplication;

	@FXML
	private BorderPane dashboardPane;

	@FXML
	private BorderPane configurationPane;

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert btnShowDashboard != null : "fx:id=\"btnShowDashboard\" was not injected: check your FXML file 'NavPane.fxml'.";
		assert btnShowConfiguration != null : "fx:id=\"btnShowConfiguration\" was not injected: check your FXML file 'NavPane.fxml'.";
		assert btnExitApplication != null : "fx:id=\"btnExitApplication\" was not injected: check your FXML file 'NavPane.fxml'.";
	}

	/**
	 * Event handler for Exit Button
	 */
	@FXML
	void exitApplication(ActionEvent event) {
		Platform.exit();
	}

	/**
	 * Event handler for Configuration
	 */
	@FXML
	void switchToConfiguration(ActionEvent event) {

		try {

			if (configurationPane == null) {
				dashboardPane = (BorderPane) AppClient.getRoot().getCenter();

				URL configurationUrl = getClass().getResource("/fxml/Configuration.fxml");
				configurationPane = FXMLLoader.load(configurationUrl);
			}

			BorderPane appPane = AppClient.getRoot();
			appPane.setCenter(configurationPane);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Event handler for Dashboard
	 */
	@FXML
	void switchToDashboard(ActionEvent event) {

		try {

			if (dashboardPane == null) {
				URL dashboardUrl = getClass().getResource("/fxml/Dashboard.fxml");
				dashboardPane = FXMLLoader.load(dashboardUrl);
				System.out.println("Creating new dashboard");
			}

			BorderPane appPane = AppClient.getRoot();
			appPane.setCenter(dashboardPane);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
