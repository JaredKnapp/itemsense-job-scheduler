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
// Application show display on laptop
	private static final int _APPHEIGHT = 500;
	private static final int _APPWIDTH = 1300;

	// Creating a static root to pass to the controller
	private static final BorderPane root = new BorderPane();
	private static final String appId = UUID.randomUUID().toString();

	public static String getApplicationId() {
		return appId;
	}

	/**
	 * Just a root getter for the controller to use
	 */
	public static BorderPane getRoot() {
		return root;
	}

	public static void main(String[] args) {
		launch(args);
	}

	public static void startJobs() throws IOException {
		JobService.getService(true).queueAllJobs();
	}

	@Override
	public void init() throws Exception {
		this.loadData();
	}

	private void loadData() {
		try {
			DataService.getService(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

			Scene scene = new Scene(root, _APPWIDTH, _APPHEIGHT);
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
}
