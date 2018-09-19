
package com.impinj.itemsense.scheduler.controller;

import com.impinj.itemsense.client.coordinator.facility.Facility;
import org.apache.commons.lang3.StringUtils;

import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EditJobController {
	ItemSenseConfigJob itemSenseConfigJob;
	EditServerController parent;
	ItemSenseConfigJob jobData;

	@FXML
	private CheckBox chkActive;
	@FXML
	private ComboBox cbFacility;
	@FXML
	private TextField txtName;
	@FXML
	private ComboBox cbRecipe;
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
        private Stage dialogStage;

	public void btnCancel_OnAction(ActionEvent event) {
		parent.onCancelJobData();
	}

	public void btnOk_OnAction(ActionEvent event) {
		jobData.setActive(chkActive.isSelected());
		jobData.setName(txtName.getText());
		jobData.setFacility((String)cbFacility.getValue());
                jobData.setRecipe((String)cbRecipe.getValue());
		jobData.setSchedule(txtSchedule.getText());
		jobData.setStartDelay(txtStartDelay.getText());
		jobData.setDuration(Integer.parseInt(StringUtils.isNotBlank(txtDuration.getText()) ? txtDuration.getText() : "0"));
		jobData.setStopRunningJobs(chkStopRunning.isSelected());
		parent.onUpdateJobData(jobData);
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
	}
        
        @FXML
        void PressHyperlink_OnAction(ActionEvent event) {
            loadCronMainder();
        }
        
	private void loadCronMainder() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CronMaker.fxml"));
		try {
			Parent popup = loader.load();
			CronMakerController controller = loader.getController();
			controller.injectParent(this);
                        
			dialogStage = new Stage();
			dialogStage.setTitle("Cron Maker");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(new Scene(popup));
                        controller.loadPage();
			dialogStage.showAndWait();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
        void onCancelCronMaker() {
		dialogStage.close();
	}
	public void injectParent(EditServerController editServerController) {
		this.parent = editServerController;
	}

	public void setData(ItemSenseConfigJob itemSenseConfig) {
		jobData = itemSenseConfig;
		chkActive.setSelected(itemSenseConfig.isActive());
		txtName.setText(itemSenseConfig.getName());
		cbFacility.getItems().addAll(parent.getItemSenseHelper().getFacilityNames());
                cbFacility.setValue(itemSenseConfig.getFacility());
                cbRecipe.getItems().addAll(parent.getItemSenseHelper().getRecipeNames());
                cbRecipe.setValue(itemSenseConfig.getRecipe());
		txtSchedule.setText(itemSenseConfig.getSchedule());
		txtStartDelay.setText(itemSenseConfig.getStartDelay());
		if (itemSenseConfig.getDuration() != null)
			txtDuration.setText(itemSenseConfig.getDuration().toString());
		chkStopRunning.setSelected(itemSenseConfig.isStopRunningJobs());
	}
}
