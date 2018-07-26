
package com.impinj.itemsense.scheduler.controller;

import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class EditJobController {
    ItemSenseConfigJob itemSenseConfigJob;
    EditServerController parent;
    ItemSenseConfigJob jobData;
    
    @FXML private boolean togActive;
    @FXML private TextField txtFacility;
    @FXML private TextField txtName;
    @FXML private TextField txtReceipe;
    @FXML private TextField txtSchedule;
    @FXML private TextField txtStartDelay;
    @FXML private TextField txtDuration;
    @FXML private boolean togStopRunning;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {

    }
/*    
    void setItemSenseConfigJob(ItemSenseConfigJob itemSenseConfigJob) {
        this.itemSenseConfigJob = itemSenseConfigJob;
        //this.itemSenseConfigJob.setName(txtName);
    }*/

	public void injectParent(EditServerController editServerController) {
		this.parent = editServerController;	
	}

	public void setData(ItemSenseConfigJob itemSenseConfig) {
		jobData = itemSenseConfig;
		
		txtName.setText(itemSenseConfig.getName());
		
	}
	
	public void btnOk_OnAction(ActionEvent event) {
		jobData.setName(txtName.getText());
		
		parent.onSaveData(jobData);
	}
}
