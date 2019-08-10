/*******************************************************************************
 * Copyright (C) 2019 Sebastian Müller <sebastian.mueller@mcservice.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.mcservice.geldbericht;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Iterator;
import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mcservice.javafx.control.table.ItemUpdateListener;
import org.mcservice.javafx.control.table.ReflectionTableView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import javax.validation.Validation;
import javax.validation.Validator;

public class VatTypeManagerController {
	
	protected DbAbstractionLayer db=null;
	protected ZonedDateTime lastUpdate=null;
	protected static Validator vatTypeValidator=Validation.buildDefaultValidatorFactory().getValidator();
	private ItemUpdateListener labelUpdateListener;
	private boolean applyUpdate=false;
	
	@FXML
	private Label changesLabel=null;
	@FXML
	private Button persistButton=null;
	@FXML
	private ReflectionTableView<VatType> vatTypeTableView=null;
	@FXML 
	private Button addButton = null;
	@FXML
	private ComboBox<VatType> defaultSelector = null;
	@FXML
	private HBox selectorHBox = null;

	
	public VatTypeManagerController(DbAbstractionLayer db) throws Exception{
		this.db=db;
	}
	
	@FXML
    public void initialize() {
		selectorHBox.setMaxHeight(Control.USE_COMPUTED_SIZE);
		lastUpdate=ZonedDateTime.now();
		ObservableList<VatType> referenceList = FXCollections.observableArrayList(this.db.getVatTypes());
		ObservableList<VatType> errorList = FXCollections.observableArrayList();
		persistButton.setDisable(true);
		applyUpdate=false;
		
		labelUpdateListener=new ItemUpdateListener() {
			@Override
			public void changed(boolean trueChange) {
				if (trueChange) {
					applyUpdate=true;
				}
				if(vatTypeTableView.getItemsWithErrors().size()>0) {
					persistButton.setDisable(true);
					changesLabel.setText(String.format(
							"%d fehlerhafte Einträge",
							vatTypeTableView.getItemsWithErrors().size()));
				} else {
					if (applyUpdate) {
						persistButton.setDisable(false);
						changesLabel.setText("Ungespeicherte Änderungen");
					}
				}				
			}
		};
		
		vatTypeTableView.addEditCommitListener(labelUpdateListener);
		vatTypeTableView.setItems(referenceList);
		vatTypeTableView.setItemsWithErrors(errorList);
		vatTypeTableView.setEditHandler();
				
		defaultSelector.setItems(referenceList);
		VatType actDefault = null;
		for (VatType vatType : referenceList) {
			if(vatTypeValidator.validate(vatType).size()>0) {
				errorList.add(vatType);
			}
			if(vatType.isDefaultVatType() && actDefault==null) {
				defaultSelector.setValue(vatType);
				actDefault=vatType;
			} else if(vatType.isDefaultVatType()) {
				vatType.setDefaultVatType(false);
				labelUpdateListener.changed(true);
			}
		}
		
		if(actDefault==null && referenceList.size()>0) {
			Iterator<VatType> it = referenceList.iterator();
			VatType vatType = it.next();
			while(it.hasNext() && vatType.isDisabledVatType())
				vatType = it.next();
			
			if(null!=vatType && !vatType.isDisabledVatType()) {
				defaultSelector.setValue(vatType);
				actDefault=vatType;
				vatType.setDefaultVatType(true);
				labelUpdateListener.changed(true);
			}
		}
		
		labelUpdateListener.changed(false);
    }
	
	@FXML
	private void selectDefaultVatType() {
		for (VatType vatType : vatTypeTableView.getItems()) {
			vatType.setDefaultVatType(false);
		}
		defaultSelector.getValue().setDefaultVatType(true);
	}

	@FXML
    private void add() throws IOException {
		VatType newVatType = new VatType("","",new BigDecimal(0),!vatTypeTableView.getItems().isEmpty());
		vatTypeTableView.getItems().add(newVatType);
		vatTypeTableView.getItemsWithErrors().add(newVatType);
		
		if(newVatType.isDefaultVatType()) {
			defaultSelector.setValue(newVatType);
		}
		
		labelUpdateListener.changed(true);
    }
	
	@FXML
    private void cancel() throws IOException {
        Stage stage = (Stage) vatTypeTableView.getScene().getWindow();
        stage.close();
    }
	
	@FXML
    private void persist() throws IOException {
		db.manageVatTypes(vatTypeTableView.getItems(), lastUpdate);
		cancel();
    }
	
}
