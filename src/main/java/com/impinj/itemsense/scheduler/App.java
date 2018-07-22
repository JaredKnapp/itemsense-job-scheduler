package com.impinj.itemsense.scheduler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import org.quartz.SchedulerException;

import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
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

	// Application Data
	private static ArrayList<ItemSenseConfig> config;

	/**
	 * Just a root getter for the controller to use
	 */
	public static BorderPane getRoot() {
		return root;
	}

	public static ArrayList<ItemSenseConfig> getConfig() {
		return config;
	}

	public static ItemSenseConfig getConfigByOID(String oid) throws Exception {
		try {
			return config.stream().filter(itemsense -> itemsense.getOid().equals(oid)).collect(Collectors.toList()).get(0);
		} catch (Exception e) {
			throw new Exception("OID Does not exist");
		}
	}

	public static String getApplicationId() {
		return UUID.randomUUID().toString();
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

			primaryStage.getIcons().add(new Image("/images/quartz_icon.png"));
			
			primaryStage.setOnCloseRequest(event -> {
			    System.out.println("Stage is closing");
			    try {
					JobService.getService().shutdown();
				} catch (SchedulerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
	        
			primaryStage.setScene(scene);
			primaryStage.setTitle("ItemSense Job Scheduler");
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		launch(args);
	}

	private void loadData() {
		try {
			config = DataService.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void startJobs() {
		JobService.getService().queueAllQuartzJobs();
	}
}
