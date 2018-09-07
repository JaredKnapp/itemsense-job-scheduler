package com.impinj.itemsense.scheduler.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.job.ItemSenseHelper;
import com.impinj.itemsense.scheduler.job.ItemSenseJob;
import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;
import com.impinj.itemsense.scheduler.util.OIDGenerator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EditServerController implements Initializable {
	private static final Logger logger = LoggerFactory.getLogger(ItemSenseJob.class);
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
	@FXML // fx:id="txtUtcOffset"
	private TextField txtUtcOffset; // Value injected by FXMLLoader
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
	void btnAdd_OnAction(ActionEvent event) {
		loadJobEditor(new ItemSenseConfigJob());
	}

	@FXML
	void btnCancel_OnCancel(ActionEvent event) {
		parent.onCancel();
	}

	@FXML
	void btnDel_OnAction(ActionEvent event) {
		ItemSenseConfigJob job = tblConfigJobs.getSelectionModel().getSelectedItem();
		tblConfigJobs.getItems().remove(job);
	}

	@FXML
	void btnSave_OnAction(ActionEvent event) {
		configData.setActive(chbIsActive.isSelected());
		configData.setName(txtName.getText());
		configData.setUrl(txtHostUrl.getText());
		configData.setUsername(txtUserName.getText());
		configData.setPassword(txtPassword.getText());
		configData.setUtcOffset(txtUtcOffset.getText());

		configData.setJobList(tblConfigJobs.getItems());
		parent.onSaveData(configData);
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

			ItemSenseConfig config = new ItemSenseConfig();
			config.setUrl(txtHostUrl.getText());
			config.setUsername(txtUserName.getText());
			config.setPassword(txtPassword.getText());

			boolean success = new ItemSenseHelper(config, null, null).testConnection();

			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Test Connection");
			alert.setHeaderText(null);
			if (success)
				alert.setContentText("Connection succeeded!");
			else
				alert.setContentText("Connection failed!");

			alert.showAndWait();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}

	public void injectParent(ConfigurationController configurationController) {
		this.parent = configurationController;
	}

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
			dialogStage.showAndWait();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void onCancelJobData() {
		dialogStage.close();
	}

	void onUpdateJobData(ItemSenseConfigJob configJobData) {
		if (configJobData.getOid() == null) {
			tblConfigJobs.getItems().add(configJobData);
			configJobData.setOid(OIDGenerator.next());
		}
		dialogStage.close();
		tblConfigJobs.refresh();
	}

	public void setData(ItemSenseConfig serverData) {
		this.configData = serverData;

		if (serverData != null) {
			chbIsActive.setSelected(serverData.isActive());
			txtName.setText(serverData.getName());
			txtHostUrl.setText(serverData.getUrl());
			txtUserName.setText(serverData.getUsername());
			txtPassword.setText(serverData.getPassword());
			txtUtcOffset.setText(serverData.getUtcOffset());

			List<ItemSenseConfigJob> jobs = serverData.getJobList();
			if (jobs == null)
				jobs = new ArrayList<ItemSenseConfigJob>();

			tblConfigJobs.setItems(FXCollections.observableArrayList(jobs));
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

}
