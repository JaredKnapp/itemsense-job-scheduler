package com.impinj.itemsense.scheduler;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import org.quartz.SchedulerException;

import com.impinj.itemsense.scheduler.service.DataService;
import com.impinj.itemsense.scheduler.service.JobService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {

	// Creating a static root to pass to the controller
	private static BorderPane root = new BorderPane();
	private static String appId = UUID.randomUUID().toString();

	/**
	 * Just a root getter for the controller to use
	 */
	public static BorderPane getRoot() {
		return root;
	}

	public static String getApplicationId() {
		return appId;
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() throws Exception {
		this.loadData();
	}

	@Override
	public void start(Stage primaryStage) {
		try {

			URL toolBarUrl = getClass().getResource("/fxml/NavPane.fxml");
			ToolBar toolBar = FXMLLoader.load(toolBarUrl);

			URL dashboardUrl = getClass().getResource("/fxml/Dashboard.fxml");
			BorderPane dashboard = FXMLLoader.load(dashboardUrl);

			root.setTop(toolBar);
			root.setCenter(dashboard);

			Scene scene = new Scene(root, 1500, 600);
			scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());

			primaryStage.setOnCloseRequest(event -> {
				try {
					JobService service = JobService.getService(false);
					if (service != null)
						service.shutdown();
				} catch (SchedulerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});

			primaryStage.setScene(scene);
			primaryStage.setTitle("ItemSense Job Scheduler");
			primaryStage.getIcons().add(new Image("/images/quartz_icon.png"));
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadData() {
		try {
			DataService.getService(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void startJobs() throws IOException {
		JobService.getService(true).queueAllJobs();
	}
}
