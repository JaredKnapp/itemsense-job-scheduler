package com.impinj.itemsense.scheduler.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;
import com.impinj.itemsense.scheduler.service.ItemSenseService;
import com.impinj.itemsense.scheduler.service.quartz.ItemSenseJob;
import com.impinj.itemsense.scheduler.util.OIDGenerator;

import ch.qos.logback.classic.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * EditServerController is the control which corresponds to the EditServer.fxml
 */

public class EditServerController {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(EditServerController.class);

	private ItemSenseConfig configData;
	private ConfigurationController parent;

	Stage dialogStage;

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;
	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;
	@FXML // fx:id="txtName"
	private TextField txtName; // Value injected by FXMLLoader
	@FXML // fx:id="txtHostUrl"
	private TextField txtHostUrl; // Value injected by FXMLLoader
	@FXML // fx:id="txtUserName"
	private TextField txtUserName; // Value injected by FXMLLoader
	@FXML // fx:id="txtPassword"
	private PasswordField txtPassword; // Value injected by FXMLLoader
	@FXML // fx:id="cbUtcOffset"
	private ComboBox<Comparable> cbUtcOffset; // Value injected by FXMLLoader
	@FXML // fx:id="btnTestConnection"
	private Button btnDel; // Value injected by FXMLLoader
	@FXML // fx:id="chbIsActive"
	private CheckBox chbIsActive; // Value injected by FXMLLoader
	@FXML // fx:id="tblConfigJobs"
	private TableView<ItemSenseConfigJob> tblConfigJobs; // Value injected by FXMLLoader
	@FXML // fx:id="btnAdd"
	private Button btnAdd; // Value injected by FXMLLoader
	@FXML // fx:id="btnCancel"
	private Button btnCancel; // Value injected by FXMLLoader
	@FXML // fx:id="btnSave"
	private Button btnSave; // Value injected by FXMLLoader
	@FXML
	private Button btnTestConnection;

	private void loadJobEditor(ItemSenseConfigJob itemSenseConfig) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditJob.fxml"));
		try {
			Parent popup = loader.load();

			EditJobController controller = loader.getController();
			controller.injectParent(this);
			controller.setData(itemSenseConfig);

			dialogStage = new Stage();
			dialogStage.setTitle(itemSenseConfig.getOid() == null ? "Add Job" : "Edit Job");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(new Scene(popup));
			dialogStage.setUserData(itemSenseConfig);

			dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					System.out.println("Exiting");
				}
			});

			dialogStage.showAndWait();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void initialize() {
		// Setup Event listeners to prompt "Save
		chbIsActive.setOnMouseClicked(event -> btnSave.setDisable(false));
		txtName.setOnKeyTyped(event -> btnSave.setDisable(false));
		txtHostUrl.setOnKeyTyped(event -> btnSave.setDisable(false));
		txtUserName.setOnKeyTyped(event -> btnSave.setDisable(false));
		txtPassword.setOnKeyTyped(event -> btnSave.setDisable(false));
		cbUtcOffset.setOnAction(event -> btnSave.setDisable(false));
		for (int index = -11; index <= 12; index++) {
			cbUtcOffset.getItems().add(index);
		}
	}

	@FXML
	void btnAdd_OnAction(ActionEvent event) {
		loadJobEditor(new ItemSenseConfigJob());
	}

	@FXML
	void btnDel_OnAction(ActionEvent event) {
		ItemSenseConfigJob job = tblConfigJobs.getSelectionModel().getSelectedItem();
		tblConfigJobs.getItems().remove(job);
		btnSave.setDisable(false);
	}
	
	@FXML
	void btnCancel_OnCancel(ActionEvent event) {
		parent.server_onCancel();
	}

	@FXML
	void btnSave_OnAction(ActionEvent event) {
		configData.setActive(chbIsActive.isSelected());
		configData.setName(txtName.getText());
		configData.setUrl(txtHostUrl.getText());
		configData.setUsername(txtUserName.getText());
		configData.setPassword(txtPassword.getText());
		Object offsetValue = cbUtcOffset.getValue();
		if (offsetValue != null)
			configData.setUtcOffset(offsetValue.toString());
		configData.setJobList(tblConfigJobs.getItems());
		parent.onSaveData(configData);
		btnSave.setDisable(true);
	}

	@FXML
	void btnTestConnection_OnAction(ActionEvent event) {

		logger.info(String.format("Testing: User:%s PWD:%s URL:%s", txtUserName.getText(), txtPassword.getText(),
				txtHostUrl.getText()));

		if (txtUserName.getText() == null || txtPassword.getText() == null || txtHostUrl.getText() == null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Test Connection");
			alert.setHeaderText(null);
			alert.setContentText("URL, User Name, and Password are required fields.");
			alert.showAndWait();

			txtHostUrl.requestFocus();

		} else {

			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Test Connection");
			alert.setHeaderText(null);

			Task<Void> task = new Task<Void>() {

				@Override
				protected Void call() throws Exception {

					btnTestConnection.setDisable(true);

					boolean status = getItemSenseService().testConnection();

					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							btnTestConnection.setDisable(false);
							if (status) {
								alert.setAlertType(AlertType.INFORMATION);
								alert.setContentText("Connection to " + txtHostUrl.getText() + " succeeded!");
							} else {
								alert.setAlertType(AlertType.ERROR);
								alert.setContentText("Connection to " + txtHostUrl.getText() + " failed!");
							}
							alert.showAndWait();
						}
					});

					return null;

				}

			};

			new Thread(task).start();

		}
	}

	@FXML
	void tblConfigJobs_OnMouseClicked(MouseEvent event) {
		// double click
		if (event.getClickCount() == 2) {
			if (tblConfigJobs.getSelectionModel().getSelectedItem() != null) {
				ItemSenseConfigJob selectedItem = tblConfigJobs.getSelectionModel().getSelectedItem();
				loadJobEditor(selectedItem);
			}
		}
		btnDel.setDisable(!(tblConfigJobs.getSelectionModel().getSelectedItem() != null));
	}

	void injectParent(ConfigurationController configurationController) {
		this.parent = configurationController;
	}

	void setData(ItemSenseConfig serverData) {
		this.configData = serverData;

		if (serverData != null) {
			chbIsActive.setSelected(serverData.isActive());
			txtName.setText(serverData.getName());
			txtHostUrl.setText(serverData.getUrl());
			txtUserName.setText(serverData.getUsername());
			txtPassword.setText(serverData.getPassword());
			cbUtcOffset.setValue(serverData.getUtcOffset());

			List<ItemSenseConfigJob> jobs = serverData.getJobList();
			if (jobs == null)
				jobs = new ArrayList<ItemSenseConfigJob>();

			tblConfigJobs.setItems(FXCollections.observableArrayList(jobs));
		}
	}
	
	void job_onCancel() {
		dialogStage.close();
	}

	void job_onSave(ItemSenseConfigJob configJobData) {
		if (configJobData.getOid() == null) {
			tblConfigJobs.getItems().add(configJobData);
			configJobData.setOid(OIDGenerator.next());
			configJobData.setItemSenseOid(configData.getOid());
		}
		dialogStage.close();
		tblConfigJobs.refresh();
		btnSave.setDisable(false);
	}

	public ItemSenseService getItemSenseService() {
		ItemSenseConfig config = new ItemSenseConfig();
		config.setUrl(txtHostUrl.getText());
		config.setUsername(txtUserName.getText());
		config.setPassword(txtPassword.getText());
		return new ItemSenseService(config, null, null);
	}

}
