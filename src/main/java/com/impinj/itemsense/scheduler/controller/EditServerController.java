package com.impinj.itemsense.scheduler.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.impinj.itemsense.scheduler.model.ItemSense;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class EditServerController implements Initializable  {

	private ItemSense serverData;
	private ConfigurationController parent;

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
    private Button btnTestConnection;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}
	
    @FXML
    void btnCancel_OnAction(ActionEvent event) {
    	//Just Get Out!!
    }

    @FXML
    void btnSave_OnAction(ActionEvent event) {
		serverData.setName(txtName.getText());
		serverData.setUrl(txtHostUrl.getText());
		serverData.setUsername(txtUserName.getText());
		serverData.setPassword(txtPassword.getText());
		serverData.setUtcOffset(txtUtcOffset.getText());
		
		parent.onSaveData(serverData);
	}

	public void setData(ItemSense serverData) {
		this.serverData = serverData;

		if (serverData != null) {
			txtName.setText(serverData.getName());
			txtHostUrl.setText(serverData.getUrl());
			txtUserName.setText(serverData.getUsername());
			txtPassword.setText(serverData.getPassword());
			txtUtcOffset.setText(serverData.getUtcOffset());
		}
	}

	public void injectParent(ConfigurationController configurationController) {
		// TODO Auto-generated method stub
		this.parent = configurationController;
	}
}
