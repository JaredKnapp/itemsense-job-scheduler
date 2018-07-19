package com.impinj.itemsense.scheduler.controller;

import java.io.IOException;
import java.net.URL;

import com.impinj.itemsense.scheduler.App;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class NavPaneController {
	  @FXML // fx:id="showDashboard"
	  private Button showDashboard;

	  @FXML // fx:id="showConfiguration"
	  private Button showConfiguration;
	  
/*	  @FXML // fx:id="exitApplication"
	  private Button exitApplication; 
*/
	  /**
	   * Event handler for Dashboard
	   */
	  @FXML
	  void switchToDashboard(ActionEvent event) {
	    
	    try {
	      
	      URL dashboardUrl = getClass().getResource("/fxml/Dashboard.fxml");
	      BorderPane dashboardPane = FXMLLoader.load( dashboardUrl );
	      
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
	      
	      URL configurationUrl = getClass().getResource("/fxml/Configuration.fxml");
	      BorderPane configurationPane = FXMLLoader.load( configurationUrl );
	      
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
}
