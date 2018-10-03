package com.impinj.itemsense.scheduler.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.service.DataService;
import com.impinj.itemsense.scheduler.util.OIDGenerator;

import ch.qos.logback.classic.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

public class ConfigurationController implements Initializable {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(ConfigurationController.class);

	@FXML
	private ListView<ItemSenseConfig> lvItemSense = new ListView<>();
	@FXML
	private AnchorPane editPane;
	@FXML
	private Button btnNew;
	@FXML
	private Button btnDelete;

	private void editItemSense(ItemSenseConfig serverData) {
		try {

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditServer.fxml"));
			Node editableForm = loader.load();

			EditServerController controller = loader.getController();
			controller.injectParent(this);
			controller.setData(serverData);

			editPane.getChildren().clear();
			editPane.getChildren().add(editableForm);

			AnchorPane.setBottomAnchor(editableForm, 0d);
			AnchorPane.setTopAnchor(editableForm, 0d);
			AnchorPane.setRightAnchor(editableForm, 0d);
			AnchorPane.setLeftAnchor(editableForm, 0d);

		} catch (IOException e) {
			logger.error("Caught IOException", e);
		}
	}

	private void populateView() {
		try {
			ObservableList<ItemSenseConfig> items = FXCollections
					.observableArrayList(DataService.getService(true).getSystemConfig().getItemSenseConfigs());
			lvItemSense.setItems(items);
			// Show first item in list when user first goes to ItemSense configuration
			// screen
			if (items.size() > 0) {
				lvItemSense.getSelectionModel().selectFirst();
				ItemSenseConfig configData = lvItemSense.getSelectionModel().getSelectedItem();
				editItemSense(configData);
				btnDelete.setDisable(false);
			}

			lvItemSense.setCellFactory(new Callback<ListView<ItemSenseConfig>, ListCell<ItemSenseConfig>>() {

				@Override
				public ListCell<ItemSenseConfig> call(ListView<ItemSenseConfig> param) {
					ListCell<ItemSenseConfig> cell = new ListCell<ItemSenseConfig>() {

						@Override
						protected void updateItem(ItemSenseConfig item, boolean isEmpty) {
							super.updateItem(item, isEmpty);
							if (item != null) {
								String rowText = item.getName() + (!item.isActive() ? " (disabled)" : "");
								setText(rowText);
							}
						}

					};
					return cell;
				}
			});

		} catch (IOException e) {
			logger.error("Caught IOException", e);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		populateView();
	}

	@FXML
	void btnDelete_OnAction(ActionEvent event) {
		ItemSenseConfig config = lvItemSense.getSelectionModel().getSelectedItem();
		if (config != null) {
			Alert alert = new Alert(AlertType.WARNING, "Delete this configuration?", ButtonType.YES, ButtonType.NO);
			alert.showAndWait();

			if (alert.getResult() == ButtonType.YES) {
				btnDelete.setDisable(true);
				try {
					DataService.getService(true).getSystemConfig().getItemSenseConfigs().remove(config);
					DataService.getService(true).saveSystemConfig();
				} catch (IOException e1) {
					logger.error("Caught IOException", e1);
				}

				this.initialize(null, null);
			}
		}
	}

	@FXML
	void btnNew_OnAction(ActionEvent event) {
		editItemSense(new ItemSenseConfig());
	}

	@FXML
	void lvItemSense_OnKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.DELETE) {
			lvItemSense.getItems().remove(lvItemSense.getEditingIndex());
		}
	}

	@FXML
	void lvItemSense_OnMouseClicked(MouseEvent event) {
		ItemSenseConfig configData = lvItemSense.getSelectionModel().getSelectedItem();
		editItemSense(configData);
	}

	void onCancel() {
		editPane.getChildren().clear();
		lvItemSense.refresh();
	}

	void onDelete() {
		try {
			DataService.getService(true).saveSystemConfig();
			lvItemSense.refresh();
		} catch (IOException e) {
			logger.error("Caught IOException", e);
		}
	}

	void onSaveData(ItemSenseConfig configData) {
		try {

			if (configData.getOid() == null) {
				configData.setOid(OIDGenerator.next());
				DataService.getService(true).getSystemConfig().getItemSenseConfigs().add(configData);
			}

			DataService.getService(true).saveSystemConfig();
			editPane.getChildren().clear();

			this.populateView();

		} catch (IOException e) {
			logger.error("Caught IOException", e);
		}
	}

}
