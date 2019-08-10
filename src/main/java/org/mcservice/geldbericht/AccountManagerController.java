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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mcservice.javafx.control.table.ItemUpdateListener;
import org.mcservice.javafx.control.table.ReflectionTableView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.validation.Validation;
import javax.validation.Validator;

public class AccountManagerController {
	
	protected DbAbstractionLayer db=null;
	protected ZonedDateTime lastUpdate=null;
	protected static Validator accountValidator=Validation.buildDefaultValidatorFactory().getValidator();
	private ItemUpdateListener labelUpdateListener;
	private boolean applyUpdate=false;
	
	@FXML
	private Label changesLabel=null;
	@FXML
	private Button persistButton=null;
	@FXML
	private ReflectionTableView<Account> accountTableView=null;
	@FXML 
	private Button addButton = null;
	@FXML
	private ComboBox<Company> companySelector = null;
	
	private Company actCompany = null;
	
	@FXML
	private HBox selectorHBox = null;
	
	public AccountManagerController(DbAbstractionLayer db) throws Exception{
		this.db=db;
	}
	
	@FXML
    public void initialize() {
		selectorHBox.setMaxHeight(Control.USE_COMPUTED_SIZE);
		lastUpdate=ZonedDateTime.now();
		companySelector.setItems(FXCollections.observableArrayList(this.db.getCompanies()));
		companySelector.setConverter(new StringConverter<Company>() {

			@Override
			public String toString(Company object) {
				if(null==object)
					return "----";
				return String.format("%s (Nr: %s)", object.getCompanyName(), object.getCompanyNumber());
			}

			@Override
			public Company fromString(String string) {
				throw new RuntimeException("Should never be called, implementation error.");
			}			
		});
		
		labelUpdateListener=new ItemUpdateListener() {
			@Override
			public void changed(boolean trueChange) {
				if (trueChange) {
					applyUpdate=true;
				}
				if(accountTableView.getItemsWithErrors().size()>0) {
					persistButton.setDisable(true);
					changesLabel.setText(String.format(
							"%d fehlerhafte Einträge",
							accountTableView.getItemsWithErrors().size()));
				} else {
					if (applyUpdate) {
						persistButton.setDisable(false);
						changesLabel.setText("Ungespeicherte Änderungen");
					}
				}				
			}
		};
		
		accountTableView.addEditCommitListener(labelUpdateListener);
		accountTableView.setItems(null);
		accountTableView.setEditHandler();
		if(companySelector.getItems().size()==1) {
			companySelector.setValue(companySelector.getItems().get(0));
			selectCompany();
		}
		addButton.setDisable(null==actCompany);
		persistButton.setDisable(true);
    }
	
	@FXML
	private void selectCompany() {
		if(companySelector.getValue()==actCompany) {
			return;
		}
		if(applyUpdate) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Ungespeicherte Änderungen");
			alert.setHeaderText("Es gibt ungespeicherte Änderungen. Änderungen verwerfen?");
			
			//This works around "`Alert` boxes are broken on Linux. #222" 
			//Reference: (https://github.com/javafxports/openjdk-jfx/issues/222)
			//           (https://bugs.openjdk.java.net/browse/JDK-8179073) (possibly related)
			if(System.getProperty("os.name").contains("Linux")) {
				alert.setResizable(true);
				alert.onShownProperty().addListener(e -> { 
				    Platform.runLater(() -> alert.setResizable(false)); 
				});
			}

			Optional<ButtonType> option = alert.showAndWait();

			if (option.get() == null || option.get() == ButtonType.CANCEL) {
				// Revert change of company if aborted
				companySelector.setValue(actCompany);
				return;
			}
		}
		
		applyUpdate=false;
		actCompany=companySelector.getValue();
		ObservableList<Account> accounts;
		accounts=FXCollections.observableArrayList(new ArrayList<Account>(actCompany.getAccounts()));
		List<Account> act=new ArrayList<Account>();
		for (Account account : accounts) {
			if(accountValidator.validate(account).size()>0) {
				act.add(account);
			}
		}
		accountTableView.setItems(accounts);
		accountTableView.getItemsWithErrors().addAll(act);
		addButton.setDisable(null==actCompany);
	}

	@FXML
    private void add() throws IOException {
		if(null==actCompany)
			return;
		MonetaryAmount nullAmount=Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(0).create();
		Account newAccount = new Account("","",nullAmount,actCompany);
		accountTableView.getItems().add(newAccount);
		accountTableView.getItemsWithErrors().add(newAccount);
		labelUpdateListener.changed(true);
    }
	
	@FXML
    private void cancel() throws IOException {
        Stage stage = (Stage) accountTableView.getScene().getWindow();
        stage.close();
    }
	
	@FXML
    private void persist() throws IOException {
		db.manageAccounts(accountTableView.getItems(), lastUpdate);
		cancel();
    }
	
}
