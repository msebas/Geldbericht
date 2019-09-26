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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeast;
import static org.mcservice.javafx.control.table.TestTypes.getComboPopupList;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mcservice.AfterFXInitBeforeEach;
import org.mcservice.MockedApplicationTest;
import org.mcservice.geldbericht.App;
import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.Transaction;
import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.data.converters.AccountStringConverter;
import org.mcservice.geldbericht.data.converters.CompanyStringConverter;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mcservice.javafx.BaseMatcherCallbackFilter;
import org.mcservice.javafx.control.date.MonthYearConverter;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;


@Tag("GUI")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
//@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class PdfGeneratorPaneControllerTest extends MockedApplicationTest{

	@Retention(RetentionPolicy.RUNTIME)
	private @interface CreateCompanies {
		int value() default 3;
		int accountNumber() default 3;
		int monthNumber() default 3;
		int transactionNumber() default 3;
	}
	
	@Mock
	DbAbstractionLayer db;
	
	String okText=javafx.scene.control.ButtonType.OK.getText();
	String cancelText=javafx.scene.control.ButtonType.CANCEL.getText();
	
	Scene scene;
	ComboBox<Company> companySelector;
	ComboBox<Account> accountSelector;
	ComboBox<LocalDate> monthSelector;
	Button createButton;
	
	ZonedDateTime mockListCreation=null;
	List<Company> companies=null;
	
	PdfGeneratorPaneController controller;
	
	int allMembers=0;

	static List<VatType> vats=new ArrayList<VatType>(List.of(new VatType(1L,ZonedDateTime.now(),"Full","19 %", BigDecimal.valueOf(0.19),false,false),
			new VatType(1L,ZonedDateTime.now(),"Half","12 %", BigDecimal.valueOf(0.12),true,false)));
		
	@Override 
	public void start(Stage stage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("PdfGeneratorPane.fxml"));
		fxmlLoader.setControllerFactory(new ControllerFactory(db));
		
		scene = new Scene(fxmlLoader.load());
		controller=fxmlLoader.getController();
		stage.setScene(scene);
        stage.show();
    }

	public void createTransactions(int n,MonthAccountTurnover month){
		List<Transaction> result = new ArrayList<Transaction>(n);
		for (int j = 0; j < n; j++) {
			result.add(new Transaction(Long.valueOf(allMembers),ZonedDateTime.now(),j,
					Money.of((j%3==1?1:0)*(j-4)*(j-4), "EUR"),Money.of((j%3==1?0:1)*(j-3)*(j-3), "EUR"),
					55555+j,(22+j)%100,(333+j)%1000, String.format("Voucher %d",j), month.getMonth().plusDays(j%28), 
					vats.get(j%2), Long.valueOf(1000+allMembers).toString(), 
					String.format("Description %d",allMembers)));
			allMembers++;
		}
		Collections.shuffle(result,new Random(12));
		month.getTransactions().addAll(result);
	}
	
	public void createMonths(int n, int m,Account account){
		List<MonthAccountTurnover> result = new ArrayList<MonthAccountTurnover>(n);
		for (int j = 0; j < n; j++) {
			result.add(new MonthAccountTurnover(Long.valueOf(allMembers),ZonedDateTime.now(),
					new ArrayList<Transaction>(),
					LocalDate.of(2019,8,1).minusMonths(j), account, 
					Money.of(BigDecimal.ZERO,"EUR"), Money.of(BigDecimal.ZERO,"EUR"), 
					Money.of(BigDecimal.ZERO,"EUR"), Money.of(BigDecimal.ZERO,"EUR"), 
					Money.of(BigDecimal.ZERO,"EUR"), Money.of(BigDecimal.ZERO,"EUR")));
			allMembers++;
			createTransactions(m,result.get(result.size()-1));
		}
		Collections.shuffle(result,new Random(12));
		account.getBalanceMonths().addAll(result);
	}
	
	public List<Account> createAccounts(int n,int m, int o,Company company){
		List<Account> result = new ArrayList<Account>(n);
		if(n==0)
			return result;
		for (int j = 0; j < n; j++) {
			result.add(new Account((long) allMembers,ZonedDateTime.now(),String.format("%05d",allMembers),
								String.format("Account %5d",allMembers),
								Money.of(10*allMembers,"EUR"), company));
			createMonths(m,o,result.get(result.size()-1));
			allMembers++;
		}
		return result;
	}
	
	public List<Company> createCompanies(int n,int m, int o,int p){
		List<Company> result = new ArrayList<Company>(n);
		if(n==0)
			return result;
		for (int i=0;i<n;++i) {
			Company tmp=new Company((long) allMembers,mockListCreation,null,String.format("Company Name %d",i),
					String.format("%05d",i),
					String.format("%010d",i));
			tmp.setAccounts(createAccounts(m,o,p, tmp));
			result.add(tmp);
			allMembers++;
		}
		return result;
	}
		
	@BeforeEach 
	public void initMock(TestInfo testInfo) {
		ArrayList<Company> act=new ArrayList<Company>();
		mockListCreation=ZonedDateTime.now();
		if (testInfo.getTestMethod().get()!=null) {
			Method actTest=testInfo.getTestMethod().get();
			CreateCompanies annotation=actTest.getAnnotation(CreateCompanies.class);
			if (annotation!=null) {
				act.addAll(createCompanies(annotation.value(), annotation.accountNumber(),annotation.monthNumber(),
						annotation.transactionNumber()));
			}
		}
		mockListCreation=ZonedDateTime.now();
		when(db.getCompanies()).thenReturn(act);
		companies=act;
	}
    
    @SuppressWarnings("unchecked")
	@AfterFXInitBeforeEach
    public void setArgs() {
    	createButton = (Button) (lookup("#createButton").query());
    	companySelector = (ComboBox<Company>) (lookup("#companySelector").query());
    	accountSelector = (ComboBox<Account>) (lookup("#accountSelector").query());
    	monthSelector = (ComboBox<LocalDate>) (lookup("#monthSelector").query());
    }
        
    @Tag("Active")
    @Test
    @Disabled
    @CreateCompanies
    public void manual() {
    	sleep(3600000);
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1)
    public void CheckInitialize() throws Exception {
    	assertTrue(createButton.isDisabled());
    	
    	assertTrue(companySelector.getConverter() instanceof CompanyStringConverter);
    	assertTrue(accountSelector.getConverter() instanceof AccountStringConverter);
    	assertTrue(monthSelector.getConverter() instanceof MonthYearConverter);
    	assertTrue(monthSelector.isEditable());
    	assertTrue(monthSelector.getEditor().getTextFormatter().getFilter() instanceof BaseMatcherCallbackFilter);
    	assertEquals("MM.YY (1950-2049)",monthSelector.getPromptText());
    	
    	clickOn(monthSelector).write("ergo12");
    	assertEquals("12.",monthSelector.getEditor().getText());
    	assertTrue(monthSelector.getEditor().getStyleClass().contains("field-validation-error"));
    	write("3");
    	assertEquals("12.3",monthSelector.getEditor().getText());
    	assertFalse(monthSelector.getEditor().getStyleClass().contains("field-validation-error"));
    	
    	
    }

    @Test
    @CreateCompanies(value=1,accountNumber=1,monthNumber=1)
    public void CheckCompanyChangedNoSelection() {
    	companies.add(null);
    	clickOn(companySelector).type(KeyCode.DOWN);
    	type(KeyCode.ENTER);
    	assertNull(companySelector.getValue());
    	assertNull(accountSelector.getValue());
    	assertNull(monthSelector.getValue());
    	assertTrue(monthSelector.getEditor().getText()==null ||
    			monthSelector.getEditor().getText().equals(""));
    	assertTrue(accountSelector.isDisabled());
    	assertTrue(monthSelector.isDisabled());
    	assertTrue(createButton.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=2,accountNumber = 2)
    public void CheckCompanyChangedSelection() {
    	verify(db,times(0)).loadAccountsToCompany(companies.get(0));
    	
    	clickOn(companySelector).type(KeyCode.DOWN).type(KeyCode.ENTER);
    	verify(db,atLeast(1)).loadAccountsToCompany(companies.get(0));
    	assertEquals(companies.get(0),companySelector.getValue());
    	assertEquals(companies.get(0).getAccounts(),accountSelector.getItems());
    	assertNull(accountSelector.getValue());
    	assertTrue(monthSelector.isDisabled());
    	assertTrue(createButton.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 2)
    public void CheckAutofillCompany() {
    	assertEquals(companies.get(0),companySelector.getValue());
    	assertEquals(companies.get(0).getAccounts(),accountSelector.getItems());
    	assertNull(accountSelector.getValue());
    	assertTrue(monthSelector.isDisabled());
    	assertTrue(createButton.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1, monthNumber=0)
    public void CheckInsertNonExistentMonth() {
    	type(KeyCode.TAB,2).write("234").type(KeyCode.ENTER);
    	assertNull(monthSelector.getValue());
    	assertTrue(createButton.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=2,accountNumber = 1, monthNumber = 2)
    public void CheckAutofillAccount() {
    	clickOn(companySelector).clickOn(getComboPopupList(this).get(0));
    	assertEquals(companies.get(0).getAccounts(),accountSelector.getItems());
    	assertEquals(companies.get(0).getAccounts().get(0),accountSelector.getValue());
    	assertNull(monthSelector.getValue());
    	assertTrue(createButton.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=2,accountNumber = 2,monthNumber = 1)
    public void CheckAutofillMonth() {
    	clickOn(companySelector).clickOn(getComboPopupList(this).get(0));
    	clickOn(accountSelector).clickOn(getComboPopupList(this).get(0));
    	assertEquals(1,monthSelector.getItems().size());
    	assertEquals(accountSelector.getItems().get(0).getBalanceMonths().get(0).getMonth(),monthSelector.getValue());
    	assertFalse(createButton.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1)
    public void CheckUpdateDataAllFine() throws Exception{
    	Field field=PdfGeneratorPaneController.class.getDeclaredField("actBalanceMonth");
    	field.setAccessible(true);
    	ArrayList<Company> locCompanies = new ArrayList<Company>(List.of(new Company(companies.get(0))));
    	
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	
    	type(KeyCode.F5);
    	assertFalse(companies.get(0)==companySelector.getValue());
    	assertFalse(companies.get(0).getAccounts().get(0)==accountSelector.getValue());
    	assertFalse(companies.get(0).getAccounts().get(0).getBalanceMonths().get(0)==field.get(controller));
    	assertTrue(companies.get(0).equals(companySelector.getValue(),true));
    	assertEquals(companies.get(0).getAccounts().get(0),accountSelector.getValue());
    	assertEquals(companies.get(0).getAccounts().get(0).getBalanceMonths().get(0).getMonth(),monthSelector.getValue());
    	assertEquals(companies.get(0).getAccounts().get(0).getBalanceMonths().get(0),field.get(controller));
    	field.setAccessible(false);
    }

    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1)
    public void CheckUpdateDataCompanyDeleted() throws Exception{
    	Field field=PdfGeneratorPaneController.class.getDeclaredField("actBalanceMonth");
    	field.setAccessible(true);
    	List<Company> locCompanies = createCompanies(2, 2, 2, 2);
    	    	
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	
    	type(KeyCode.F5);
    	assertTrue(accountSelector.isDisabled());
    	assertNull(companySelector.getValue());
    	assertTrue(monthSelector.isDisabled());
    	assertTrue(createButton.isDisabled());
    	field.setAccessible(false);
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1)
    public void CheckUpdateDataCompanyDeletedAutoSelect() throws Exception{
    	List<Company> locCompanies = createCompanies(1, 2, 2, 2);
    	    	
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	
    	type(KeyCode.F5);
    	assertNotNull(companySelector.getValue());
    	assertFalse(accountSelector.isDisabled());
    	assertTrue(monthSelector.isDisabled());
    	assertTrue(createButton.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1)
    public void CheckUpdateDataAccountDeleted() throws Exception{
    	ArrayList<Company> locCompanies = new ArrayList<Company>(List.of(new Company(companies.get(0))));
    	locCompanies.get(0).getAccounts().clear();
    	locCompanies.get(0).getAccounts().addAll(createAccounts(2,2,2, locCompanies.get(0)));
    	
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	
    	type(KeyCode.F5);
    	assertNotNull(companySelector.getValue());
    	assertNull(accountSelector.getValue());
    	assertTrue(monthSelector.isDisabled());
    	assertTrue(createButton.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1)
    public void CheckUpdateDataAccountDeletedAutoselect() throws Exception{
    	ArrayList<Company> locCompanies = new ArrayList<Company>(List.of(new Company(companies.get(0))));
    	locCompanies.get(0).getAccounts().clear();
    	locCompanies.get(0).getAccounts().addAll(createAccounts(1,2,2, locCompanies.get(0)));
    	
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	
    	type(KeyCode.F5);
    	assertNotNull(companySelector.getValue());
    	assertNotNull(accountSelector.getValue());
    	assertNull(monthSelector.getValue());
    	assertFalse(monthSelector.isDisabled());
    	assertTrue(createButton.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1)
    public void CheckUpdateDataMonthDeleted() throws Exception{
    	ArrayList<Company> locCompanies = new ArrayList<Company>(List.of(new Company(companies.get(0))));
    	locCompanies.get(0).getAccounts().get(0).getBalanceMonths().clear();
    	
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	
    	type(KeyCode.F5);
    	assertNotNull(companySelector.getValue());
    	assertNotNull(accountSelector.getValue());
    	assertNull(monthSelector.getValue());
    	assertTrue(createButton.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1)
    public void CheckUpdateDataMonthDeletedAutoselect() throws Exception{
    	ArrayList<Company> locCompanies = new ArrayList<Company>(List.of(new Company(companies.get(0))));
    	locCompanies.get(0).getAccounts().get(0).getBalanceMonths().clear();
    	createMonths(1,2, locCompanies.get(0).getAccounts().get(0));
    	
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	
    	type(KeyCode.F5);
    	assertNotNull(companySelector.getValue());
    	assertNotNull(accountSelector.getValue());
    	assertNotNull(monthSelector.getValue());
    	assertFalse(createButton.isDisabled());
    }
    
}
