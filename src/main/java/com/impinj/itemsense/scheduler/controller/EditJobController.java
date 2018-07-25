
package com.impinj.itemsense.scheduler.controller;

import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class EditJobController {
    ItemSenseConfigJob itemSenseConfigJob;
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
    
    void setItemSenseConfigJob(ItemSenseConfigJob itemSenseConfigJob) {
        this.itemSenseConfigJob = itemSenseConfigJob;
        this.itemSenseConfigJob.setName(txtName);
    }
}
