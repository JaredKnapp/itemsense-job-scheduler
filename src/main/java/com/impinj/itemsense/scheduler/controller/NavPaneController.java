package com.impinj.itemsense.scheduler.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import com.impinj.itemsense.scheduler.App;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NavPaneController {
	@FXML // fx:id="showDashboard"
	private Button btnShowDashboard;

	@FXML // fx:id="showConfiguration"
	private Button btnShowConfiguration;

	@FXML // fx:id="exitApplication"
	private Button btnExitApplication;
	
	@FXML 
	private Button btnPopup;


	private BorderPane dashboardPane;
	private BorderPane configurationPane;

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

			BorderPane appPane = App.getRoot();
			appPane.setCenter(dashboardPane);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Event handler for Configuration
	 */
	@FXML
	void switchToConfiguration(ActionEvent event) {

		try {

			if (configurationPane == null) {
				dashboardPane = (BorderPane) App.getRoot().getCenter();

				URL configurationUrl = getClass().getResource("/fxml/Configuration.fxml");
				configurationPane = FXMLLoader.load(configurationUrl);
			}

			BorderPane appPane = App.getRoot();
			appPane.setCenter(configurationPane);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Event handler for Exit Button
	 */
	@FXML
	void exitApplication(ActionEvent event) {
		System.exit(0);
	}
	
	@FXML 
	void btnPopup_OnPopup(ActionEvent event) throws IOException {
        URL popupUrl = getClass().getResource("/fxml/EditJob.fxml");
        BorderPane popup = FXMLLoader.load(popupUrl);
        Scene scene = new Scene(popup);
        
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit ItemSense Job");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();        
        
        
//		Dialog<ItemSenseConfigJob> dialog = new Dialog<>();
//		Optional<ItemSenseConfigJob> result = dialog.showAndWait();
		System.out.println("DONE");
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert btnShowDashboard != null : "fx:id=\"btnShowDashboard\" was not injected: check your FXML file 'NavPane.fxml'.";
		assert btnShowConfiguration != null : "fx:id=\"btnShowConfiguration\" was not injected: check your FXML file 'NavPane.fxml'.";
		assert btnExitApplication != null : "fx:id=\"btnExitApplication\" was not injected: check your FXML file 'NavPane.fxml'.";
		
		System.out.println("Initialized Component");
	}
}
