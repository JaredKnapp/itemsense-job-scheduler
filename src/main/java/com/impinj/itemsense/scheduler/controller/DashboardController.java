package com.impinj.itemsense.scheduler.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.impinj.itemsense.scheduler.service.JobService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DashboardController {
	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="btnStart"
	private Button btnStart; // Value injected by FXMLLoader

	@FXML // fx:id="btnStop"
	private Button btnStop; // Value injected by FXMLLoader

	@FXML
	void btnStart_OnAction(ActionEvent event) {
		try {
			JobService.getService(true).queueAllJobs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		btnStart.setDisable(true);
		btnStart.setVisible(false);
		btnStop.setDisable(false);
		btnStop.setVisible(true);
	}

	@FXML
	void btnStop_OnAction(ActionEvent event) {
		JobService service = JobService.getService(false);
		if (service != null)
			service.dequeueAllJobs();
		btnStart.setDisable(false);
		btnStart.setVisible(true);
		btnStop.setDisable(true);
		btnStop.setVisible(false);
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert btnStop != null : "fx:id=\"btnStop\" was not injected: check your FXML file 'Dashboard.fxml'.";
		assert btnStart != null : "fx:id=\"btnStart\" was not injected: check your FXML file 'Dashboard.fxml'.";
	}
}
