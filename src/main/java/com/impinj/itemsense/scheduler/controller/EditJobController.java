
package com.impinj.itemsense.scheduler.controller;

import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class EditJobController {
	ItemSenseConfigJob itemSenseConfigJob;
	EditServerController parent;
	ItemSenseConfigJob jobData;

	@FXML
	private CheckBox chkActive;
	@FXML
	private TextField txtFacility;
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtReceipe;
	@FXML
	private TextField txtSchedule;
	@FXML
	private TextField txtStartDelay;
	@FXML
	private TextField txtDuration;
	@FXML
	private CheckBox chkStopRunning;
        @FXML
	private Hyperlink hyperlink;
        @FXML
        WebView CronWebView;

	public void btnCancel_OnAction(ActionEvent event) {
		parent.onCancelJobData();
	}

	public void btnOk_OnAction(ActionEvent event) {
		jobData.setActive(chkActive.isSelected());
		jobData.setName(txtName.getText());
		jobData.setFacility(txtFacility.getText());
		jobData.setRecipe(txtReceipe.getText());
		jobData.setSchedule(txtSchedule.getText());
		jobData.setStartDelay(txtStartDelay.getText());
		jobData.setDuration(Integer.parseInt(txtDuration.getText()));
		jobData.setStopRunningJobs(chkStopRunning.isSelected());
		parent.onUpdateJobData(jobData);
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
	}
        
        @FXML
        void PressHyperlink_OnAction(ActionEvent event) {
            WebEngine webEngine = CronWebView.getEngine();
            webEngine.load("http://java2s.com");
        }

	public void injectParent(EditServerController editServerController) {
		this.parent = editServerController;
	}

	public void setData(ItemSenseConfigJob itemSenseConfig) {
		jobData = itemSenseConfig;
		chkActive.setSelected(itemSenseConfig.isActive());
		txtName.setText(itemSenseConfig.getName());
		txtFacility.setText(itemSenseConfig.getFacility());
		txtReceipe.setText(itemSenseConfig.getRecipe());
		txtSchedule.setText(itemSenseConfig.getSchedule());
		txtStartDelay.setText(itemSenseConfig.getStartDelay());
		if (itemSenseConfig.getDuration() != null)
			txtDuration.setText(itemSenseConfig.getDuration().toString());
		chkStopRunning.setSelected(itemSenseConfig.isStopRunningJobs());
	}
}
