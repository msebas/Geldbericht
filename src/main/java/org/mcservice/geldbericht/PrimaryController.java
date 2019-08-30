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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.regex.Pattern;

import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.Transaction;
import org.mcservice.geldbericht.data.converters.AccountStringConverter;
import org.mcservice.geldbericht.data.converters.CompanyStringConverter;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mcservice.geldbericht.pdf.MonthAccountTurnoverPDF;
import org.mcservice.javafx.control.date.MonthYearConverter;
import org.mcservice.javafx.BaseMatcherCallbackFilter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextFormatter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

public class PrimaryController {
	
	protected DbAbstractionLayer db=null;
	protected MonthAccountTurnover actBalanceMonth;
	
	@FXML
	protected Button pdfCreateButton;
	@FXML 
	protected VBox transactionTab;
	@FXML
	protected TransactionInputPaneController transactionTabController;	
	@FXML
	protected ChoiceBox<Company> pdfCompanySelector;
	@FXML
	protected ChoiceBox<Account> pdfAccountSelector;
	@FXML
	protected ComboBox<LocalDate> pdfMonthSelector;
	
	@FXML
    protected void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
	
	public PrimaryController(DbAbstractionLayer db) {
		this.db=db;
	}
	
	@FXML
    public void initialize() {
		
		BaseMatcherCallbackFilter filter;
		pdfCompanySelector.setConverter(new CompanyStringConverter());
		pdfAccountSelector.setConverter(new AccountStringConverter());
		pdfMonthSelector.setConverter(new MonthYearConverter());
		pdfMonthSelector.setEditable(true);
		filter=new BaseMatcherCallbackFilter(Pattern.compile(MonthYearConverter.pattern));
		pdfMonthSelector.getEditor().setTextFormatter(new TextFormatter<LocalDate>(null,null,filter));
		pdfMonthSelector.getEditor().setPromptText("MM.YY (1950-2049)");
		filter.setMatchCallback(noErrorPresent -> {
			if (noErrorPresent) {
				pdfMonthSelector.getEditor().getStyleClass().remove("field-validation-error");
			} else {
				if (!pdfMonthSelector.getEditor().getStyleClass().contains("field-validation-error"))
					pdfMonthSelector.getEditor().getStyleClass().add("field-validation-error");
			}
		});
		
		pdfMonthSelector.setDisable(true);
		pdfAccountSelector.getItems().clear();
		pdfCreateButton.setDisable(true);
		this.actBalanceMonth=null;
						
		ObservableList<Company> companies = FXCollections.observableList(db.getCompanies());
		
		pdfCompanySelector.setItems(companies);
		if(companies.size()==1) {
			pdfCompanySelector.setValue(companies.get(0));
		}
    }
		
	@FXML 
	protected void pdfCompanyChanged() {
		Company actCompany=pdfCompanySelector.getValue();

		if(actCompany!=null) {
			db.loadAccountsToCompany(actCompany);
			pdfAccountSelector.setItems(FXCollections.observableList(actCompany.getAccounts()));
			pdfAccountSelector.setDisable(false);
			if(pdfAccountSelector.getItems().size()==1) {
				pdfAccountSelector.setValue(pdfAccountSelector.getItems().get(0));
			}
		} else {
			pdfMonthSelector.setDisable(true);
			pdfAccountSelector.getItems().clear();
			pdfCreateButton.setDisable(true);
			this.actBalanceMonth=null;
		}		
	}

	@FXML 
	protected void pdfAccountChanged() {
		Account actAccount=pdfAccountSelector.getValue();

		if(actAccount!=null) {
			db.loadMonthsToAccount(actAccount);
			ObservableList<LocalDate> actMonthList=FXCollections.observableArrayList();
			for (MonthAccountTurnover turnover : actAccount.getBalanceMonths()) {
				actMonthList.add(turnover.getMonth());
			}
			
			pdfMonthSelector.setItems(actMonthList);
			pdfMonthSelector.setDisable(false);
		} else {
			pdfMonthSelector.setDisable(true);
			pdfCreateButton.setDisable(true);
			this.actBalanceMonth=null;
		}
	}
	
	@FXML 
	protected void pdfMonthChanged() {
		LocalDate actMonth=pdfMonthSelector.getValue();
		
		if(actMonth!=null) {
			Account actAccount=pdfAccountSelector.getValue();
			for (MonthAccountTurnover month : actAccount.getBalanceMonths()) {
				if(month.isInMonth(actMonth)) {
					actBalanceMonth=month;
					break;
				}
			}
			if(actBalanceMonth==null) {
				pdfCreateButton.setDisable(true);
				pdfMonthSelector.setValue(null);
				return;
			}
			
			pdfCreateButton.setDisable(false);
		} else {
			pdfCreateButton.setDisable(true);
		}
	}

	@FXML
	protected void createPDF() throws IOException{
		if(null==actBalanceMonth)
			return;
		db.loadTransactionsToMonth(actBalanceMonth);
		MonthAccountTurnoverPDF pdf=new MonthAccountTurnoverPDF(actBalanceMonth);
		
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setTitle("PDF Bericht speichern");
		File file = fileChooser.showSaveDialog((Stage) this.pdfMonthSelector.getScene().getWindow());
		
		if(file!=null && (file.isFile() || file.getAbsoluteFile().getParentFile().exists())) {
			FileOutputStream fileOut=new FileOutputStream(file);
			fileOut.write(pdf.getPdf());
			fileOut.close();
		}
	}

	@FXML
	protected void startCompanyManager() throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("CompanyManager.fxml"));
		CompanyManagerController controller = new CompanyManagerController(db);
		fxmlLoader.setController(controller);
		controller.setUpdatedNotification(() -> transactionTabController.updateData());
		Scene scene = new Scene(fxmlLoader.load());
        
		// New window (Stage)
		Stage newWindow = new Stage();
		newWindow.setTitle("Betriebmanager");
		newWindow.setScene(scene);

		newWindow.show();
	}
	
	@FXML
	protected void startAccountManager() throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("AccountManager.fxml"));
		AccountManagerController controller = new AccountManagerController(db);
		fxmlLoader.setController(controller);
		controller.setUpdatedNotification(() -> transactionTabController.updateData());
		Scene scene = new Scene(fxmlLoader.load());
        
		// New window (Stage)
		Stage newWindow = new Stage();
		newWindow.setTitle("");
		newWindow.setScene(scene);
		
		newWindow.show();
	}
	
	@FXML
	protected void startVatTypeManager() throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("VatTypeManager.fxml"));
		VatTypeManagerController controller = new VatTypeManagerController(db);
		controller.setUpdatedNotification(() -> transactionTabController.updateData());
		fxmlLoader.setController(controller);
		Scene scene = new Scene(fxmlLoader.load());
        
		// New window (Stage)
		Stage newWindow = new Stage();
		newWindow.setTitle("Second Stage");
		newWindow.setScene(scene);
		
		newWindow.show();
	}
	
	protected class TransactionComparator implements Comparator<Transaction>{
		@Override
		public int compare(Transaction o1, Transaction o2) {
			return o1.getNumber()-o2.getNumber();
		}
	}
}
