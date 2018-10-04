package com.impinj.itemsense.scheduler.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.model.TriggeredJob;
import com.impinj.itemsense.scheduler.service.quartz.JobResult;
import com.impinj.itemsense.scheduler.service.quartz.QuartzService;

import ch.qos.logback.classic.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.util.Duration;

/* 
    DashboardController is the control which corresponds to the CDashboard.fxml
*/


public class DashboardController {
	private static final int _TIMLINE_REPEAT_INTERVAL = 10;

	private static final Logger logger = (Logger) LoggerFactory.getLogger(DashboardController.class);

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="btnStart"
	private Button btnStart; // Value injected by FXMLLoader

	@FXML // fx:id="btnStop"
	private Button btnStop; // Value injected by FXMLLoader

	@FXML
	private TableView<TriggeredJob> tblTriggeredJobs;

	@FXML
	private TableView<JobResult> tblTriggeredJobResults;

	private Timeline refreshTimer;

	private void refreshTriggeredJobs() {
		List<TriggeredJob> triggeredJobs = QuartzService.getService(true).getQuartzJobs();
		tblTriggeredJobs.setItems(FXCollections.observableArrayList(triggeredJobs));
		tblTriggeredJobResults
				.setItems(FXCollections.observableArrayList(QuartzService.getService(true).getJobResults()));
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert btnStop != null : "fx:id=\"btnStop\" was not injected: check your FXML file 'Dashboard.fxml'.";
		assert btnStart != null : "fx:id=\"btnStart\" was not injected: check your FXML file 'Dashboard.fxml'.";
	}

	@FXML
	void btnStart_OnAction(ActionEvent event) {
		try {
			QuartzService.getService(true).queueAllJobs();
			refreshTriggeredJobs();

			refreshTimer = new Timeline(new KeyFrame(Duration.seconds(_TIMLINE_REPEAT_INTERVAL), new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					logger.debug("this is called every {} seconds on UI thread", _TIMLINE_REPEAT_INTERVAL);
					refreshTriggeredJobs();
				}
			}));
			refreshTimer.setCycleCount(Animation.INDEFINITE);
			refreshTimer.play();

		} catch (IOException e) {
			logger.error("Caught IOException", e);
		}
		btnStart.setDisable(true);
		btnStart.setVisible(false);
		btnStop.setDisable(false);
		btnStop.setVisible(true);
	}

	@FXML
	void btnStop_OnAction(ActionEvent event) {
		if (refreshTimer != null) {
			refreshTimer.stop();
		}

		QuartzService service = QuartzService.getService(false);
		if (service != null) {
			service.dequeueAllJobs();
		}

		tblTriggeredJobs.setItems(FXCollections.observableArrayList());

		btnStart.setDisable(false);
		btnStart.setVisible(true);
		btnStop.setDisable(true);
		btnStop.setVisible(false);
	}

}
