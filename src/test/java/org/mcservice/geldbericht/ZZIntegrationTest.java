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

import static org.junit.jupiter.api.Assertions.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.mockito.quality.Strictness;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import org.mcservice.AfterFXInitBeforeEach;
import org.mcservice.MockedApplicationTest;
import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.Transaction;
import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mcservice.javafx.control.date.DayMonthField;
import org.mcservice.javafx.control.table.ReflectionTableView;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.apache.log4j.Logger;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

//@Tag("Active")
@Tag("DB")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class ZZIntegrationTest extends MockedApplicationTest{
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface CreateDatabase {
		int vats() default 3;
		int companies() default 3;
		int accountNumber() default 3;
		int monthNumber() default 3;
		int transactionNumber() default 3;
	}
	
	TreeMap<String,String> sysenv=new TreeMap<String,String>(System.getenv());
	
	DbAbstractionLayer db;
	DbAbstractionLayer dbspy;
	String okText=javafx.scene.control.ButtonType.OK.getText();
	String cancelText=javafx.scene.control.ButtonType.CANCEL.getText();
	
	Scene scene;
	Node menuManager;
	TextField receiptsInput;	
	TextField spendingInput;	
	TextField accountingContraAccountInput;	
	TextField accountingCostGroupInput;	
	TextField accountingCostCenterInput;	
	TextField voucherInput;	
	DayMonthField transactionDateInput;	
	ComboBox<VatType> vatInput;	
	TextField inventoryNumberInput;	
	TextField descriptionOfTransactionInput;	
	ReflectionTableView<Transaction> dataTableView;	 
	Button saveChangesButton;	 
	Button deleteActualMonthButton; 
	Button insertLineButton;	
	ComboBox<Company> companySelector;
	ComboBox<Account> accountSelector;
	ComboBox<LocalDate> monthSelector;
	Label balanceLabel;
	Label actAccountingYearLabel;
	
	PrimaryController controller;
	ZonedDateTime mockListCreation=null;
	
	int allMembers=0;
	
	
	@Override 
	public void start(Stage stage) throws Exception {
		dbspy=Mockito.spy(db);
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));
		fxmlLoader.setControllerFactory(new ControllerFactory(dbspy,null));
		
		App.logger=Logger.getLogger(App.class);
		
		scene = new Scene(fxmlLoader.load());
		controller=fxmlLoader.getController();
		stage.setScene(scene);
        stage.show();
    }
	

	private void createVats(int vats) {
		for (int j = 0; j < vats; j++) {
			VatType t=new VatType(null,ZonedDateTime.now(), String.format("Vat %d",j), String.format("%d ",j)+"%", 
					BigDecimal.valueOf(0.01*j), j==0, false);
			t=db.persistVatType(t);
			allMembers++;
		}
	}
	
	public void createTransactions(int n,int vatNr,MonthAccountTurnover month){
		List<Transaction> result = new ArrayList<Transaction>(n);
		for (int j = 0; j < n; j++) {
			Transaction t=new Transaction(null,ZonedDateTime.now(),j,
					Money.of((j%3==1?1:0)*(j-4)*(j-4), "EUR"),Money.of((j%3==1?0:1)*(j-3)*(j-3), "EUR"),
					55555+j,(22+j)%100,(333+j)%1000, String.format("Voucher %d",j), month.getMonth().plusDays(j%28), 
					db.getVatTypes().get(j%vatNr), Long.valueOf(1000+allMembers).toString(), 
					String.format("Description %d",allMembers));
			t=db.persistTransaction(t);
			result.add(t);
			allMembers++;
		}
		Collections.shuffle(result,new Random(12));
		month.getTransactions().addAll(result);
		month.updateBalance();
		for(Transaction t:month.getTransactions()) {
			db.updateTransaction(t);
		}
	}
	
	public void createMonths(int n, int m,int vatNr, Account account){
		List<MonthAccountTurnover> result = new ArrayList<MonthAccountTurnover>(n);
		for (int j = 0; j < n; j++) {
			MonthAccountTurnover t = new MonthAccountTurnover(null,ZonedDateTime.now(),
					new ArrayList<Transaction>(),
					LocalDate.of(2019,8,1).minusMonths(j), account, 
					Money.of(BigDecimal.ZERO,"EUR"), Money.of(BigDecimal.ZERO,"EUR"), 
					Money.of(BigDecimal.ZERO,"EUR"), Money.of(BigDecimal.ZERO,"EUR"), 
					Money.of(BigDecimal.ZERO,"EUR"), Money.of(BigDecimal.ZERO,"EUR"));
			createTransactions(m,vatNr,t);
			t=db.persistMonthAccountTurnover(t);
			result.add(t);
			allMembers++;
		}
		Collections.shuffle(result,new Random(12));
		account.getBalanceMonths().addAll(result);
		account.updateBalance();
		for(MonthAccountTurnover t:account.getBalanceMonths()) {
			db.updateMonthAccountTurnover(t);
		}
	}
	
	public List<Account> createAccounts(int n,int m, int o, int vatNr, Company company){
		List<Account> result = new ArrayList<Account>(n);
		if(n==0)
			return result;
		for (int j = 0; j < n; j++) {
			Account a=new Account((long) allMembers,ZonedDateTime.now(),String.format("%05d",allMembers),
					String.format("Account %5d",allMembers),Money.of(10*allMembers,"EUR"), company);
			a=db.persistAccount(a);
			createMonths(m,o,vatNr,a);
			a=db.updateAccount(a);
			result.add(a);			
			allMembers++;
		}
		return result;
	}
	
	public List<Company> createCompanies(int n,int m, int o,int p,int vatNr){
		List<Company> result = new ArrayList<Company>(n);
		if(n==0)
			return result;
		for (int i=0;i<n;++i) {
			Company tmp=new Company((long) allMembers,mockListCreation,null,String.format("Company Name %d",i),
					String.format("%05d",i),
					String.format("%010d",i));
			db.persistCompany(tmp);
			tmp.setAccounts(createAccounts(m,o,p,vatNr, tmp));
			tmp=db.updateCompany(tmp);
			result.add(tmp);
			allMembers++;
		}
		return result;
	}
		
	@BeforeEach 
	public void initMock(TestInfo testInfo) throws Exception {
		URL data=this.getClass().getClassLoader().getResource("connection.cfg.xml");
    	Map<String,String> newenv=new TreeMap<String,String>(sysenv);
    	newenv.put("GELDBERICHT_CONFIGFILE",data.getPath());
    	System.setProperty("GELDBERICHT_LOGFILE", "geldbericht.log");
    	MockedApplicationTest.setEnv(newenv);
    	db=new DbAbstractionLayer();		
		
		mockListCreation=ZonedDateTime.now();
		if (testInfo.getTestMethod().get()!=null) {
			Method actTest=testInfo.getTestMethod().get();
			CreateDatabase annotation=actTest.getAnnotation(CreateDatabase.class);
			if (annotation!=null) {
				createVats(annotation.vats());
				createCompanies(annotation.companies(), annotation.accountNumber(),annotation.monthNumber(),
						annotation.vats(),annotation.transactionNumber());
			}
		}
	}
	

	@AfterFXInitBeforeEach
    public void setArgs() {
		//Getting it via lookup does not work, because sometimes lookup does only 
		//return equals, not same objects.
		TransactionInputPaneController inp=controller.transactionTabController;
		
		receiptsInput=inp.receiptsInput;
        spendingInput = inp.spendingInput;
        accountingContraAccountInput = inp.accountingContraAccountInput;
        accountingCostGroupInput = inp.accountingCostGroupInput;
        accountingCostCenterInput = inp.accountingCostCenterInput;
        voucherInput = inp.voucherInput;
        transactionDateInput = inp.transactionDateInput;
        vatInput = inp.vatInput;
        inventoryNumberInput = inp.inventoryNumberInput;
        descriptionOfTransactionInput = inp.descriptionOfTransactionInput;
        dataTableView = inp.dataTableView;
        saveChangesButton = inp.saveChangesButton;
        deleteActualMonthButton = inp.deleteActualMonthButton;
        insertLineButton = inp.insertLineButton;
        companySelector = inp.companySelector;
        accountSelector = inp.accountSelector;
        monthSelector = inp.monthSelector;
        balanceLabel = inp.balanceLabel;
        actAccountingYearLabel = inp.actAccountingYearLabel;
    	menuManager = (Node) (lookup("#menuManager").query());
    }
	
	//Shortcut
	public TableCell<?, ?> getCell(TableView<?> view,int col, int row) {
        return org.mcservice.javafx.control.table.TestTypes.getCell(view,col,row);
    }
	
	@Tag("Active")
    @Test
    @Disabled
    //@CreateDatabase
    public void manual() {
    	sleep(3600000);
    }
	
	@Tag("Active")
    @Test
    public void CreateAnythingFromScratch1C1A() {
		create1C1A2V();

		//Create month 04.72
    	clickOn(monthSelector).write("472").type(KeyCode.ENTER);
    	assertEquals(1,accountSelector.getValue().getBalanceMonths().size());
    	assertNull(accountSelector.getValue().getBalanceMonths().get(0).getUid());
    	
    	clickOn(transactionDateInput).write("54").type(KeyCode.TAB,3).type(KeyCode.ENTER);
    	while(accountSelector.getValue().getBalanceMonths().get(0).getUid()==null);
    	assertEquals(1,accountSelector.getValue().getBalanceMonths().get(0).getTransactions().size());
    	
    	write("7").type(KeyCode.TAB,6).write("64").type(KeyCode.TAB,3).type(KeyCode.ENTER);
    	while(accountSelector.getValue().getBalanceMonths().get(0).getTransactions().get(1).getUid()==null);
    	
    	assertEquals(Money.of(130, "EUR"),db.getAccounts().get(0).getBalance());
    	
    	type(KeyCode.TAB).write("69").type(KeyCode.TAB,5).write("74").type(KeyCode.TAB,3).type(KeyCode.ENTER);
    	while(accountSelector.getValue().getBalanceMonths().get(0).getTransactions().get(2).getUid()==null);
    	
    	assertEquals(Money.of(61, "EUR"),db.getAccounts().get(0).getBalance());
    	
    	type(KeyCode.TAB).write("69").type(KeyCode.TAB,6).type(KeyCode.DOWN).type(KeyCode.TAB,2).type(KeyCode.ENTER);
    	write("84").type(KeyCode.TAB,3).type(KeyCode.ENTER);
    	while(accountSelector.getValue().getBalanceMonths().get(0).getTransactions().get(3).getUid()==null);
    	
    	assertEquals(Money.of(-8, "EUR"),db.getAccounts().get(0).getBalance());
    	
    	//Create month 03.72
    	clickOn(monthSelector).type(KeyCode.BACK_SPACE,5).write("372").type(KeyCode.ENTER);
    	while(!receiptsInput.isFocused()) {
    		type(KeyCode.TAB);
    	}
    	
    	write("9").type(KeyCode.TAB,6).write("63").type(KeyCode.TAB,3).type(KeyCode.ENTER);
    	while(accountSelector.getValue().getBalanceMonths().get(0).getTransactions().get(0).getUid()==null);
    	
    	Account account = db.getAccounts().get(0);
    	db.loadMonthsToAccount(account);
    	assertEquals(Money.of(1, "EUR"),account.getBalance());
    	assertEquals(Money.of(123, "EUR"),account.getBalanceMonths().get(0).getInitialAssets());
    	assertEquals(Money.of(132, "EUR"),account.getBalanceMonths().get(1).getInitialAssets());
    	
    	//Create month 05.72
    	clickOn(monthSelector).type(KeyCode.BACK_SPACE,5).write("572").type(KeyCode.ENTER);
    	while(!receiptsInput.isFocused()) {
    		type(KeyCode.TAB);
    	}
    	
    	write("69").type(KeyCode.TAB,6).write("65").type(KeyCode.TAB,3).type(KeyCode.ENTER);
    	while(accountSelector.getValue().getBalanceMonths().get(2).getTransactions().get(0).getUid()==null);
    	
    	type(KeyCode.TAB).write("9").type(KeyCode.TAB,5).write("75").type(KeyCode.TAB,3).type(KeyCode.ENTER);
    	while(accountSelector.getValue().getBalanceMonths().get(2).getTransactions().get(1).getUid()==null);
    	
    	assertEquals(Money.of(61, "EUR"),db.getAccounts().get(0).getBalance());
    	
    	type(KeyCode.TAB).write("69").type(KeyCode.TAB,6).type(KeyCode.DOWN).type(KeyCode.TAB,2).type(KeyCode.ENTER);
    	write("85").type(KeyCode.TAB,3).type(KeyCode.ENTER);
    	while(accountSelector.getValue().getBalanceMonths().get(2).getTransactions().get(2).getUid()==null);
    	
    	assertEquals(Money.of(-8, "EUR"),db.getAccounts().get(0).getBalance());
    	
    	while(!descriptionOfTransactionInput.isFocused()) {
    		type(KeyCode.TAB);
    	}
    	
    	type(KeyCode.ENTER).write("85").type(KeyCode.TAB,3).type(KeyCode.ENTER);
    	doubleClickOn(getCell(dataTableView,2,3)).type(KeyCode.NUMPAD7,2).type(KeyCode.ENTER);
    	
    	assertEquals(Money.of(-8, "EUR"),db.getAccounts().get(0).getBalance());
    	assertEquals(Money.of(-85, "EUR"),accountSelector.getValue().getBalance());
    	
    	clickOn(getCell(dataTableView,0,2),MouseButton.SECONDARY);
    	clickOn((Node) lookup("#DeleteOptionButton").query());
    	
    	assertEquals(Money.of(-16, "EUR"),db.getAccounts().get(0).getBalance());
    	assertEquals(Money.of(-16, "EUR"),accountSelector.getValue().getBalance());
    }


	protected void create1C1A2V() {
		clickOn(menuManager);
    	clickOn((Node) lookup("#menuCompanyManager").query());
    	
    	clickOn((Node) lookup("#addButton").query());
    	TableView<?> table =(TableView<?>) lookup("#companyTableView").query();
    	clickOn(getCell(table,0,0));
    	type(KeyCode.C).write("ompany Name").type(KeyCode.ENTER);
    	type(KeyCode.NUMPAD0,5).type(KeyCode.ENTER);
    	type(KeyCode.NUMPAD1,10).type(KeyCode.ENTER);
    	    	
    	clickOn((Node) lookup("#persistButton").query());
    	
    	List<Company> companies=db.getCompanies();
    	assertTrue(companies.size()==1);
    	assertNotNull(companies.get(0).getUid());
    	assertEquals("company Name",companies.get(0).getCompanyName());
    	assertEquals("00000",companies.get(0).getCompanyNumber());
    	assertEquals("1111111111",companies.get(0).getCompanyBookkeepingAppointment());
    	assertEquals(companies.get(0).getUid(),companySelector.getValue().getUid());
		
    	clickOn(menuManager);
    	clickOn((Node) lookup("#menuAccountManager").query());
    	@SuppressWarnings("unchecked")
		Company selected=((ComboBox<Company>) lookup("#companySelector").query()).getValue();
    	assertEquals(companies.get(0).getUid(),selected.getUid());
    	
    	clickOn((Node) lookup("#addButton").query());
    	table =(TableView<?>) lookup("#accountTableView").query();
    	clickOn(getCell(table,0,0));
    	type(KeyCode.NUMPAD2,5).type(KeyCode.ENTER);
    	type(KeyCode.A).write("ccount Name").type(KeyCode.ENTER).type(KeyCode.RIGHT);
    	type(KeyCode.NUMPAD1).write("23").type(KeyCode.ENTER);
    	    	
    	clickOn((Node) lookup("#persistButton").query());
    	
    	List<Account> accounts=db.getAccounts();
    	companies=db.getCompanies();
    	db.loadAccountsToCompany(companies.get(0));
    	assertEquals(1,companies.get(0).getAccounts().size());
    	assertEquals(1,accounts.size());
    	assertEquals(accounts.get(0),companies.get(0).getAccounts().get(0));
    	assertNotNull(accounts.get(0).getUid());
    	assertEquals("account Name",accounts.get(0).getAccountName());
    	assertEquals("22222",accounts.get(0).getAccountNumber());
    	assertEquals(Money.of(123, "EUR"),accounts.get(0).getBalance());
    	assertEquals(Money.of(123, "EUR"),accounts.get(0).getInitialBalance());
    	assertEquals(companies.get(0).getUid(),companySelector.getValue().getUid());
    	assertEquals(accounts.get(0).getUid(),accountSelector.getValue().getUid());
    	
    	clickOn(menuManager);
    	clickOn((Node) lookup("#menuVatTypeManager").query());
    	
    	clickOn((Node) lookup("#addButton").query());
    	table =(TableView<?>) lookup("#vatTypeTableView").query();
    	clickOn(getCell(table,0,0));
    	type(KeyCode.F).type(KeyCode.ENTER);
    	type(KeyCode.F).write("ull").type(KeyCode.ENTER);
    	type(KeyCode.NUMPAD0).write(",12").type(KeyCode.ENTER);
    	clickOn((Node) lookup("#addButton").query());
    	clickOn(getCell(table,0,1));
    	type(KeyCode.H).type(KeyCode.ENTER);
    	type(KeyCode.H).write("alf").type(KeyCode.ENTER);
    	type(KeyCode.NUMPAD6).write(" %").type(KeyCode.ENTER);
    	    	
    	clickOn((Node) lookup("#persistButton").query());
    	
    	List<VatType> vatTypes=db.getVatTypes();
    	assertEquals(2,vatTypes.size());
    	assertNotNull(vatTypes.get(0).getUid());
    	assertEquals("full",vatTypes.get(0).getName());
    	assertEquals("f",vatTypes.get(0).getShortName());
    	assertEquals(0,vatTypes.get(0).getValue().compareTo(BigDecimal.valueOf(0.12)));
    	assertEquals(true,vatTypes.get(0).isDefaultVatType());
    	assertNotNull(vatTypes.get(1).getUid());
    	assertEquals("half",vatTypes.get(1).getName());
    	assertEquals("h",vatTypes.get(1).getShortName());
    	assertEquals(0,vatTypes.get(1).getValue().compareTo(BigDecimal.valueOf(0.06)));
    	assertEquals(false,vatTypes.get(1).isDefaultVatType());
    	assertEquals(companies.get(0).getUid(),companySelector.getValue().getUid());
    	assertEquals(accounts.get(0).getUid(),accountSelector.getValue().getUid());
	}
	
	
}
