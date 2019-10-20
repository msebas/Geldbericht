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
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryFormats;

import org.javamoney.moneta.Money;
import org.mcservice.geldbericht.data.AbstractDataObject;
import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.Transaction;
import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.data.converters.AccountStringConverter;
import org.mcservice.geldbericht.data.converters.CompanyStringConverter;
import org.mcservice.geldbericht.data.converters.VatTypeStringConverter;
import org.mcservice.geldbericht.database.BackgroundDbThread;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mcservice.javafx.control.date.DayMonthField;
import org.mcservice.javafx.control.date.MonthYearConverter;
import org.mcservice.javafx.AnnotationBasedFormatter;
import org.mcservice.javafx.BaseMatcherCallbackFilter;
import org.mcservice.javafx.control.table.DefaultTableMonetaryAmountConverter;
import org.mcservice.javafx.control.table.ItemUpdateListener;
import org.mcservice.javafx.control.table.ReflectionTableView;

import org.mcservice.javafx.control.table.MemberVariable;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class TransactionInputPaneController {
	
	protected DbAbstractionLayer db = null;
	protected BackgroundDbThread backgroundDatabase = null;
	protected ZonedDateTime lastUpdate = ZonedDateTime.now();
	protected Company actCompany = null;
	protected Account actAccount = null;
	protected MonthAccountTurnover actTurnover = null;
	protected ListChangeListener<? super Transaction> transactionUpdateListener = null;
	protected ItemUpdateListener accountUpdateListener =  null;
	protected Map<Account,ObservableList<MonthAccountTurnover>> turnoverList = new HashMap<Account,ObservableList<MonthAccountTurnover>>();
	protected Set<AbstractDataObject> deletedItems  =  new HashSet<AbstractDataObject>();
	protected ObservableList<LocalDate> actMonthList = null;
	protected VatType defaultVat;
	
	@FXML
	protected SplitPane mainPane;
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
	protected DayMonthField transactionDateInput;
	@FXML
	protected ComboBox<VatType> vatInput;
	@FXML
	protected TextField inventoryNumberInput;
	@FXML
	protected TextField descriptionOfTransactionInput;
	@FXML
	protected ReflectionTableView<Transaction> dataTableView;
	@FXML
	protected Button saveChangesButton;
	//@FXML
	//protected Button revertChangesButton;
	@FXML
	protected Button deleteActualMonthButton;
	@FXML
	protected Button insertLineButton;
	@FXML
	protected ComboBox<Company> companySelector;
	@FXML
	protected ComboBox<Account> accountSelector;
	@FXML
	protected ComboBox<LocalDate> monthSelector;
	@FXML
	protected Label balanceLabel;
	@FXML
	protected Label actAccountingYearLabel;
	
	
			
	public TransactionInputPaneController(DbAbstractionLayer db, BackgroundDbThread backgroundDatabase) {
		this.db=db;
		this.transactionUpdateListener=new ListChangeListener<Transaction>() {
			@Override
			public void onChanged(Change<? extends Transaction> c) {
				actTurnover.updateBalance();
			}
		};
		
		this.backgroundDatabase = backgroundDatabase;
	}
	
	@FXML
    public void initialize() {
		//Initialize vats list and configure input fields
		setVatTypes();
		configureInputFields();
		
		mainPane.setOnKeyPressed(keyEvent -> {
			if(keyEvent.getCode().equals(KeyCode.F5)) {
				this.updateData();
			}
		});
		
		companySelector.setConverter(new CompanyStringConverter());
		accountSelector.setConverter(new AccountStringConverter());
		monthSelector.setConverter(new MonthYearConverter());
		monthSelector.setEditable(true);
		BaseMatcherCallbackFilter filter=new BaseMatcherCallbackFilter(Pattern.compile(MonthYearConverter.pattern));
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
		
		accountUpdateListener = changed -> updateAccountingLabels();
						
		setCompanies();

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
		
		//Create context menu for simple deleting of transactions 
		ContextMenu cm = new ContextMenu();
		MenuItem mi1 = new MenuItem("");
		mi1.setId("DeleteOptionButton");
		cm.getItems().add(mi1);
		mi1.setOnAction(event -> {
			Transaction actTransaction=dataTableView.getSelectionModel().getSelectedItem();
			if(actTransaction!=null) {
				//FIXME Introduces an inconsistency when an exception is thrown
				try {
					dataTableView.getItems().remove(actTransaction);
					actAccount.updateBalance();
					backgroundDatabase.addToQueue(actAccount, false);
					backgroundDatabase.addToQueue(actTransaction, true);
				} catch (Exception e) {
					updateData();
				}
			}
		});
		
		dataTableView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent t) {
		        if(t.getButton() == MouseButton.SECONDARY) {
		        	Node r=t.getPickResult().getIntersectedNode();
		        	if(null!=r && r instanceof TableCell) {
		        		TableCell<?,?> cell=(TableCell<?, ?>) r;
		        		if(cell.getTableView()!=dataTableView)
		        			return;
		        		dataTableView.getSelectionModel().clearSelection();
		        		dataTableView.getSelectionModel().select(cell.getTableRow().getIndex());
		        		Transaction actTransaction=dataTableView.getSelectionModel().getSelectedItem();
		        		mi1.setText(String.format("Buchung %d löschen",actTransaction.getNumber()));
			            cm.show(dataTableView, t.getScreenX(), t.getScreenY());
		        	}
		        }
		    }
		});
		
		if(companySelector.getItems().size()==1) {
			companySelector.setValue(companySelector.getItems().get(0));
		}
		companyChanged();
		updateAccountingLabels();
    }

	protected void setCompanies() {
		ObservableList<Company> companies = FXCollections.observableList(db.getCompanies());
		companySelector.setItems(companies);
	}

	protected void setVatTypes() {
		List<VatType> vatTypes=db.getVatTypes(false);
		vatInput.setValue(null);
		vatInput.setConverter(new VatTypeStringConverter());
		vatInput.setItems(FXCollections.observableList(vatTypes));
		for (int i = 0; i < vatTypes.size(); i++) {
			if(vatTypes.get(i).isDefaultVatType()) {
				defaultVat=vatTypes.get(i);
				break;
			}
		}
		vatInput.setValue(defaultVat);
		
		//Initialize main transaction table view and set input field formatters
		Field vatField;
		try {
			vatField = Transaction.class.getDeclaredField("vat");
		} catch (NoSuchFieldException | SecurityException e) {
			//This should only happen if someone changes Transaction.class
			throw new RuntimeException(e);
		}
		dataTableView.getColumnInternalLists().get(vatField).addAll(vatTypes);
	}

	protected void configureInputFields() {
		Field field;
		try {
			field=Transaction.class.getDeclaredField("receipts");
			receiptsInput.setTextFormatter(
					new AnnotationBasedFormatter<Transaction,Money>(field,Transaction.class,null));
			field=Transaction.class.getDeclaredField("spending");
			spendingInput.setTextFormatter(
					new AnnotationBasedFormatter<Transaction,Money>(field,Transaction.class,null));
			field=Transaction.class.getDeclaredField("accountingContraAccount");
			accountingContraAccountInput.setTextFormatter(
					new AnnotationBasedFormatter<Transaction,Integer>(field,Transaction.class,null));
			field=Transaction.class.getDeclaredField("accountingCostGroup");
			accountingCostGroupInput.setTextFormatter(
					new AnnotationBasedFormatter<Transaction,Integer>(field,Transaction.class,null));
			field=Transaction.class.getDeclaredField("accountingCostCenter");
			accountingCostCenterInput.setTextFormatter(
					new AnnotationBasedFormatter<Transaction,Integer>(field,Transaction.class,null));
			field=Transaction.class.getDeclaredField("voucher");
			voucherInput.setTextFormatter(
					new AnnotationBasedFormatter<Transaction,String>(field,Transaction.class,""));
			field=Transaction.class.getDeclaredField("inventoryNumber");
			inventoryNumberInput.setTextFormatter(
					new AnnotationBasedFormatter<Transaction,String>(field,Transaction.class,""));
			field=Transaction.class.getDeclaredField("descriptionOfTransaction");
			descriptionOfTransactionInput.setTextFormatter(
					new AnnotationBasedFormatter<Transaction,String>(field,Transaction.class,""));
		} catch (NoSuchFieldException | SecurityException e) {
			//This should only happen if someone changes Transaction.class
			throw new RuntimeException(e);
		}
		
		descriptionOfTransactionInput.setOnKeyPressed(keyPressedEvent -> {
			if(keyPressedEvent.getCode().equals(KeyCode.ENTER)) {
				boolean res=false;
				try {
					res=addRowByFields();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				if(res) {
					clearInputFields();
					receiptsInput.requestFocus();
				}
			}
		});
		clearInputFields();
	}
	
	protected void clearInputFields() {
		receiptsInput.setText(null);
		spendingInput.setText(null);
		accountingContraAccountInput.clear();
		accountingCostGroupInput.clear();
		accountingCostCenterInput.clear();
		voucherInput.clear();
		transactionDateInput.clear();
		vatInput.setValue(defaultVat);
		inventoryNumberInput.clear();
		descriptionOfTransactionInput.clear();
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
			
			String resultText=String.format("Kontostand: %s, ",strBalance);
			
			MonthYearConverter locConverter = new MonthYearConverter();
			if(actMonths==0) {
				resultText=resultText.concat("keine gebuchten Monate");
			} else if(actMonths==1) {
				String minDate=locConverter.toString(actAccountTurnovers.get(0).getMonth());
				resultText=resultText.concat(String.format("1 gebuchter Monat, %s",minDate));
			} else {
				String minDate=locConverter.toString(actAccountTurnovers.get(0).getMonth());
				String maxDate=locConverter.toString(actAccountTurnovers.get(actMonths-1).getMonth());
				resultText=resultText.concat(String.format("%d gebuchte Monate von %s bis %s",
						actMonths,minDate,maxDate));
			}
			
			balanceLabel.setText(resultText);
		}
	}
	
	@FXML
    protected boolean addRowByFields() throws IOException {
		int counter=dataTableView.getItems().size()+1;
		DefaultTableMonetaryAmountConverter moneyFormatter = new DefaultTableMonetaryAmountConverter();
		MonetaryAmount receipts=moneyFormatter.fromString(receiptsInput.getText()==null?"":receiptsInput.getText());
		MonetaryAmount spending=moneyFormatter.fromString(spendingInput.getText()==null?"":spendingInput.getText());
		
		String contraAccount=accountingContraAccountInput.getText().strip();
		String costGroup=accountingCostGroupInput.getText().strip();
		String costCenter=accountingCostCenterInput.getText().strip();
		String voucher=voucherInput.getText().strip();
		LocalDate transactionDate=transactionDateInput.getDate();
		String inventoryNumber=inventoryNumberInput.getText().strip();
		String description=descriptionOfTransactionInput.getText().strip();
		
		//Verification
		if(transactionDate==null) {
			transactionDateInput.requestFocus();
			return false;
		}
		
		
		Transaction tmp=new Transaction(counter,receipts,spending,
        		contraAccount.length()>0?Integer.parseInt(contraAccount):null,
				costGroup.length()>0?Integer.parseInt(costGroup):null,
				costCenter.length()>0?Integer.parseInt(costCenter):null,
				voucher.length()>0?voucher:null,transactionDate,vatInput.getValue(),
				inventoryNumber.length()>0?inventoryNumber:null,
				description.length()>0?description:null);
		App.logger.debug(String.format("New Transaction created with Date %s",transactionDate.toString()));
		dataTableView.getItems().add(tmp);
        persistChanges();
        return true;
    }
	
	@FXML 
	protected void companyChanged() {
		if(null==companySelector.getValue() && companySelector.getItems().size()==1) {
			companySelector.setValue(companySelector.getItems().get(0));
			return;
		}
		
		Company actCompany=companySelector.getValue();
		if(actCompany!=null && actCompany==this.actCompany) {
			return;
		} else {
			disableNoCompany();
			this.actCompany=actCompany;
		}
		
		if(actCompany!=null) {
			db.loadAccountsToCompany(actCompany);
			accountSelector.setItems(FXCollections.observableList(actCompany.getAccounts()));
			accountSelector.setDisable(false);
			if(accountSelector.getItems().size()==1) {
				accountSelector.setValue(accountSelector.getItems().get(0));
				accountChanged();
			}
		}
	}
	
	@FXML 
	protected void accountChanged() {
		Account actAccount=accountSelector.getValue();
		
		if(actAccount!=null && actAccount==this.actAccount) {
			return;
		} else {
			disableNoAccount();
			this.actAccount=actAccount;
		}

		if(actAccount!=null) {
			if(!turnoverList.containsKey(actAccount)) {
				db.loadMonthsToAccount(actAccount);
				turnoverList.put(actAccount, FXCollections.observableList(actAccount.getBalanceMonths()));
			}
			actMonthList=FXCollections.observableArrayList();
			for (MonthAccountTurnover turnover : turnoverList.get(actAccount)) {
				actMonthList.add(turnover.getMonth());
			}
			actMonthList.sort((m1,m2) -> -m1.compareTo(m2));
			
			monthSelector.setItems(actMonthList);
			monthSelector.setDisable(false);
			if(monthSelector.getItems().size()==1) {
				monthSelector.setValue(monthSelector.getItems().get(0));
				monthChanged();
			}
		}
		updateAccountingLabels();
	}
	
	@FXML 
	protected void monthChanged() {
		LocalDate actMonth=monthSelector.getValue();
		
		if(actMonth!=null) {
			Account actAccount=accountSelector.getValue();
			actTurnover=null;
			for (MonthAccountTurnover month : turnoverList.get(actAccount)) {
				if(month.isInMonth(actMonth)) {
					actTurnover=month;
					break;
				}
			}
			if(actTurnover==null) {
				actTurnover=MonthAccountTurnover.getEmptyMonthAccountTurnover(actMonth, actAccount);
				turnoverList.get(actAccount).add(actTurnover);
				actMonthList.add(actMonth);
				actMonthList.sort((m1,m2) -> -m1.compareTo(m2));				
			} else if(actTurnover.getUid()!=null){
				db.loadTransactionsToMonth(actTurnover);
			}
			
			dataTableView.getItems().removeListener(transactionUpdateListener);
			ObservableList<Transaction> transactionList=FXCollections.observableList(actTurnover.getTransactions());
			transactionList.addListener(transactionUpdateListener);
			dataTableView.setItems(transactionList);
			
			insertPane.setDisable(false);
			deleteActualMonthButton.setDisable(false);
			insertLineButton.setDisable(false);
			dataTableView.setDisable(false);
			transactionDateInput.setYear(actMonth.getYear());
			int actAccountingYear=actMonth.getYear();
			if(actMonth.getMonthValue()<7)
				actAccountingYear--;
			actAccountingYearLabel.setText(String.format("Buchungsjahr: 01.07.%d bis 30.06.%d", actAccountingYear,actAccountingYear+1));
			
		} else {
			disableNoMonth();
		}
	}
	
	@FXML 
	protected void deleteActualMonth() {
		if(null==actTurnover)
			return;
		if(actTurnover.getTransactions().size()>0) {
			//Show warning
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Bestätigung: Monat löschen");
			alert.setHeaderText(String.format("Möchten sie wirklich den Monat %s %d löschen?",
					actTurnover.getMonth().getMonth().getDisplayName(TextStyle.FULL,Locale.GERMANY),
					actTurnover.getMonth().getYear()));
			alert.setContentText(String.format("Der Monat enthält %d Transaktionen, die ebenfalls gelöscht werden.",
					actTurnover.getTransactions().size()));
			
			//This works around "`Alert` boxes are broken on Linux. #222" 
			//Reference: (https://github.com/javafxports/openjdk-jfx/issues/222)
			//           (https://bugs.openjdk.java.net/browse/JDK-8179073) (possibly related)
			if(System.getProperty("os.name").contains("Linux")) {
				alert.setResizable(true);
				alert.onShownProperty().addListener(e -> { 
				    Platform.runLater(() -> alert.setResizable(false)); 
				});
			}
			
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() != ButtonType.OK){
			    return; //Just abort
			}
		}
		
		actAccount.getBalanceMonths().remove(actTurnover);
		actAccount.updateBalance();
		
		backgroundDatabase.addToQueue(actTurnover,true);
		backgroundDatabase.addToQueue(actAccount,false);
		LocalDate monthDate=monthSelector.getValue();
		monthSelector.setValue(null);
		monthSelector.getItems().remove(monthDate);
		actTurnover=null;
		monthChanged();
	}
		
	@FXML
    protected void persistChanges() throws IOException {
		for (Account account : turnoverList.keySet()) {
			backgroundDatabase.addToQueue(account, false);
		}
		//ArrayList<Account> dataList = new ArrayList<Account>();
		//dataList.addAll(turnoverList.keySet());		
		//db.recursiveMergeData(dataList);
		//db.deleteData(deletedItems);
		
		lastUpdate=ZonedDateTime.now();
		updateAccountingLabels();
		ObservableList<MonthAccountTurnover> tmpList = turnoverList.get(actAccount);
		turnoverList.clear();
		turnoverList.put(actAccount, tmpList);
    }
	
	private void disableNoCompany() {
		actCompany=null;
		accountSelector.setValue(null);
		accountSelector.setDisable(true);
		disableNoAccount();
	}

	private void disableNoAccount() {
		actAccount=null;
		monthSelector.setValue(null);
		monthSelector.setDisable(true);
		disableNoMonth();
	}
	
	private void disableNoMonth() {
		clearInputFields();
		actTurnover=null;
		actAccountingYearLabel.setText(null);
		dataTableView.getItems().removeListener(transactionUpdateListener);
		dataTableView.setItems(FXCollections.emptyObservableList());
		insertPane.setDisable(true);
		deleteActualMonthButton.setDisable(true);
		insertLineButton.setDisable(true);
		dataTableView.setDisable(true);
	}
	
	public void updateData() {
		internalUpdateData();
		dataTableView.refresh();
		companyChanged();
	}
	
	protected void internalUpdateData() {
		Company locActCompany=actCompany;
		Account locActAccount=actAccount;
		MonthAccountTurnover locActTurnover = actTurnover;
		
		setCompanies();
		setVatTypes();
		mergeTurnoverList();
		lastUpdate=ZonedDateTime.now();
				
		if(null!=locActCompany) {
			Company tmpCompany=null;
			for(Company company:this.companySelector.getItems()){
				if(company.getUid()==locActCompany.getUid()) {
					tmpCompany=company;
					break;
				}
			}
			if(null!=tmpCompany) {
				db.loadAccountsToCompany(tmpCompany);
				if(companySelector.getValue()!=tmpCompany) {
					companySelector.setValue(tmpCompany);
					companyChanged();
				}
				if(tmpCompany.getAccounts().size()==1) {
					locActAccount=tmpCompany.getAccounts().get(0);
				}
					
				if(null!=locActAccount) {
					Account tmpAccount=null;
					for(Account account:tmpCompany.getAccounts()){
						if(account.getUid()==locActAccount.getUid()) {
							tmpAccount=account;
							break;
						}
					}
					if(null!=tmpAccount) {
						db.loadMonthsToAccount(tmpAccount);
						if(accountSelector.getValue()!=tmpAccount) {
							accountSelector.setValue(tmpAccount);
						} else {
							actAccount=null;
						}
						accountChanged();
						
						if(null!=locActTurnover) {
							MonthAccountTurnover tmpTurnover=null;
							for(MonthAccountTurnover turnover:tmpAccount.getBalanceMonths()){
								if(turnover.getUid()==locActTurnover.getUid()) {
									tmpTurnover=turnover;
									break;
								}
							}
							if(null!=tmpTurnover) {
								monthSelector.setValue(tmpTurnover.getMonth());								
							}
						}
					}
				}
			}
		}
	}

	private void mergeTurnoverList() {
		Set<Long> accountUidSet=new HashSet<Long>();
		Set<Long> companyUidSet=new HashSet<Long>();
		Set<Account> deletedAccounts=new HashSet<Account>();
		for(Entry<Account, ObservableList<MonthAccountTurnover>> act : turnoverList.entrySet()) {
			accountUidSet.add(act.getKey().getUid());
			companyUidSet.add(act.getKey().getCompany().getUid());
			deletedAccounts.add(act.getKey());
		}
		
		for(Company company:this.companySelector.getItems()){
			if(!companyUidSet.contains(company.getUid()))
				continue;
			db.loadAccountsToCompany(company);
			for(Account account:company.getAccounts()){
				if(!accountUidSet.contains(account.getUid()))
					continue;
				db.loadMonthsToAccount(account);
				Account oldAccount=null;
				for(Entry<Account, ObservableList<MonthAccountTurnover>> act : turnoverList.entrySet()) {
					if(act.getKey().getUid()==account.getUid()) {
						oldAccount=act.getKey();
						break;
					}
				}
				if(null==oldAccount)
					continue;
				mergeAccounts(oldAccount,account);
				turnoverList.remove(oldAccount);
				turnoverList.put(account, FXCollections.observableList(account.getBalanceMonths()));
				deletedAccounts.remove(oldAccount);
			}
		}
		
		for (Account account : deletedAccounts) {
			turnoverList.remove(account);			
		}
		
	}

	private void mergeAccounts(Account oldAccount, Account newAccount) {
		List<MonthAccountTurnover> newMonthList=newAccount.getBalanceMonths();
		List<MonthAccountTurnover> oldMonthList=oldAccount.getBalanceMonths();
		newMonthList.sort(null);
		oldMonthList.sort(null);
		int i,j;
		for(i=0,j=0;i<newMonthList.size() && j<oldMonthList.size();) {
			if(newMonthList.get(i).isInMonth(oldMonthList.get(j).getMonth())) {
				if (oldMonthList.get(j).isTransactionsLoaded()) {
					oldMonthList.get(j).updateBalance();
					db.loadTransactionsToMonth(newMonthList.get(i));
					for(Transaction actTransaction : oldMonthList.get(j).getTransactions()) {
						if(actTransaction.getUid()==null) {
							newMonthList.get(i).appendTranaction(actTransaction);
						} else {
							//TODO Changes on transaction field level are dropped silently by newer write operations
							//     This could be fixed by adding a field level time stamp for transaction changes, but
							//     this increases the complexity of the actual code by adding a time-stamp to any field
							//     of the data objects.
							List<Transaction> newTransactionList = newMonthList.get(i).getTransactions();
							for(int k=0;k<newTransactionList.size();++k) {
								if(newTransactionList.get(k).getUid()==actTransaction.getUid()) {
									if(actTransaction.getLastChange().isAfter(newTransactionList.get(k).getLastChange())) {
										newTransactionList.set(k,actTransaction);
									}										
									break;
								}
							}						
						}
					}
				}
				i++;j++;
			} else if (newMonthList.get(i).getMonth().isBefore(oldMonthList.get(j).getMonth())) {
				i++;
			} else {
				if(oldMonthList.get(j).getLastChange().isAfter(lastUpdate)) {
					newMonthList.add(i, oldMonthList.get(j));
					i++;j++;
				} else {
					j++;
				}
			}
		}
		for(;j<oldMonthList.size();++j) {
			newMonthList.add(oldMonthList.get(j));
		}
	}
}
