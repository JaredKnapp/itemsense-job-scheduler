
package com.impinj.itemsense.scheduler.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;

import ch.qos.logback.classic.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EditJobController {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(EditJobController.class);

	private EditServerController parent;
	
	private ItemSenseConfigJob jobData;

    @FXML
    private BorderPane bpRoot;

    @FXML
    private Button btnOk;

    @FXML
    private CheckBox chkActive;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtSchedule;

    @FXML
    private TextField txtStartDelay;

    @FXML
    private TextField txtDuration;

    @FXML
    private CheckBox chkStopRunning;

    @FXML
    private Hyperlink cronHyperlink;

    @FXML
    private ComboBox<String> cbFacility;

    @FXML
    private ComboBox<String> cbRecipe;

	@FXML
	void initialize() {
		
	}

	@FXML
	void btnCancel_OnAction(ActionEvent event) {
		parent.job_onCancel();
	}

	@FXML
	void btnOk_OnAction(ActionEvent event) {
		StringBuilder errorText = new StringBuilder();
		String startDelayValue = "0";
		int durationValue = 0;

		// Validate Data
		try {
			startDelayValue = String.valueOf(
					Integer.parseInt(StringUtils.isNotBlank(txtStartDelay.getText()) ? txtStartDelay.getText() : "0"));
		} catch (NumberFormatException e1) {
			errorText.append("'Start Delay' must be a numeric value \n");
		}

		try {
			durationValue = Integer
					.parseInt(StringUtils.isNotBlank(txtDuration.getText()) ? txtDuration.getText() : "0");
		} catch (NumberFormatException e1) {
			errorText.append("'Duration' must be a numeric value \n");
		}

		// Show Error, or SAVE
		if (errorText.length() > 0) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Validation Errors");
			alert.setHeaderText("Please correct the following values");
			alert.setContentText(errorText.toString());
			alert.showAndWait();
		} else {
			jobData.setActive(chkActive.isSelected());
			jobData.setName(txtName.getText());
			jobData.setFacility((String) cbFacility.getValue());
			jobData.setRecipe((String) cbRecipe.getValue());
			jobData.setSchedule(txtSchedule.getText());
			jobData.setStartDelay(startDelayValue);
			jobData.setDuration(durationValue);
			jobData.setStopRunningJobs(chkStopRunning.isSelected());
			parent.job_onSave(jobData);
		}
	}

	@FXML
	void cronHyperlink_OnAction(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CronMaker.fxml"));
		try {
			Parent popup = loader.load();
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Cron Maker");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(new Scene(popup));
			dialogStage.showAndWait();
		} catch (IOException e) {
			logger.error("Caught IOException", e);
		}
	}

	public void injectParent(EditServerController editServerController) {
		this.parent = editServerController;
	}

	void setData(ItemSenseConfigJob itemSenseConfig) {
		jobData = itemSenseConfig;
		chkActive.setSelected(itemSenseConfig.isActive());
		txtName.setText(itemSenseConfig.getName());

		cbFacility.setValue(itemSenseConfig.getFacility());
		cbFacility.getItems().clear();
		cbFacility.getItems().add(itemSenseConfig.getFacility());

		cbRecipe.setValue(itemSenseConfig.getRecipe());
		cbRecipe.getItems().clear();
		cbRecipe.getItems().add(itemSenseConfig.getRecipe());

		txtSchedule.setText(itemSenseConfig.getSchedule());

		txtStartDelay.setText(itemSenseConfig.getStartDelay());
		if (itemSenseConfig.getDuration() != null) {
			txtDuration.setText(itemSenseConfig.getDuration().toString());
		}

		chkStopRunning.setSelected(itemSenseConfig.isStopRunningJobs());

		Task<Void> taskFacilityLookup = new Task<Void>() {
			@Override
			public Void call() {
				List<String> faciltiesNames = parent.getItemSenseService().getFacilityNames();
				if (faciltiesNames != null) {
					cbFacility.getItems().clear();
					cbFacility.getItems().addAll(faciltiesNames);
					cbFacility.setValue(itemSenseConfig.getFacility());
				}
				return null;
			}
		};

		Task<Void> taskRecipeLookup = new Task<Void>() {
			@Override
			public Void call() {
				List<String> recipeNames = parent.getItemSenseService().getRecipeNames();
				if (recipeNames != null) {
					cbRecipe.getItems().clear();
					cbRecipe.getItems().addAll(recipeNames);
					cbRecipe.setValue(itemSenseConfig.getRecipe());
				}
				return null;
			}
		};

		new Thread(taskFacilityLookup).start();
		new Thread(taskRecipeLookup).start();
	}
}
