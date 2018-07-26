package com.impinj.itemsense.scheduler.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.service.DataService;
import com.impinj.itemsense.scheduler.util.OIDGenerator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

public class ConfigurationController implements Initializable {

	ObservableSet<ItemSenseConfig> observableSet;

	@FXML
	private ListView<ItemSenseConfig> lvItemSense = new ListView<>();
	@FXML
	private AnchorPane editPane;
	@FXML
	private Button btnNew;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			lvItemSense.setItems(FXCollections
					.observableArrayList(DataService.getService(true).getSystemConfig().getItemSenseConfigs()));
			lvItemSense.setCellFactory(new Callback<ListView<ItemSenseConfig>, ListCell<ItemSenseConfig>>() {

				@Override
				public ListCell<ItemSenseConfig> call(ListView<ItemSenseConfig> param) {
					ListCell<ItemSenseConfig> cell = new ListCell<ItemSenseConfig>() {

						@Override
						protected void updateItem(ItemSenseConfig item, boolean isEmpty) {
							super.updateItem(item, isEmpty);
							if (item != null) {
								setText(item.getName());
							}
						}

					};
					return cell;
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	void lvItemSense_OnKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.DELETE) {
			lvItemSense.getItems().remove(lvItemSense.getEditingIndex());
		}
	}

	@FXML
	public void lvItemSense_OnMouseClicked(MouseEvent event) {
		ItemSenseConfig configData = lvItemSense.getSelectionModel().getSelectedItem();
		editItemSense(configData);
	}

	@FXML
	public void btnNew_OnAction(ActionEvent event) {
		editItemSense(new ItemSenseConfig());
	}

	@FXML
	public void btnDelete_OnAction(ActionEvent event) {
		int index = lvItemSense.getSelectionModel().getSelectedIndex();
		if (index != -1) {
			final int newSelectedIdx = (index == lvItemSense.getItems().size() - 1) ? index - 1 : index;
			lvItemSense.getItems().remove(index);
			lvItemSense.getSelectionModel().select(newSelectedIdx);
		}
		System.out.printf("There are %s items remanining.", lvItemSense.getItems().size());
	}

	// @FXML
	// public void btnDelete_OnAction(ActionEvent event) {
	// Alert alert = new Alert(AlertType.WARNING, "Delete this thing?",
	// ButtonType.YES, ButtonType.NO);
	// alert.showAndWait();
	//
	// if (alert.getResult() == ButtonType.YES) {
	// btnDelete.setDisable(true);
	//
	// System.out.println("DELETING!!!");
	// }
	// }

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
			e.printStackTrace();
		}
	}

	public void onSaveData(ItemSenseConfig configData) {
		if (configData.getOid() == null) {
			lvItemSense.getItems().add(configData);
			configData.setOid(OIDGenerator.next());
		}
		// editPane.getChildren().clear();
		// lvItemSense.refresh();
	}

	public void onCancel() {
		editPane.getChildren().clear();
		lvItemSense.refresh();
	}

	public void onDelete() {
		lvItemSense.refresh();
	}

        @FXML
	public void btnSave_OnAction(ActionEvent event) {
		// TBD
	}
}
