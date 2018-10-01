package com.impinj.itemsense.scheduler;

import java.io.IOException;
import java.net.URL;

import org.quartz.SchedulerException;
import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.service.DataService;
import com.impinj.itemsense.scheduler.service.quartz.QuartzService;

import ch.qos.logback.classic.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AppClient extends Application {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(AppClient.class);

	private static final int _APPHEIGHT = 650;
	private static final int _APPWIDTH = 900;
	private static final int _MIN_APPHEIGHT = 300;
	private static final int _MIN_APPWIDTH = 400;

	private static final BorderPane root = new BorderPane();

	public static BorderPane getRoot() {
		return root;
	}

	private static class ShutdownHook extends Thread {
		public void run() {
			logger.info("Shutting down {}.", App.getApplicationTitle());
			try {
				QuartzService service = QuartzService.getService(false);
				if (service != null) {
					service.shutdown();
				}
			} catch (SchedulerException e) {
				logger.error("Caught SchedulerException: {}.", e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void init() throws Exception {
		try {
			DataService.getService(true);
			Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		} catch (IOException e) {
			logger.error("Caught IOException: {}.", e);
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

			primaryStage.setScene(scene);
			primaryStage.setTitle(App.getApplicationTitle());
			primaryStage.setMinHeight(_MIN_APPHEIGHT);
			primaryStage.setMinWidth(_MIN_APPWIDTH);
			primaryStage.getIcons().add(new Image("/images/quartz_icon.png"));
			primaryStage.show();

		} catch (Exception e) {
			logger.error("Caught Exception: {}.", e);
			e.printStackTrace();
		}
	}
}
