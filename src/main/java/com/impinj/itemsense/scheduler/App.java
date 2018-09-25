package com.impinj.itemsense.scheduler;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import org.quartz.SchedulerException;

import com.impinj.itemsense.scheduler.service.DataService;
import com.impinj.itemsense.scheduler.service.quartz.QuartzService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class App extends Application {
// Application show display on laptop
	private static final int _APPHEIGHT = 650;
	private static final int _APPWIDTH = 900;

	// Creating a static root to pass to the controller
	private static final BorderPane root = new BorderPane();
	private static final String appId = UUID.randomUUID().toString();
	private static final String SERVICE_ARG = "service";
	private static final String HELP_ARG = "help";

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
		OptionParser parser = new OptionParser();
		parser.accepts(SERVICE_ARG); // used to specify that process is run headless
		parser.accepts(HELP_ARG); // used to specify that process is run headless
		OptionSet optionsSet = parser.parse(args);
		// Use arguments to decide path of execution
		if (optionsSet.has(HELP_ARG)) {
			System.out.println("com.impinj.itemsense.scheduler.App [-service]");
			System.exit(0); // Exit program
		} else if (optionsSet.has(SERVICE_ARG)) {
			System.out.println("Running as just a service. i.e. No GUI");
			try {
				DataService.getService(true);
				QuartzService.getService(true).queueAllJobs();
				// wait()
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			launch(args);
		}
	}

	public static void startJobs() throws IOException {
		QuartzService.getService(true).queueAllJobs();
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
					QuartzService service = QuartzService.getService(false);
					if (service != null)
						service.shutdown();
				} catch (SchedulerException e) {
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
