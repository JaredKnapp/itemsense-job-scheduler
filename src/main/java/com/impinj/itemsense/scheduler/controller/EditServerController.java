package com.impinj.itemsense.scheduler.controller;

import com.impinj.itemsense.client.coordinator.CoordinatorApiController;
import java.net.URL;
import java.util.ResourceBundle;

import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;
import com.impinj.itemsense.scheduler.util.OIDGenerator;

import java.net.URI;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

public class EditServerController implements Initializable {

	private ItemSenseConfig configData;
	private ConfigurationController parent;
	Stage dialogStage;

	@FXML 
	private Button btnAdd;
	@FXML 
	private TextField txtName;
	@FXML
	private TextField txtHostUrl;
	@FXML
	private TextField txtUserName;
	@FXML
	private PasswordField txtPassword;
	@FXML
	private TextField txtUtcOffset;
	@FXML
	private TableView<ItemSenseConfigJob> tblConfigJobs;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}

	@FXML
	void btnCancel_OnAction(ActionEvent event) {
		// Just Get Out!!
	}

	@FXML
	void btnTestConnection_OnAction(ActionEvent event) {
		Client client = ClientBuilder.newClient();
		client.register(HttpAuthenticationFeature.basic(txtUserName.getText(), txtPassword.getText()));
		CoordinatorApiController itemsenseCoordinatorController = new CoordinatorApiController(client,
				URI.create(txtHostUrl.getText()));
		boolean success = true;
		try {
			System.out.println("# configs=" + itemsenseCoordinatorController.getUserController().getUsers().size());
		} catch (Exception e) {
			success = false;
		}
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Test Connection");
		// Header Text: null
		alert.setHeaderText(null);
		System.out.println(
				"Connection to: " + txtUserName.getText() + "/" + txtPassword.getText() + " " + txtHostUrl.getText());
		if (success)
			alert.setContentText("Connection succeeded!");
		else
			alert.setContentText("Connection failed!");

		alert.showAndWait();
	}

	@FXML
	void btnAdd_OnAction(ActionEvent event) {
		loadJobEditor(new ItemSenseConfigJob());
	}

	@FXML
	void btnSave_OnAction(ActionEvent event) {
		configData.setName(txtName.getText());
		configData.setUrl(txtHostUrl.getText());
		configData.setUsername(txtUserName.getText());
		configData.setPassword(txtPassword.getText());
		configData.setUtcOffset(txtUtcOffset.getText());

		parent.onSaveData(configData);
	}

	@FXML
	void tblConfigJobs_OnMouseClicked(MouseEvent event) {
		// double click
		if (event.getClickCount() == 2) {
			if (tblConfigJobs.getSelectionModel().getSelectedItem() != null) {
				TableViewSelectionModel<ItemSenseConfigJob> selectionModel = tblConfigJobs.getSelectionModel();
				ItemSenseConfigJob selectedItem = selectionModel.getSelectedItem();
				loadJobEditor(selectedItem);
			}
		}
	}

	private void loadJobEditor(ItemSenseConfigJob itemSenseConfig) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditJob.fxml"));
		try {
			Parent popup = loader.load();

			EditJobController controller = loader.getController();
			controller.injectParent(this);
			controller.setData(itemSenseConfig);

			dialogStage = new Stage();
			dialogStage.setTitle("Edit Job");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(new Scene(popup));
			dialogStage.showAndWait();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setData(ItemSenseConfig serverData) {
		this.configData = serverData;

		if (serverData != null) {
			txtName.setText(serverData.getName());
			txtHostUrl.setText(serverData.getUrl());
			txtUserName.setText(serverData.getUsername());
			txtPassword.setText(serverData.getPassword());
			txtUtcOffset.setText(serverData.getUtcOffset());
			List<ItemSenseConfigJob> jobs = serverData.getJobList();
			System.out.println("class" + FXCollections.observableArrayList(serverData.getJobList()).getClass());
			tblConfigJobs.setItems(FXCollections.observableArrayList(serverData.getJobList()));
		}
	}


	public void injectParent(ConfigurationController configurationController) {
		// TODO Auto-generated method stub
		this.parent = configurationController;
	}


	public void onSaveData(ItemSenseConfigJob configJobData) {
		if (configJobData.getOid() == null) {
			tblConfigJobs.getItems().add(configJobData);
			configJobData.setOid(OIDGenerator.next());
		}

		dialogStage.close();

		tblConfigJobs.refresh();
	}

	public void onCancel() {
		dialogStage.close();
	}

}
