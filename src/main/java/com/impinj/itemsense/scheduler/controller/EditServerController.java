package com.impinj.itemsense.scheduler.controller;

import com.impinj.itemsense.client.coordinator.CoordinatorApiController;
import java.net.URL;
import java.util.ResourceBundle;

import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;
import java.net.URI;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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

public class EditServerController implements Initializable  {

	private ItemSenseConfig configData;
	private ConfigurationController parent;
    @FXML private TextField txtName;
    @FXML private TextField txtHostUrl;
    @FXML private TextField txtUserName;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtUtcOffset;
    @FXML private TableView jobsTableView;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}
	
    @FXML
    void btnCancel_OnAction(ActionEvent event) {
    	//Just Get Out!!
    }
    
    @FXML
    void btnTestConnection_OnAction(ActionEvent event) {
        Client client = ClientBuilder.newClient();
        client.register(HttpAuthenticationFeature.basic(txtUserName.getText(),
                                                        txtPassword.getText()));
        CoordinatorApiController itemsenseCoordinatorController = new CoordinatorApiController(client, URI.create(txtHostUrl.getText()));
        boolean success = true;
        try { 
            System.out.println("# configs="+ itemsenseCoordinatorController.getUserController().getUsers().size());
        } catch (Exception e)  {
            success = false;
        }
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Test Connection");
        // Header Text: null
        alert.setHeaderText(null);
        System.out.println("Connection to: "+ txtUserName.getText() +"/"+ txtPassword.getText() + " " +
                                  txtHostUrl.getText());
        if (success)
            alert.setContentText("Connection succeeded!");
        else
            alert.setContentText("Connection failed!");
 
        alert.showAndWait();
    }
    void load_JobsEditor(ItemSenseConfigJob itemSenseConfig)  {
       FXMLLoader loader = new FXMLLoader();
       loader.setLocation(getClass().getResource("/fxml/EditJob.fxml"));
       try {
            BorderPane popup = loader.load();
            Scene scene = new Scene(popup);
            EditJobController controller = loader.getController();
            controller.setItemSenseConfigJob(itemSenseConfig);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Job");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
       } catch (Exception e) { e.printStackTrace(); }
    }
    
    @FXML
    void btnAdd_OnAction(ActionEvent event) {
        load_JobsEditor(null);
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
    void TableViewOnMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) { // double click
            if(jobsTableView.getSelectionModel().getSelectedItem() != null) {
                TableViewSelectionModel selectionModel = jobsTableView.getSelectionModel();
                ObservableList selectedCells = selectionModel.getSelectedCells();
                TablePosition tablePosition = (TablePosition) selectedCells.get(0);
                System.out.println("Selected Value" + tablePosition);
                load_JobsEditor(configData.getJobList().get(tablePosition.getRow()));
            }
            System.out.println(jobsTableView.getSelectionModel().getSelectedItem());
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
                    System.out.println("class"+FXCollections.observableArrayList( serverData.getJobList()).getClass());
                    jobsTableView.setItems( FXCollections.observableArrayList( serverData.getJobList()));
		}
	}

	public void injectParent(ConfigurationController configurationController) {
		// TODO Auto-generated method stub
		this.parent = configurationController;
	}
}
