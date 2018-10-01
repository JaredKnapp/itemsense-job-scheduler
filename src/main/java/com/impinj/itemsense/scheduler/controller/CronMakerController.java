package com.impinj.itemsense.scheduler.controller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * @author jskinner
 * CronMakerController is the control which corresponds to the CronMaker.fxml
 * 
 */
public class CronMakerController implements Initializable {

    EditJobController parent;
    @FXML 
    private WebView CronWebView;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
    public void loadPage() {
        WebEngine webEngine = CronWebView.getEngine();
        webEngine.load("http://www.cronmaker.com/");
    }    
    public void btnOk_OnAction(ActionEvent event) {
        parent.onCancelCronMaker();
    }
     public void injectParent(EditJobController editJobController) {
	this.parent = editJobController;
     }
}
