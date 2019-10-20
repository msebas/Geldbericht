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
import java.util.regex.Pattern;

import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.converters.AccountStringConverter;
import org.mcservice.geldbericht.data.converters.CompanyStringConverter;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mcservice.geldbericht.pdf.MonthAccountTurnoverPDF;
import org.mcservice.javafx.BaseMatcherCallbackFilter;
import org.mcservice.javafx.control.date.MonthYearConverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PdfGeneratorPaneController {
	
	protected DbAbstractionLayer db=null;
	protected MonthAccountTurnover actBalanceMonth;
	
	@FXML
	protected AnchorPane mainPane;
	@FXML
	protected Button createButton;
	@FXML
	protected ComboBox<Company> companySelector;
	@FXML
	protected ComboBox<Account> accountSelector;
	@FXML
	protected ComboBox<LocalDate> monthSelector;
	
	public PdfGeneratorPaneController(DbAbstractionLayer db) {
		this.db=db;
	}
	
	@FXML
    public void initialize() {
		
		mainPane.setOnKeyPressed(keyEvent -> {
			if(keyEvent.getCode().equals(KeyCode.F5)) {
				this.updateData();
			}
		});
		
		BaseMatcherCallbackFilter filter;
		companySelector.setConverter(new CompanyStringConverter());
		accountSelector.setConverter(new AccountStringConverter());
		monthSelector.setConverter(new MonthYearConverter());
		monthSelector.setEditable(true);
		filter=new BaseMatcherCallbackFilter(Pattern.compile(MonthYearConverter.pattern));
		monthSelector.getEditor().setTextFormatter(new TextFormatter<LocalDate>(null,null,filter));
		monthSelector.setPromptText("MM.YY (1950-2049)");
		filter.setMatchCallback(noErrorPresent -> {
			if (noErrorPresent) {
				monthSelector.getEditor().getStyleClass().remove("field-validation-error");
			} else {
				if (!monthSelector.getEditor().getStyleClass().contains("field-validation-error"))
					monthSelector.getEditor().getStyleClass().add("field-validation-error");
			}
		});
		
		disableAsRequired();
		this.actBalanceMonth=null;
						
		ObservableList<Company> companies = FXCollections.observableList(db.getCompanies());
		
		companySelector.setItems(companies);
		if(companies.size()==1) {
			companySelector.setValue(companies.get(0));
			companyChanged();
		}
    }

	protected void disableAsRequired() {
		if(null==companySelector.getValue()) {
			accountSelector.setValue(null);
			accountSelector.setDisable(true);
		}
		if(null==accountSelector.getValue()) {
			monthSelector.setValue(null);
			monthSelector.setDisable(true);
		}
		if(null==monthSelector.getValue()) {
			this.actBalanceMonth=null;
			createButton.setDisable(true);
		}			
	}
		
	@FXML 
	protected void companyChanged() {
		Company actCompany=companySelector.getValue();

		if(actCompany!=null) {
			db.loadAccountsToCompany(actCompany);
			accountSelector.setItems(FXCollections.observableList(actCompany.getAccounts()));
			accountSelector.setDisable(false);
			if(accountSelector.getItems().size()==1) {
				accountSelector.setValue(accountSelector.getItems().get(0));
				accountChanged();
			}
		} else {
			disableAsRequired();			
		}		
	}

	@FXML 
	protected void accountChanged() {
		Account actAccount=accountSelector.getValue();

		if(actAccount!=null) {
			db.loadMonthsToAccount(actAccount);
			ObservableList<LocalDate> actMonthList=FXCollections.observableArrayList();
			for (MonthAccountTurnover turnover : actAccount.getBalanceMonths()) {
				actMonthList.add(turnover.getMonth());
			}
			
			monthSelector.setItems(actMonthList);
			monthSelector.setDisable(false);
			if(actMonthList.size()==1) {
				monthSelector.setValue(actMonthList.get(0));
				monthChanged();
			}
		} else {
			disableAsRequired();
		}
	}
	
	@FXML 
	protected void monthChanged() {
		LocalDate actMonth=monthSelector.getValue();
		
		if(actMonth!=null) {
			Account actAccount=accountSelector.getValue();
			for (MonthAccountTurnover month : actAccount.getBalanceMonths()) {
				if(month.isInMonth(actMonth)) {
					actBalanceMonth=month;
					break;
				}
			}
			if(actBalanceMonth==null) {
				createButton.setDisable(true);
				monthSelector.setValue(null);
				return;
			}
			
			createButton.setDisable(false);
		} else {
			disableAsRequired();
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
		File file = fileChooser.showSaveDialog((Stage) this.monthSelector.getScene().getWindow());
		
		if(file!=null && (file.isFile() || file.getAbsoluteFile().getParentFile().exists())) {
			FileOutputStream fileOut=new FileOutputStream(file);
			fileOut.write(pdf.getPdf());
			fileOut.close();
		}
	}
	
	public void updateData() {
		ObservableList<Company> companies = FXCollections.observableList(db.getCompanies());
		Company oldCompany=companySelector.getValue();
		Account oldAccount=accountSelector.getValue();
		MonthAccountTurnover oldMonth=actBalanceMonth;
		
		companySelector.setValue(null);
		accountSelector.setValue(null);
		monthSelector.setValue(null);		
		actBalanceMonth=null;
		
		if(oldCompany!=null) {
			Company tmpCompany=oldCompany;
			oldCompany=null;
			for (Company company : companies) {
				if(company.getUid()==tmpCompany.getUid()) {
					oldCompany=company;
					break;
				}
			}
		}
		if(null==oldCompany) {
			oldAccount=null;
		}
		if(companies.size()==1) {
			oldCompany=companies.get(0);
		}
		
		if(oldAccount!=null) {
			Account tmpAccount=oldAccount;
			oldAccount=null;
			db.loadAccountsToCompany(oldCompany);
			for (Account account : oldCompany.getAccounts()) {
				if(account.getUid()==tmpAccount.getUid()) {
					oldAccount=account;
					break;
				}
			}
		}
		if(null==oldAccount) {
			oldMonth=null;
		}
		
		if(oldMonth!=null) {
			MonthAccountTurnover tmpMonth=oldMonth;
			oldMonth=null;
			db.loadMonthsToAccount(oldAccount);
			for (MonthAccountTurnover month : oldAccount.getBalanceMonths()) {
				if(month.getUid()==tmpMonth.getUid()) {
					oldMonth=month;
					break;
				}
			}
		}
		
		companySelector.setItems(companies);
		companySelector.setValue(oldCompany);
		companyChanged();
		if(null!=oldCompany && null!=oldAccount) {
			accountSelector.setValue(oldAccount);
			accountChanged();
			if(null!=oldAccount && null!=oldMonth) {
				monthSelector.setValue(oldMonth.getMonth());
				monthChanged();
			}
		}
	}
}
