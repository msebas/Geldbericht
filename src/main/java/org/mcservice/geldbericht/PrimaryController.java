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
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.Transaction;
import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.database.DbAbstractionLayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class PrimaryController {
	
	protected DbAbstractionLayer db=null;
	protected ZonedDateTime lastUpdate=ZonedDateTime.now();

	protected ObservableList<Company> companies=null;
	protected ObservableList<Account> accounts=null;
	protected ObservableList<MonthAccountTurnover> months=null;
	protected ObservableList<Transaction> transactions=null;
	@FXML
	protected TextField receiptsInput;
	@FXML
	protected TextField spendingInput;
	@FXML
	protected TextField accountingContraAccountInput;
	@FXML
	protected TextField accountingCostGroupInput;
	@FXML
	protected TextField accountingCostCenterInput;
	@FXML
	protected TextField voucherInput;
	@FXML
	protected TextField transactionDateDayInput;
	@FXML
	protected TextField transactionDateMonthInput;
	@FXML
	protected ChoiceBox<VatType> vatInput;
	@FXML
	protected TextField inventoryNumberInput;
	@FXML
	protected TextField descriptionOfTransactionInput;
	@FXML
	protected TableView<Transaction> dataTableView;
	@FXML 
	protected Button saveChangesButton;
	@FXML 
	protected Button revertChangesButton;
	@FXML 
	protected Button insertLineButton;
	@FXML
	protected ChoiceBox<Company> companySelector;
	@FXML
	protected ChoiceBox<Account> accountSelector;
	@FXML
	protected DatePicker monthSelector;
	
	@FXML
    protected void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
	
	public PrimaryController() {
		this.db=new DbAbstractionLayer("/tmp/geldberichtTestDB.sqlite");
	}
	
	@FXML
    public void initialize() {
		List<VatType> vatTypes=db.getVatTypes();
		if (vatTypes.size()==0) {
			db.persistVatType(new VatType("Voll",19,true));
			db.persistVatType(new VatType("Reduziert",7,false));
			vatTypes=db.getVatTypes();
		}
		vatInput.setItems(FXCollections.observableList(vatTypes));
		for (int i = 0; i < vatTypes.size(); i++) {
			if(vatTypes.get(i).isDefaultVatType()) {
				vatInput.setValue(vatTypes.get(i));
				break;
			}
		}
		vatInput.setConverter(new VatTypeStringConverter());
		
		companies=FXCollections.observableList(db.getCompanies());
		accounts=FXCollections.observableList(db.getAccounts());
		months=FXCollections.observableList(db.getMonthAccountTurnovers());
		transactions=FXCollections.observableList(db.getTransactions());
		lastUpdate=ZonedDateTime.now();
		
		companySelector.setItems(companies);
		
		
		dataTableView.setItems(transactions);
		dataTableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory("receipts"));
		dataTableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory("spending"));
		dataTableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory("accountingContraAccount"));
		dataTableView.getColumns().get(3).setCellValueFactory(new PropertyValueFactory("accountingCostGroup"));
		dataTableView.getColumns().get(4).setCellValueFactory(new PropertyValueFactory("accountingCostCenter"));
		dataTableView.getColumns().get(5).setCellValueFactory(new PropertyValueFactory("voucher"));
		dataTableView.getColumns().get(6).setCellValueFactory(new PropertyValueFactory("transactionDate"));
		dataTableView.getColumns().get(7).setCellValueFactory(new PropertyValueFactory("vat"));
		dataTableView.getColumns().get(8).setCellValueFactory(new PropertyValueFactory("inventoryNumber"));
		dataTableView.getColumns().get(9).setCellValueFactory(new PropertyValueFactory("descriptionOfTransaction"));
    }
	
	
	@FXML
    protected void addRowByFields() throws IOException {
		int counter=this.dataTableView.getItems().size()+1;
		int receipts=Integer.parseInt(this.receiptsInput.getText().replace(",",""));
		int spending=Integer.parseInt(this.spendingInput.getText().replace(",",""));
		String contraAccount=this.accountingContraAccountInput.getText().strip();
		String costGroup=this.accountingCostGroupInput.getText().strip();
		String costCenter=this.accountingCostCenterInput.getText().strip();
		String voucher=this.voucherInput.getText().strip();
		short dateDay=Short.parseShort(this.transactionDateDayInput.getText().strip());
		short dateMonth=Short.parseShort(this.transactionDateMonthInput.getText().strip());
		LocalDate transactionDate=LocalDate.of(2019, dateMonth, dateDay);
		String inventoryNumber=this.inventoryNumberInput.getText().strip();
        Transaction tmp=new Transaction(counter,receipts,spending,
        		contraAccount.length()>0?Short.parseShort(contraAccount):null,
				costGroup.length()>0?Short.parseShort(costGroup):null,
				costCenter.length()>0?Short.parseShort(costCenter):null,
				voucher.length()>0?voucher:null,transactionDate,this.vatInput.getValue(),
				inventoryNumber.length()>0?Long.parseLong(inventoryNumber):null,
				this.descriptionOfTransactionInput.getText().strip());
        transactions.add(tmp);
    }
	
	@FXML
    protected void revertChanges() throws IOException {
		companies=FXCollections.observableList(db.getCompanies());
		accounts=FXCollections.observableList(db.getAccounts());
		months=FXCollections.observableList(db.getMonthAccountTurnovers());
		transactions=FXCollections.observableList(db.getTransactions());
		lastUpdate=ZonedDateTime.now();
    }
	
	@FXML
    protected void persistChanges() throws IOException {
		for (Company company : companies) {
			if (company.getLastChange().isAfter(lastUpdate))
				db.persistCompany(company);
		}
		for (Account Account : accounts) {
			if (Account.getLastChange().isAfter(lastUpdate))
				db.persistAccount(Account);
		}
		for (MonthAccountTurnover MonthAccountTurnover : months) {
			if (MonthAccountTurnover.getLastChange().isAfter(lastUpdate))
				db.persistMonthAccountTurnover(MonthAccountTurnover);
		}
		for (Transaction Transaction : transactions) {
			if (Transaction.getLastChange().isAfter(lastUpdate))
				db.persistTransaction(Transaction);
		}
		
		lastUpdate=ZonedDateTime.now();
    }
	
	@FXML
	protected void startCompanyManager() throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("companyManager.fxml"));
		CompanyManagerController controller = new CompanyManagerController(companies, db, lastUpdate);
		fxmlLoader.setController(controller);
		Scene scene = new Scene(fxmlLoader.load());
        
		// New window (Stage)
		Stage newWindow = new Stage();
		newWindow.setTitle("Second Stage");
		newWindow.setScene(scene);

		// Set position of second window, related to primary window.
		//newWindow.setX(primaryStage.getX() + 200);
		//newWindow.setY(primaryStage.getY() + 100);

		newWindow.show();
	}
	

	
	protected class TransactionComparator implements Comparator<Transaction>{
		@Override
		public int compare(Transaction o1, Transaction o2) {
			return o1.getNumber()-o2.getNumber();
		}
	}
	
	protected class VatTypeStringConverter extends StringConverter<VatType>{

		@Override
		public String toString(VatType vatType) {
			return String.format("%s (%.2f%%)",vatType.getName(),vatType.getValue());
		}

		@Override
		public VatType fromString(String string) {
			throw new RuntimeException("Not Implemented.");
		}
	}
}
