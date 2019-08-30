/*******************************************************************************
 * Copyright (C) 2019 Sebastian Müller <sebastian.mueller@mcservice.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.mcservice.geldbericht;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mcservice.javafx.control.table.ItemUpdateListener;
import org.mcservice.javafx.control.table.ReflectionTableView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javax.validation.Validation;
import javax.validation.Validator;

public class CompanyManagerController {
	
	protected DbAbstractionLayer db=null;
	protected ZonedDateTime lastUpdate=null;
	protected static Validator companyValidator=Validation.buildDefaultValidatorFactory().getValidator();
	private ItemUpdateListener labelUpdateListener;
	private boolean applyUpdate=false;
	
	@FXML
	private Label changesLabel=null;
	@FXML
	private Button persistButton=null;
	@FXML
	private ReflectionTableView<Company> companyTableView=null;
	private Runnable updatedNotification;
	
	public CompanyManagerController(DbAbstractionLayer db){
		this.db=db;
	}
	
	@FXML
    public void initialize() {
		lastUpdate=ZonedDateTime.now();
		ObservableList<Company> companies;
		companies=FXCollections.observableArrayList(this.db.getCompanies());
		List<Company> act=new ArrayList<Company>();
		for (Company company : companies) {
			if(companyValidator.validate(company).size()>0) {
				act.add(company);
			}
		}
		
		labelUpdateListener=new ItemUpdateListener() {
			@Override
			public void changed(boolean trueChange) {
				if (trueChange) {
					applyUpdate=true;
				}
				if(companyTableView.getItemsWithErrors().size()>0) {
					persistButton.setDisable(true);
					changesLabel.setText(String.format(
							"%d fehlerhafte Einträge",
							companyTableView.getItemsWithErrors().size()));
				} else {
					persistButton.setDisable(false);
					if (applyUpdate) {
						changesLabel.setText("Ungespeicherte Änderungen");
					}
				}				
			}
		};
		
		companyTableView.addEditCommitListener(labelUpdateListener);
		companyTableView.getItemsWithErrors().addAll(act);
		companyTableView.setItems(companies);
		companyTableView.setEditHandler();
    }

	@FXML
    private void add() throws IOException {
		Company newCompany = new Company("","","");
		companyTableView.getItems().add(newCompany);
		companyTableView.getItemsWithErrors().add(newCompany);
		labelUpdateListener.changed(true);
    }
	
	@FXML
    private void cancel() throws IOException {
        Stage stage = (Stage) companyTableView.getScene().getWindow();
        stage.close();
    }
	
	@FXML
    private void persist() throws IOException {
		db.manageCompanies(companyTableView.getItems(), lastUpdate);
		if(null!=updatedNotification) {
			Platform.runLater(updatedNotification);
		}
		cancel();
	}

	/**
	 * @return the updatedNotification
	 */
	public Runnable getUpdatedNotification() {
		return updatedNotification;
	}

	/**
	 * @param updatedNotification the updatedNotification to set
	 */
	public void setUpdatedNotification(Runnable updatedNotification) {
		this.updatedNotification = updatedNotification;
	}
	
}
