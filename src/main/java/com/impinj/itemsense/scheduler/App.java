package com.impinj.itemsense.scheduler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import com.impinj.itemsense.scheduler.model.ItemSense;
import com.impinj.itemsense.scheduler.service.DataService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {

	// Creating a static root to pass to the controller
	private static BorderPane root = new BorderPane();
	
	// Application Data
	private static ArrayList<ItemSense> config;

	/**
	 * Just a root getter for the controller to use
	 */
	public static BorderPane getRoot() {
		return root;
	}
	
	public static ArrayList<ItemSense> getConfig() {
		return config;
	}

	@Override
	public void init() throws Exception {
		this.loadData();
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			
		    URL toolBarUrl = getClass().getResource("/fxml/NavPane.fxml");
		    ToolBar toolBar = FXMLLoader.load( toolBarUrl );

		    URL dashboardUrl = getClass().getResource("/fxml/Dashboard.fxml");
		    BorderPane dashboard = FXMLLoader.load( dashboardUrl );
		    
		    root.setTop(toolBar);
		    root.setCenter(dashboard);
		    
		    Scene scene = new Scene(root, 1500, 600);
		    scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
		    
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
}
