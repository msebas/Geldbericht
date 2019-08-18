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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryFormats;

import org.mcservice.geldbericht.data.AbstractDataObject;
import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.Transaction;
import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.data.converters.AccountStringConverter;
import org.mcservice.geldbericht.data.converters.CompanyStringConverter;
import org.mcservice.geldbericht.data.converters.VatTypeStringConverter;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mcservice.geldbericht.pdf.MonthAccountTurnoverPDF;
import org.mcservice.javafx.control.date.MonthYearConverter;
import org.mcservice.javafx.BaseMatcherCallbackFilter;
import org.mcservice.javafx.control.table.DefaultTableMonetaryAmountConverter;
import org.mcservice.javafx.control.table.ItemUpdateListener;
import org.mcservice.javafx.control.table.ReflectionTableView;

import org.mcservice.javafx.control.table.MemberVariable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MonthAccountTurnoverController {
	
	protected DbAbstractionLayer db=null;
	protected ZonedDateTime lastUpdate=ZonedDateTime.now();
	protected MonthAccountTurnover actTurnover=null;
	protected ListChangeListener<? super Transaction> transactionUpdateListener=null;
	protected ItemUpdateListener accountUpdateListener= null;
	protected Map<Account,ObservableList<MonthAccountTurnover>> turnoverList=new HashMap<Account,ObservableList<MonthAccountTurnover>>();
	protected ObservableList<LocalDate> actMonthList=null;
	protected Set<MonthAccountTurnover> possiblyChanged = new TreeSet<MonthAccountTurnover>();
	protected Set<MonthAccountTurnover> removed = new TreeSet<MonthAccountTurnover>();

	@FXML
	protected GridPane insertPane;
	@FXML
	protected HBox savePane;
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
	protected ReflectionTableView<Transaction> dataTableView;
	@FXML 
	protected Button saveChangesButton;
	@FXML 
	protected Button revertChangesButton;
	@FXML 
	protected Button insertLineButton;
	@FXML 
	protected Button pdfCreatePDFButton;
	@FXML
	protected ChoiceBox<Company> companySelector;
	@FXML
	protected ChoiceBox<Account> accountSelector;
	@FXML
	protected ComboBox<LocalDate> monthSelector;
	@FXML
	protected ChoiceBox<Company> pdfCompanySelector;
	@FXML
	protected ChoiceBox<Account> pdfAccountSelector;
	@FXML
	protected ComboBox<LocalDate> pdfMonthSelector;
	@FXML 
	protected Label balanceLabel;
	
	protected MonthAccountTurnover actBalanceMonth=null;
	
	@FXML
    protected void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
	
	public MonthAccountTurnoverController() {
		this.db=new DbAbstractionLayer("/tmp/geldberichtTestDB.sqlite");
		this.transactionUpdateListener=new ListChangeListener<Transaction>() {
			@Override
			public void onChanged(Change<? extends Transaction> c) {
				actTurnover.updateBalance();
			}
		};
	}
	
	@FXML
    public void initialize() {
		//Initialize vats list
		List<VatType> vatTypes=db.getVatTypes();
		if (vatTypes.size()==0) {
			db.persistVatType(new VatType("Voll","V",new BigDecimal(19),true));
			db.persistVatType(new VatType("Reduziert","R",new BigDecimal(7),false));
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
		
		//Initialize main transaction table view
		Field vatField;
		try {
			vatField = Transaction.class.getDeclaredField("vat");
		} catch (NoSuchFieldException | SecurityException e) {
			//This should only happen if someone changes Transaction.class
			throw new RuntimeException(e);
		}
		for (VatType v:vatTypes) {
			dataTableView.getColumnInternalLists().get(vatField).add(v);
		}
		
		companySelector.setConverter(new CompanyStringConverter());
		accountSelector.setConverter(new AccountStringConverter());
		monthSelector.setConverter(new MonthYearConverter());
		monthSelector.setEditable(true);
		BaseMatcherCallbackFilter filter=new BaseMatcherCallbackFilter(Pattern.compile(MonthYearConverter.pattern));
		monthSelector.getEditor().setTextFormatter(new TextFormatter<LocalDate>(null,null,filter));
		monthSelector.getEditor().setPromptText("MM.YY (1950-2049)");
		filter.setMatchCallback(noErrorPresent -> {
			if (noErrorPresent) {
				monthSelector.getEditor().getStyleClass().remove("field-validation-error");
			} else {
				if (!monthSelector.getEditor().getStyleClass().contains("field-validation-error"))
					monthSelector.getEditor().getStyleClass().add("field-validation-error");
			}
		});
		
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
		
		
		accountUpdateListener = changed -> updateAccountingLabels();
		
		disableAll();
				
		ObservableList<Company> companies = FXCollections.observableList(db.getCompanies());
		
		companySelector.setItems(companies);
		pdfCompanySelector.setItems(companies);
		if(companies.size()==1) {
			companySelector.setValue(companies.get(0));
			pdfCompanySelector.setValue(companies.get(0));
		}

		lastUpdate=ZonedDateTime.now();
		
		for(TableColumn<Transaction, ?> column:dataTableView.getColumns()) {
			EventHandler<?> editCommit = column.getOnEditCommit();
			if(editCommit instanceof MemberVariable) {
				@SuppressWarnings("unchecked")
				MemberVariable<Transaction, ?> memberVaraiable=(MemberVariable<Transaction, ?>) editCommit;
				if(memberVaraiable.getType().equals(MonetaryAmount.class)) {
					memberVaraiable.addListener(accountUpdateListener);
				}
			}
		}
    }
	
	protected void updateAccountingLabels() {
		balanceLabel.setText("Kein Konot gewählt");
		Account actAccount=accountSelector.getValue();
		if(actAccount!=null) {
			actAccount.updateBalance();
			MonetaryAmount balance=actAccount.getBalance();
			//if(actTurnover!=null)
			//	balance.add(actTurnover.updateBalance());
			String strBalance=MonetaryFormats.getAmountFormat(Locale.GERMANY).format(balance);
			List<MonthAccountTurnover> actAccountTurnovers=turnoverList.getOrDefault(actAccount,null);
			if(null==actAccountTurnovers)
				actAccountTurnovers=actAccount.getBalanceMonths();
			int actMonths=actAccountTurnovers.size();
					
			balanceLabel.setText(String.format("Konotstand: %s über %d Monate",strBalance,actMonths));
		}
	}
	
	
	@FXML
    protected void addRowByFields() throws IOException {
		int counter=this.dataTableView.getItems().size()+1;
		DefaultTableMonetaryAmountConverter moneyFormatter = new DefaultTableMonetaryAmountConverter();
		MonetaryAmount receipts=moneyFormatter.fromString(this.receiptsInput.getText());
		MonetaryAmount spending=moneyFormatter.fromString(this.spendingInput.getText());
		String contraAccount=this.accountingContraAccountInput.getText().strip();
		String costGroup=this.accountingCostGroupInput.getText().strip();
		String costCenter=this.accountingCostCenterInput.getText().strip();
		String voucher=this.voucherInput.getText().strip();
		short dateDay=Short.parseShort(this.transactionDateDayInput.getText().strip());
		short dateMonth=Short.parseShort(this.transactionDateMonthInput.getText().strip());
		LocalDate transactionDate=LocalDate.of(2019, dateMonth, dateDay);
		String inventoryNumber=this.inventoryNumberInput.getText().strip();
        Transaction tmp=new Transaction(counter,receipts,spending,
        		contraAccount.length()>0?Integer.parseInt(contraAccount):null,
				costGroup.length()>0?Integer.parseInt(costGroup):null,
				costCenter.length()>0?Integer.parseInt(costCenter):null,
				voucher.length()>0?voucher:null,transactionDate,this.vatInput.getValue(),
				inventoryNumber.length()>0?Long.parseLong(inventoryNumber):null,
				this.descriptionOfTransactionInput.getText().strip());
        dataTableView.getItems().add(tmp);
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
			}
		} else {
			disableAll();
		}		
	}
	
	@FXML 
	protected void accountChanged() {
		Account actAccount=accountSelector.getValue();

		if(actAccount!=null) {
			if(!turnoverList.containsKey(actAccount)) {
				db.loadMonthsToAccount(actAccount);
				turnoverList.put(actAccount, FXCollections.observableList(actAccount.getBalanceMonths()));
			}
			actMonthList=FXCollections.observableArrayList();
			for (MonthAccountTurnover turnover : turnoverList.get(actAccount)) {
				actMonthList.add(turnover.getMonth());
			}
			
			monthSelector.setItems(actMonthList);
			monthSelector.setDisable(false);
		} else {
			insertPane.setDisable(true);
			savePane.setDisable(true);
			dataTableView.setDisable(true);
			monthSelector.setDisable(true);
		}
		updateAccountingLabels();
	}
	
	@FXML 
	protected void monthChanged() {
		LocalDate actMonth=monthSelector.getValue();
		
		if(actMonth!=null) {
			Account actAccount=accountSelector.getValue();
			for (MonthAccountTurnover month : turnoverList.get(actAccount)) {
				if(month.isInMonth(actMonth)) {
					actTurnover=month;
					break;
				}
			}
			if(actTurnover==null) {
				actTurnover=MonthAccountTurnover.getEmptyMonthAccountTurnover(actMonth, actAccount);
				turnoverList.get(actAccount).add(actTurnover);
			} else if(actTurnover.getUid()!=null){
				db.loadTransactionsToMonth(actTurnover);
			}
			possiblyChanged.add(actTurnover);
			
			dataTableView.getItems().removeListener(transactionUpdateListener);
			ObservableList<Transaction> transactionList=FXCollections.observableList(actTurnover.getTransactions());
			transactionList.addListener(transactionUpdateListener);
			dataTableView.setItems(transactionList);
			
			insertPane.setDisable(false);
			savePane.setDisable(false);
			dataTableView.setDisable(false);
		} else {
			insertPane.setDisable(true);
			savePane.setDisable(true);
			dataTableView.setDisable(true);
		}
	}
	
	@FXML 
	protected void deleteActualMonth() {
		throw new RuntimeException("Not done yet.");
	}
	
	@FXML
    protected void revertChanges() throws IOException {
		this.companySelector.setItems(FXCollections.observableList(db.getCompanies()));
		companySelector.setValue(null);
		accountSelector.setValue(null);
		monthSelector.setDisable(true);
		lastUpdate=ZonedDateTime.now();
    }
	
	@FXML
    protected void persistChanges() throws IOException {
		ArrayList<List<? extends AbstractDataObject>> dataList = new ArrayList<List<? extends AbstractDataObject>>();
		
		for(Entry<Account, ObservableList<MonthAccountTurnover>> act : turnoverList.entrySet()) {
			act.getKey().updateBalance();
			dataList.add(act.getValue());
		}
		for (MonthAccountTurnover turnover : possiblyChanged) {
			dataList.add(turnover.getTransactions());
		}
		dataList.add(new ArrayList<Account>(turnoverList.keySet()));
		db.mergeData(dataList);
		lastUpdate=ZonedDateTime.now();
		updateAccountingLabels();
    }
	
	@FXML
	protected void startCompanyManager() throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("CompanyManager.fxml"));
		CompanyManagerController controller = new CompanyManagerController(db);
		fxmlLoader.setController(controller);
		Scene scene = new Scene(fxmlLoader.load());
        
		// New window (Stage)
		Stage newWindow = new Stage();
		newWindow.setTitle("Betriebmanager");
		newWindow.setScene(scene);

		newWindow.show();
		//TODO update companies on close
	}
	
	@FXML
	protected void startAccountManager() throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("AccountManager.fxml"));
		AccountManagerController controller = new AccountManagerController(db);
		fxmlLoader.setController(controller);
		Scene scene = new Scene(fxmlLoader.load());
        
		// New window (Stage)
		Stage newWindow = new Stage();
		newWindow.setTitle("");
		newWindow.setScene(scene);

		newWindow.show();
		//TODO update accounts on close
	}
	
	@FXML 
	protected void pdfCompanyChanged() {
		Company actCompany=companySelector.getValue();

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
			pdfCreatePDFButton.setDisable(true);
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
			pdfCreatePDFButton.setDisable(true);
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
				pdfCreatePDFButton.setDisable(true);
				pdfMonthSelector.setValue(null);
				return;
			}
			
			pdfCreatePDFButton.setDisable(false);
		} else {
			pdfCreatePDFButton.setDisable(true);
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
	protected void startVatTypeManager() throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("VatTypeManager.fxml"));
		VatTypeManagerController controller = new VatTypeManagerController(db);
		fxmlLoader.setController(controller);
		Scene scene = new Scene(fxmlLoader.load());
        
		// New window (Stage)
		Stage newWindow = new Stage();
		newWindow.setTitle("Second Stage");
		newWindow.setScene(scene);

		newWindow.show();
		//TODO update vattypes on close
	}


	private void disableAll() {
		insertPane.setDisable(true);
		savePane.setDisable(true);
		dataTableView.setDisable(true);
		monthSelector.setDisable(true);
		accountSelector.getItems().clear();
	}
	
	
	protected class TransactionComparator implements Comparator<Transaction>{
		@Override
		public int compare(Transaction o1, Transaction o2) {
			return o1.getNumber()-o2.getNumber();
		}
	}
}
