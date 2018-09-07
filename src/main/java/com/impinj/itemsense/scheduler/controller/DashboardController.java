package com.impinj.itemsense.scheduler.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.impinj.itemsense.scheduler.job.JobResult;
import com.impinj.itemsense.scheduler.model.TriggeredJob;
import com.impinj.itemsense.scheduler.service.JobService;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.util.Duration;

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
	private TableView<TriggeredJob> tblTriggeredJobs;
        
        @FXML
	private TableView<JobResult> tblTriggeredJobResults;

	private Timeline refreshTimer;

	@FXML
	void btnStart_OnAction(ActionEvent event) {
		try {
			JobService.getService(true).queueAllJobs();
			refreshTriggeredJobs();

			refreshTimer = new Timeline(new KeyFrame(Duration.seconds(10), new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					System.out.println("this is called every 10 seconds on UI thread");
					refreshTriggeredJobs();
				}
			}));
			refreshTimer.setCycleCount(Animation.INDEFINITE);
			refreshTimer.play();

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
		if (refreshTimer != null) {
			refreshTimer.stop();
		}

		JobService service = JobService.getService(false);
		if (service != null) {
			service.dequeueAllJobs();
		}

		tblTriggeredJobs.setItems(FXCollections.observableArrayList());
		setTableHeight(3);

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

	private void refreshTriggeredJobs() {
		List<TriggeredJob> triggeredJobs = JobService.getService(true).getQuartzJobs();
		tblTriggeredJobs.setItems(FXCollections.observableArrayList(triggeredJobs));
                tblTriggeredJobResults.setItems( FXCollections.observableArrayList(JobService.getService(true).getJobResults()));
		setTableHeight(1.1);
	}

	private void setTableHeight(double rowPadding) {
		tblTriggeredJobs.prefHeightProperty().bind(tblTriggeredJobs.fixedCellSizeProperty().multiply(Bindings.size(tblTriggeredJobs.getItems()).add(rowPadding)));
		tblTriggeredJobs.minHeightProperty().bind(tblTriggeredJobs.prefHeightProperty());
		tblTriggeredJobs.maxHeightProperty().bind(tblTriggeredJobs.prefHeightProperty());
	}
}
