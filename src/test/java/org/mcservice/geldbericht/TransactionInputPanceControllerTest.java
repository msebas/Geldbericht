package org.mcservice.geldbericht;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mcservice.javafx.control.table.TestTypes.getComboPopupList;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mcservice.AfterFXInitBeforeEach;
import org.mcservice.MockedApplicationTest;
import org.mcservice.geldbericht.App;
import org.mcservice.geldbericht.data.AbstractDataObject;
import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.Transaction;
import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.data.converters.AccountStringConverter;
import org.mcservice.geldbericht.data.converters.CompanyStringConverter;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mcservice.javafx.BaseMatcherCallbackFilter;
import org.mcservice.javafx.control.date.DayMonthField;
import org.mcservice.javafx.control.date.MonthYearConverter;
import org.mcservice.javafx.control.table.ItemUpdateListener;
import org.mcservice.javafx.control.table.MemberVariable;
import org.mcservice.javafx.control.table.ReflectionTableView;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

@Tag("GUI")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
//@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class TransactionInputPanceControllerTest extends MockedApplicationTest{

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
	GridPane insertPane;
	HBox savePane;	
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
	Button revertChangesButton;	
	Button deleteActualMonthButton; 
	Button insertLineButton;	
	ComboBox<Company> companySelector;
	ComboBox<Account> accountSelector;
	ComboBox<LocalDate> monthSelector;
	Label balanceLabel;
	
	ZonedDateTime mockListCreation=null;
	List<Company> companies=null;
	
	TransactionInputPaneController controller;
	
	
	int allMembers=0;

	private Label actAccountingYearLabel;
	static List<VatType> vats=new ArrayList<VatType>(List.of(new VatType(1L,ZonedDateTime.now(),"Full","19 %", BigDecimal.valueOf(0.19),false,false),
			new VatType(1L,ZonedDateTime.now(),"Half","12 %", BigDecimal.valueOf(0.12),true,false)));
		
	@Override 
	public void start(Stage stage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("TransactionInputPane.fxml"));
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
		when(db.getVatTypes(false)).thenReturn(vats);
		companies=act;
	}
    
    @SuppressWarnings("unchecked")
	@AfterFXInitBeforeEach
    public void setArgs() {
    	insertPane = (GridPane) (lookup("#insertPane").query());
    	savePane = (HBox) (lookup("#savePane").query());
    	receiptsInput = (TextField) (lookup("#receiptsInput").query());
    	spendingInput = (TextField) (lookup("#spendingInput").query());
    	accountingContraAccountInput = (TextField) (lookup("#accountingContraAccountInput").query());
    	accountingCostGroupInput = (TextField) (lookup("#accountingCostGroupInput").query());
    	accountingCostCenterInput = (TextField) (lookup("#accountingCostCenterInput").query());
    	voucherInput = (TextField) (lookup("#voucherInput").query());
    	transactionDateInput = (DayMonthField) (lookup("#transactionDateInput").query());
    	vatInput = (ComboBox<VatType>) (lookup("#vatInput").query());
    	inventoryNumberInput = (TextField) (lookup("#inventoryNumberInput").query());
    	descriptionOfTransactionInput = (TextField) (lookup("#descriptionOfTransactionInput").query());
    	dataTableView = (ReflectionTableView<Transaction>) (lookup("#dataTableView").query());
    	saveChangesButton = (Button) (lookup("#saveChangesButton").query());
    	revertChangesButton = (Button) (lookup("#revertChangesButton").query());
    	deleteActualMonthButton = (Button) (lookup("#deleteActualMonthButton").query());
    	insertLineButton = (Button) (lookup("#insertLineButton").query());
    	companySelector = (ComboBox<Company>) (lookup("#companySelector").query());
    	accountSelector = (ComboBox<Account>) (lookup("#accountSelector").query());
    	monthSelector = (ComboBox<LocalDate>) (lookup("#monthSelector").query());
    	balanceLabel = (Label) (lookup("#balanceLabel").query());
    	actAccountingYearLabel = (Label) (lookup("#actAccountingYearLabel").query());
    }
    
    public TableCell<Transaction, ?> getCell(int col, int row) {
        return org.mcservice.javafx.control.table.TestTypes.getCell(dataTableView,col,row);
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
    	assertTrue(vatInput.getItems().containsAll(vats));
    	assertEquals(vats.size(),vatInput.getItems().size());
    	assertEquals(vats.get(1),vatInput.getValue());
    	
    	assertTrue(dataTableView.getColumnInternalLists().
    			get(Transaction.class.getDeclaredField("vat")).containsAll(vats));
    	assertEquals(vats.size(),dataTableView.getColumnInternalLists().
    			get(Transaction.class.getDeclaredField("vat")).size());
    	
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
    	
    	ItemUpdateListener accountUpdateListener;
    	Field field = TransactionInputPaneController.class.getDeclaredField("accountUpdateListener");
    	field.setAccessible(true);
    	accountUpdateListener=(ItemUpdateListener) field.get(controller);
    	field.setAccessible(false);
    	
    	field=MemberVariable.class.getDeclaredField("listeners");
		field.setAccessible(true);
    	for(int i=0;i<dataTableView.getColumns().size();++i) {
    		EventHandler<?> eventHandler = dataTableView.getColumns().get(i).getOnEditCommit();
    		if(eventHandler instanceof MemberVariable) {
	    		@SuppressWarnings("unchecked")
				MemberVariable<Transaction, ?> memberVaraiable=(MemberVariable<Transaction, ?>) eventHandler;
	    		if(i==1 || i==2)
	    			assertTrue(((List<?>) field.get(memberVaraiable)).contains(accountUpdateListener));
	    		else
	    			assertFalse(((List<?>) field.get(memberVaraiable)).contains(accountUpdateListener));
	        	
    		} else {
    			assertFalse(dataTableView.getColumns().get(i).isEditable());
    		}
    	}
    	field.setAccessible(false);
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
    	assertTrue(dataTableView.getItems().isEmpty());
    	assertTrue(accountSelector.isDisabled());
    	assertTrue(monthSelector.isDisabled());
    	assertTrue(deleteActualMonthButton.isDisabled());
    	assertTrue(insertLineButton.isDisabled());
    	assertTrue(insertPane.isDisabled());
    	assertTrue(dataTableView.isDisabled());    	
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
    	assertTrue(deleteActualMonthButton.isDisabled());
    	assertTrue(insertLineButton.isDisabled());
    	assertTrue(insertPane.isDisabled());
    	assertTrue(dataTableView.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 2)
    public void CheckAutofillCompany() {
    	assertEquals(companies.get(0),companySelector.getValue());
    	assertEquals(companies.get(0).getAccounts(),accountSelector.getItems());
    	assertNull(accountSelector.getValue());
    	assertTrue(monthSelector.isDisabled());
    	assertTrue(deleteActualMonthButton.isDisabled());
    	assertTrue(insertLineButton.isDisabled());
    	assertTrue(insertPane.isDisabled());
    	assertTrue(dataTableView.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=2,accountNumber = 1, monthNumber = 2)
    public void CheckAutofillAccount() {
    	clickOn(companySelector).clickOn(getComboPopupList(this).get(0));
    	assertEquals(companies.get(0).getAccounts(),accountSelector.getItems());
    	assertEquals(companies.get(0).getAccounts().get(0),accountSelector.getValue());
    	assertNull(monthSelector.getValue());
    	assertTrue(deleteActualMonthButton.isDisabled());
    	assertTrue(insertLineButton.isDisabled());
    	assertTrue(insertPane.isDisabled());
    	assertTrue(dataTableView.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=2,accountNumber = 2,monthNumber = 1)
    public void CheckAutofillMonth() {
    	clickOn(companySelector).clickOn(getComboPopupList(this).get(0));
    	clickOn(accountSelector).clickOn(getComboPopupList(this).get(0));
    	assertEquals(1,monthSelector.getItems().size());
    	assertEquals(accountSelector.getItems().get(0).getBalanceMonths().get(0).getMonth(),monthSelector.getValue());
    	assertEquals("Buchungsjahr: 01.07.2019 bis 30.06.2020",actAccountingYearLabel.getText());
    	assertFalse(savePane.isDisabled());
    	assertFalse(insertPane.isDisabled());
    	assertFalse(dataTableView.isDisabled());
    }
    
    @Test
    @CreateCompanies(value=2,accountNumber = 1,monthNumber = 0)
    public void CheckAccountLabelNoChangeNoMonth() {
    	assertEquals("Kein Konot gewählt",balanceLabel.getText());
    	Account spyAccount=Mockito.spy(companies.get(0).getAccounts().get(0));
    	companies.get(0).getAccounts().set(0, spyAccount);
    	clickOn(companySelector).type(KeyCode.DOWN).type(KeyCode.ENTER);
    	
    	verify(spyAccount,atLeast(1)).updateBalance();
    	assertEquals(String.format("Kontostand: %.2f EUR, keine gebuchten Monate",
    			companies.get(0).getAccounts().get(0).getBalance().getNumber().doubleValue()),
    			balanceLabel.getText());
    }
    
    @Test
    @CreateCompanies(value=2,accountNumber = 1,monthNumber = 1)
    public void CheckAccountLabelNoChangeOneMonth() {
    	assertEquals("Kein Konot gewählt",balanceLabel.getText());
    	Account spyAccount=Mockito.spy(companies.get(0).getAccounts().get(0));
    	companies.get(0).getAccounts().set(0, spyAccount);
    	clickOn(companySelector).type(KeyCode.DOWN).type(KeyCode.ENTER);
    	
    	verify(spyAccount,atLeast(1)).updateBalance();
    	assertEquals(String.format("Kontostand: %.2f EUR, 1 gebuchter Monat, %02d.%02d",
    			companies.get(0).getAccounts().get(0).getBalance().getNumber().doubleValue(),
    			spyAccount.getBalanceMonths().get(0).getMonth().getMonthValue(),19),balanceLabel.getText());
    }
    
    @Test
    @CreateCompanies(value=2,accountNumber = 1,monthNumber = 5)
    public void CheckAccountLabelNoChangeManyMonths() {
    	assertEquals("Kein Konot gewählt",balanceLabel.getText());
    	Account acc=companies.get(0).getAccounts().get(0);
    	Account spyAccount=Mockito.spy(acc);
    	companies.get(0).getAccounts().set(0, spyAccount);
    	type(KeyCode.DOWN).type(KeyCode.TAB);
    	
    	verify(spyAccount,atLeast(1)).updateBalance();
    	acc.updateBalance();
    	assertEquals(String.format("Kontostand: %.2f EUR, 5 gebuchte Monate von %02d.%02d bis %02d.%02d",
    			acc.getBalance().getNumber().doubleValue(),
    			acc.getBalanceMonths().get(0).getMonth().getMonthValue(),19,
    			acc.getBalanceMonths().get(4).getMonth().getMonthValue(),19),balanceLabel.getText());
    	assertTrue(acc.getBalanceMonths().get(0).getMonth().isBefore(
    			acc.getBalanceMonths().get(4).getMonth()));
    }
    
    
    static Stream<Arguments> insertCharsValues() {
        return Stream.of(
    		Arguments.of(new String[] {"4"," ","12345","12","123","Voucher","123","","1234","Description"},
    				new Transaction(1,Money.of(4, "EUR"),Money.of(0, "EUR"),12345,12,123,"Voucher",LocalDate.of(2019, 3, 12),vats.get(1), "1234", "Description")),
    		Arguments.of(new String[] {""," 12","12345","12","123","Voucher","123","","1234","Description"},
    				new Transaction(1,Money.of(0, "EUR"),Money.of(12, "EUR"),12345,12,123,"Voucher",LocalDate.of(2019, 3, 12),vats.get(1), "1234", "Description")),
    		Arguments.of(new String[] {"","","","","","","","","",""},
    				new Transaction(1,Money.of(0, "EUR"),Money.of(0, "EUR"),null,null,null,null,null,vats.get(1), null, null)),
    		Arguments.of(new String[] {"EUR 4","Ostern 4 ","EE12345EE","EE12EE","EE123EE","Voucher","EE123WW","V","1234","Description"},
    				new Transaction(1,Money.of(4, "EUR"),Money.of(4, "EUR"),12345,12,123,"Voucher",LocalDate.of(2019, 3, 12),vats.get(1), "1234", "Description"))
        		);
    }
    
	@ParameterizedTest
    @MethodSource("insertCharsValues")
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1,transactionNumber = 0)
    public void CheckInputTransaction(String[] inputs,Transaction ref) {
    	clickOn(receiptsInput);
    	for(int i=0;i<inputs.length;++i) {
    		write(inputs[i]).type(KeyCode.TAB);
    	}
    	clickOn(insertLineButton);
    	
    	assertEquals(1,dataTableView.getItems().size());
    	assertEquals(ref,dataTableView.getItems().get(0));
    }
	
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1,transactionNumber = 0)
    public void CheckDeleteEmptyMonth() {
    	assertEquals(accountSelector.getItems().get(0).getBalanceMonths().get(0).getMonth(),monthSelector.getValue());
    	MonthAccountTurnover month=accountSelector.getItems().get(0).getBalanceMonths().get(0);
    	
    	clickOn(deleteActualMonthButton);
    	
    	assertNull(monthSelector.getValue());
    	assertTrue(monthSelector.getItems().isEmpty());
    	assertTrue(insertPane.isDisabled());
    	
    	List<Object> res=new ArrayList<Object>();
    	doAnswer(new Answer<Void>() {
			@SuppressWarnings("unchecked")
			public Void answer(InvocationOnMock invocation) {
    	        Object[] args = invocation.getArguments();
    	        if(args[0] instanceof Collection) {
    	        	res.addAll((Collection<? extends Object>) args[0]);
    	        }
    	        return null;
    	      }}).when(db).deleteData(any());
    	
    	verify(db,times(0)).deleteData(any());
    	clickOn(saveChangesButton);
    	verify(db).deleteData(any());
    	
    	assertTrue(res.contains(month));
    	assertEquals(1,res.size());
    }

    @Test
    @CreateCompanies(value=2,accountNumber = 1,monthNumber = 1,transactionNumber = 7)
    public void CheckDeleteFilledMonthOK() {
    	Account spyAccount=Mockito.spy(companies.get(0).getAccounts().get(0));
    	companies.get(0).getAccounts().set(0, spyAccount);
    	clickOn(companySelector).type(KeyCode.DOWN).type(KeyCode.ENTER);
    	assertEquals(accountSelector.getItems().get(0).getBalanceMonths().get(0).getMonth(),monthSelector.getValue());
    	MonthAccountTurnover month=accountSelector.getItems().get(0).getBalanceMonths().get(0);
    	Set<Node> bsMain=lookup(".button").queryAll();
    	Set<Node> labelMain=lookup(".label").queryAll();
    	
    	clickOn(deleteActualMonthButton);
    	
    	Set<Node> bsAll=lookup(".button").queryAll();
    	List<String> buttonTexts = new ArrayList<String>(2);
    	Button bc= null;
    	for (Node node : bsAll) {
			assertTrue(node instanceof Button);
			if (bsMain.contains(node))
				continue;
			buttonTexts.add(((Button) node).getText());
			if(((Button) node).getText().equals(okText))
				bc=(Button) node;
		}
    	Set<Node> labelAll=lookup(".label").queryAll();
    	List<String> labelTexts = new ArrayList<String>(2);
    	for (Node node : labelAll) {
			assertTrue(node instanceof Label);
			if (labelMain.contains(node))
				continue;
			labelTexts.add(((Label) node).getText());
		}
    	assertTrue(buttonTexts.contains(okText));
    	assertTrue(buttonTexts.contains(cancelText));
    	assertEquals(2,buttonTexts.size());
    	
    	assertTrue(labelTexts.contains("Möchten sie wirklich den Monat August 2019 löschen?"));
    	assertTrue(labelTexts.contains("Der Monat enthält 7 Transaktionen, die ebenfalls gelöscht werden."));
    	assertEquals(2,labelTexts.size());
    	
    	assertNotNull(bc);
    	
    	clickOn(bc);
    	
    	assertNull(monthSelector.getValue());
    	assertTrue(monthSelector.getItems().isEmpty());
    	verify(spyAccount,atLeast(1)).updateBalance();
    	assertTrue(insertPane.isDisabled());
    	
    	List<Object> res=new ArrayList<Object>();
    	doAnswer(new Answer<Void>() {
			@SuppressWarnings("unchecked")
			public Void answer(InvocationOnMock invocation) {
    	        Object[] args = invocation.getArguments();
    	        if(args[0] instanceof Collection) {
    	        	res.addAll((Collection<? extends Object>) args[0]);
    	        }
    	        return null;
    	      }}).when(db).deleteData(any());
    	
    	verify(db,times(0)).deleteData(any());
    	clickOn(saveChangesButton);
    	verify(db).deleteData(any());
    	
    	assertTrue(res.contains(month));
    	assertTrue(res.containsAll(month.getTransactions()));
    	assertEquals(1+month.getTransactions().size(),res.size());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1,transactionNumber = 7)
    public void CheckDeleteFilledMonthCancel() {
    	assertEquals(accountSelector.getItems().get(0).getBalanceMonths().get(0).getMonth(),monthSelector.getValue());
    	Set<Node> bsMain=lookup(".button").queryAll();
    	
    	clickOn(deleteActualMonthButton);
    	
    	Set<Node> bsAll=lookup(".button").queryAll();
    	Button bc= null;
    	for (Node node : bsAll) {
			assertTrue(node instanceof Button);
			if (bsMain.contains(node))
				continue;
			if(((Button) node).getText().equals(cancelText))
				bc=(Button) node;
		}
    	
    	clickOn(bc);
    	
    	List<Object> res=new ArrayList<Object>();
    	doAnswer(new Answer<Void>() {
			@SuppressWarnings("unchecked")
			public Void answer(InvocationOnMock invocation) {
    	        Object[] args = invocation.getArguments();
    	        if(args[0] instanceof Collection) {
    	        	res.addAll((Collection<? extends Object>) args[0]);
    	        }
    	        return null;
    	      }}).when(db).deleteData(any());
    	
    	verify(db,times(0)).deleteData(any());
    	clickOn(saveChangesButton);
    	verify(db).deleteData(any());
    	
    	assertTrue(res.isEmpty());    	
    	assertEquals(accountSelector.getItems().get(0).getBalanceMonths().get(0).getMonth(),monthSelector.getValue());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 2,transactionNumber = 2)
    public void CheckDeleteMonthNoSelection() {
    	savePane.setDisable(false);
    	deleteActualMonthButton.setDisable(false);
    	assertEquals(null,monthSelector.getValue());
    	sleep(10);
    	clickOn(deleteActualMonthButton);
    	
    	verify(db,times(0)).remove(any(MonthAccountTurnover.class));
    	assertEquals(2,accountSelector.getValue().getBalanceMonths().size());
    }
    
    @Tag("Active")
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1,transactionNumber = 0)
    public void CheckInsertOnEnterInLastField() throws Exception {
    	assertTrue(dataTableView.getItems().size()==0);
    	type(KeyCode.TAB,2);
    	for(int i=0;i<dataTableView.getColumns().size()-1;++i) {
    		type(KeyCode.TAB,KeyCode.DOWN);
    		write("Hugo123");    		
    	}
    	type(KeyCode.ENTER);
    	assertTrue(dataTableView.getItems().size()==1);
    	assertEquals(receiptsInput,scene.focusOwnerProperty().get());
    	assertEquals(null,receiptsInput.getText());
    	assertEquals(null,spendingInput.getText());
    	assertEquals("",accountingContraAccountInput.getText());
    	assertEquals("",accountingCostGroupInput.getText());
    	assertEquals("",accountingCostCenterInput.getText());
    	assertEquals("",voucherInput.getText());
    	assertEquals("",transactionDateInput.getText());
    	assertEquals("",inventoryNumberInput.getText());
    	assertEquals("",descriptionOfTransactionInput.getText());
    	assertEquals(vats.get(1),vatInput.getValue());
    }
    
    @Tag("Active")
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1,transactionNumber = 0)
    public void CheckDoubleInsertOnEnterInLastField() throws Exception {
    	assertTrue(dataTableView.getItems().size()==0);
    	type(KeyCode.TAB,3);
    	for(int j=0;j<2;++j) {
	    	for(int i=0;i<dataTableView.getColumns().size()-2;++i) {
	    		type(KeyCode.TAB);    		
	    	}
	    	type(KeyCode.ENTER);
    	}
    	assertEquals(2,dataTableView.getItems().size());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 2,monthNumber = 2,transactionNumber = 2)
    public void CheckRevertChanges() throws Exception {
    	verify(db,times(1)).getCompanies();
    	
    	type(KeyCode.DOWN,KeyCode.TAB,KeyCode.DOWN,KeyCode.TAB);
    	write("47").type(KeyCode.ENTER).type(KeyCode.TAB,10).type(KeyCode.ENTER);
    	Field fieldTurnovers=TransactionInputPaneController.class.getDeclaredField("turnoverList");
    	Field fieldActMonth=TransactionInputPaneController.class.getDeclaredField("actMonthList");
    	fieldTurnovers.setAccessible(true);
    	fieldActMonth.setAccessible(true);
    	
    	assertFalse(((Map<?,?>)fieldTurnovers.get(controller)).isEmpty());
    	assertNotNull(fieldActMonth.get(controller));
    	
    	clickOn(revertChangesButton);
    	
    	verify(db,times(2)).getCompanies();
    	assertTrue(((Map<?,?>)fieldTurnovers.get(controller)).isEmpty());
    	assertNull(fieldActMonth.get(controller));
    	
    	assertNotNull(companySelector.getValue());
    	assertNull(accountSelector.getValue());
    	assertTrue(monthSelector.isDisabled());
    }

    @Test
    @CreateCompanies(value=2,accountNumber = 2,monthNumber = 2,transactionNumber = 2)
    public void CheckPersistChanges() {
    	Account actAccount=companies.get(0).getAccounts().get(0);
    	Account spyAccount=Mockito.spy(actAccount);
    	companies.get(0).getAccounts().set(0, spyAccount);
    	type(KeyCode.DOWN).type(KeyCode.TAB).type(KeyCode.DOWN).type(KeyCode.TAB);
    	write("4.18").type(KeyCode.ENTER);
    	clickOn(insertLineButton);
    	reset(spyAccount);
    	clickOn(saveChangesButton);
    	ArrayList<List<? extends AbstractDataObject>> dataList = new ArrayList<List<? extends AbstractDataObject>>();
    	dataList.add(actAccount.getBalanceMonths());
    	dataList.add(new ArrayList<Account>(List.of(spyAccount)));
    	
    	verify(spyAccount,times(2)).updateBalance();
    	verify(db).mergeData(dataList);    	
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1,transactionNumber = 1)
    public void CheckUpdateDataAddedCompaniesFlat() {
    	Company actCompany=companySelector.getValue();
    	List<Company> locCompanies = createCompanies(3, 2,2,2);
    	locCompanies.addAll(companies);
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	type(KeyCode.F5);
    	sleep(10);
    	verify(db,times(1)).getCompanies();
    	assertTrue(companySelector.getItems().containsAll(locCompanies));
    	assertEquals(locCompanies.size(),companySelector.getItems().size());
    	assertEquals(actCompany,companySelector.getValue());
    	assertEquals(actCompany.getAccounts().get(0),accountSelector.getValue());
    	assertEquals(actCompany.getAccounts().get(0).
    			getBalanceMonths().get(0).getMonth(),monthSelector.getValue());	
    }
    
    @Test
    @CreateCompanies(value=3,accountNumber = 4,monthNumber = 5,transactionNumber = 1)
    public void CheckUpdateDataAddedCompaniesAlternateFlat() {
    	type(KeyCode.DOWN,KeyCode.DOWN,KeyCode.TAB);
		type(KeyCode.DOWN,3).type(KeyCode.TAB).write("704").type(KeyCode.ENTER);
    	Company actCompany=companySelector.getValue();
    	Account actAccount=accountSelector.getValue();
    	LocalDate actMonth=monthSelector.getValue();
    	List<Company> locCompanies = createCompanies(3, 2,2,2);
    	locCompanies.addAll(companies);
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	type(KeyCode.F5);
    	sleep(10);
    	verify(db,times(1)).getCompanies();
    	assertTrue(companySelector.getItems().containsAll(locCompanies));
    	assertEquals(locCompanies.size(),companySelector.getItems().size());
    	assertEquals(actCompany,companySelector.getValue());
    	assertEquals(actAccount,accountSelector.getValue());
    	assertEquals(actMonth,monthSelector.getValue());
    }
    
    @Test
    @CreateCompanies(value=3,accountNumber = 3,monthNumber = 3,transactionNumber = 3)
    public void CheckUpdateDataDeleteActCompany() {
    	type(KeyCode.DOWN,KeyCode.DOWN,KeyCode.TAB);
		type(KeyCode.DOWN,3).type(KeyCode.TAB).write("704").type(KeyCode.ENTER);
    	Company actCompany=companySelector.getValue();
    	List<Company> locCompanies = new ArrayList<Company>();
    	for (Company company : companies) {
    		if(company==actCompany)
    			continue;
			locCompanies.add(new Company(company));
		}
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	    	
    	type(KeyCode.F5);
    	sleep(10);
    	verify(db,times(1)).getCompanies();
    	assertTrue(companySelector.getItems().containsAll(locCompanies));
    	assertEquals(locCompanies.size(),companySelector.getItems().size());
    	assertEquals(null,companySelector.getValue());
    	assertEquals(null,accountSelector.getValue());
    	assertEquals(null,monthSelector.getValue());
    }
    
    @Test
    @CreateCompanies(value=3,accountNumber = 3,monthNumber = 3,transactionNumber = 3)
    public void CheckUpdateDataDeleteActAccount() {
    	type(KeyCode.DOWN,KeyCode.DOWN,KeyCode.TAB);
		type(KeyCode.DOWN,3).type(KeyCode.TAB).write("704").type(KeyCode.ENTER);
    	Company actCompany=companySelector.getValue();
    	Account actAccount=accountSelector.getValue();
    	List<Company> locCompanies = new ArrayList<Company>();
    	for (Company company : companies) {
    		Company tmp = new Company(company);
    		if(company==actCompany)
    			tmp.getAccounts().remove(actAccount);    			
			locCompanies.add(tmp);
		}
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	    	
    	type(KeyCode.F5);
    	sleep(10);
    	verify(db,times(1)).getCompanies();
    	assertTrue(companySelector.getItems().containsAll(locCompanies));
    	assertEquals(locCompanies.size(),companySelector.getItems().size());
    	assertEquals(actCompany.getUid(),companySelector.getValue().getUid());
    	assertEquals(null,accountSelector.getValue());
    	assertEquals(null,monthSelector.getValue());
    }
	
    @Test
    @CreateCompanies(value=3,accountNumber = 3,monthNumber = 3,transactionNumber = 3)
    public void CheckUpdateDataDeleteActMonth() {
    	type(KeyCode.DOWN,KeyCode.DOWN,KeyCode.TAB);
		type(KeyCode.DOWN,3).type(KeyCode.TAB).write("619").type(KeyCode.ENTER);
    	Company actCompany=companySelector.getValue();
    	Account actAccount=accountSelector.getValue();
    	LocalDate actMonth=monthSelector.getValue();
    	List<Company> locCompanies = new ArrayList<Company>();
    	for (Company company : companies) {
    		for(Account account:company.getAccounts()){
    			account.updateBalance();
    		}
    		Company tmp = new Company(company);
			for(Account account:tmp.getAccounts()){
				MonthAccountTurnover balanceMonth=null;
				for(MonthAccountTurnover month:account.getBalanceMonths()) {
					if(month.isInMonth(actMonth)) {
						balanceMonth=month;
						break;
					}
				}
				account.getBalanceMonths().remove(balanceMonth);
				account.setAccountName(account.getAccountName()+"a");
				account.updateBalance();
			}
			locCompanies.add(tmp);
		}
    	type(KeyCode.F5);
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	    	
    	type(KeyCode.F5);
    	sleep(10);
    	verify(db,times(1)).getCompanies();
    	assertTrue(companySelector.getItems().containsAll(locCompanies));
    	assertEquals(locCompanies.size(),companySelector.getItems().size());
    	assertEquals(actCompany.getUid(),companySelector.getValue().getUid());
    	assertEquals(actAccount.getUid(),accountSelector.getValue().getUid());
    	assertEquals(null,monthSelector.getValue());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 3,transactionNumber = 3)
    public void CheckKeepAddedMonth() {
    	List<Company> locCompanies = new ArrayList<Company>();
    	for (Company company : companies) {
			locCompanies.add(new Company(company));
		}
    	type(KeyCode.TAB,2).write("604").type(KeyCode.ENTER);
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	    	
    	type(KeyCode.F5);
    	sleep(10);
    	assertEquals(4,accountSelector.getValue().getBalanceMonths().size());
    	assertNotNull(monthSelector.getValue());
    }
    
    public Transaction getDummyTransaction(Long uid, int number) {
    	return new Transaction(Long.valueOf(uid), ZonedDateTime.now(), number, 
    					Money.of(0, "EUR"),Money.of(0, "EUR"),
    							null,null,null,null,LocalDate.of(2019, 6, 6),null,null,null);
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1,transactionNumber = 3)
    public void CheckUpdateDataAddToActMonthDatabase() {
    	type(KeyCode.F5);
    	    	
    	List<Company> locCompanies = new ArrayList<Company>(List.of(new Company(companies.get(0))));
    	locCompanies.get(0).getAccounts().get(0).getBalanceMonths().get(0).insertTranaction(1, getDummyTransaction(7L, 0));
    	List<Transaction> transactions=locCompanies.get(0).getAccounts().get(0).getBalanceMonths().get(0).getTransactions();
    	
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	    	
    	type(KeyCode.F5);
    	sleep(10);
    	verify(db,times(1)).getCompanies();
    	assertEquals(4,transactions.size());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1,transactionNumber = 3)
    public void CheckUpdateDataAddToActMonthBoth() {
    	type(KeyCode.F5);
    	
    	List<Company> locCompanies = new ArrayList<Company>(List.of(new Company(companies.get(0))));
    	locCompanies.get(0).getAccounts().get(0).getBalanceMonths().get(0).insertTranaction(1, getDummyTransaction(7L, 0));
    	List<Transaction> transactions=locCompanies.get(0).getAccounts().get(0).getBalanceMonths().get(0).getTransactions();
    	List<Transaction> transactionsRef=new ArrayList<Transaction>();
    	for (Transaction transaction : transactions) {
			transactionsRef.add(new Transaction(transaction));
		}
    	
    	type(KeyCode.TAB,16);
    	type(KeyCode.ENTER);
    	transactionsRef.add(new Transaction(companies.get(0).getAccounts().get(0).
    			getBalanceMonths().get(0).getTransactions().get(3)));
    	transactionsRef.get(4).setNumber(5);
    	    	
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	    	
    	type(KeyCode.F5);
    	sleep(10);
    	verify(db,times(1)).getCompanies();
    	assertTrue(transactions.containsAll(transactionsRef));
    }

    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1,transactionNumber = 3)
    public void CheckUpdateDataAddToActMonthForm() {
    	type(KeyCode.F5);
    	    	
    	List<Company> locCompanies = new ArrayList<Company>(List.of(new Company(companies.get(0))));
    	List<Transaction> transactions=locCompanies.get(0).getAccounts().get(0).getBalanceMonths().get(0).getTransactions();

    	type(KeyCode.TAB,16);
    	type(KeyCode.ENTER);
    	
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	    	
    	type(KeyCode.F5);
    	sleep(10);
    	verify(db,times(1)).getCompanies();
    	assertEquals(4,transactions.size());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1,transactionNumber = 3)
    public void CheckMergeMonthTRansactionsChanged() {
    	type(KeyCode.F5);
    	    	
    	List<Company> locCompanies = new ArrayList<Company>(List.of(new Company(companies.get(0))));
    	MonthAccountTurnover month=locCompanies.get(0).getAccounts().get(0).getBalanceMonths().get(0);
    	
    	month.getTransactions().get(1).setReceipts(Money.of(12345.56, "EUR"));
    	
    	doubleClickOn(getCell(1,1)).write("23").type(KeyCode.ENTER);
    	doubleClickOn(getCell(1,2)).write("13").type(KeyCode.ENTER);
    	
    	month.getTransactions().get(2).setReceipts(Money.of(12345.56, "EUR"));
    	    	    	
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	    	
    	type(KeyCode.F5);
    	sleep(10);
    	verify(db,times(1)).getCompanies();
    	
    	month=accountSelector.getValue().getBalanceMonths().get(0);
    	
    	assertEquals(Money.of(23, "EUR"),month.getTransactions().get(1).getReceipts());
    	assertEquals(Money.of(12345.56, "EUR"),month.getTransactions().get(2).getReceipts());
    }
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 3,transactionNumber = 3)
    public void CheckMergeMonths() {
    	type(KeyCode.F5);
    	    	
    	List<Company> locCompanies = new ArrayList<Company>(List.of(new Company(companies.get(0))));
    	List<MonthAccountTurnover> months=locCompanies.get(0).getAccounts().get(0).getBalanceMonths();
    			
    	type(KeyCode.TAB,2);
    	write("520").type(KeyCode.ENTER).type(KeyCode.DELETE,5);
    	months.add(MonthAccountTurnover.getEmptyMonthAccountTurnover(LocalDate.of(2020,4,1), locCompanies.get(0).getAccounts().get(0)));
    	write("720").type(KeyCode.ENTER);
    	months.add(MonthAccountTurnover.getEmptyMonthAccountTurnover(LocalDate.of(2020,6,1), locCompanies.get(0).getAccounts().get(0)));
    	
    	    	
    	reset(db);
    	when(db.getCompanies()).thenReturn(locCompanies);
    	    	
    	type(KeyCode.F5);
    	sleep(10);
    	verify(db,times(1)).getCompanies();
    	for (MonthAccountTurnover month:locCompanies.get(0).getAccounts().get(0).getBalanceMonths()) {
			System.out.println(month.getMonth().toString());
		}
    	assertEquals(7,locCompanies.get(0).getAccounts().get(0).getBalanceMonths().size());
    	assertEquals(7,monthSelector.getItems().size());
    }
    
    
    @Test
    @CreateCompanies(value=1,accountNumber = 1,monthNumber = 1,transactionNumber = 3)
    public void CheckDeleteTransaction() {
    	MonthAccountTurnover oldMonth = 
    			new MonthAccountTurnover(companies.get(0).getAccounts().get(0).getBalanceMonths().get(0));
    	
    	clickOn(getCell(0,1), MouseButton.SECONDARY);
    	sleep(10);
    	clickOn((Node) lookup("#DeleteOptionButton").query());
    	
    	MonthAccountTurnover actMonth = 
    			new MonthAccountTurnover(companies.get(0).getAccounts().get(0).getBalanceMonths().get(0));
    	oldMonth.getTransactions().get(2).setNumber(2);
    	
    	assertEquals(2,actMonth.getTransactions().size());
    	assertTrue(actMonth.getTransactions().contains(oldMonth.getTransactions().get(0)));
    	assertFalse(actMonth.getTransactions().contains(oldMonth.getTransactions().get(1)));
    	assertTrue(actMonth.getTransactions().contains(oldMonth.getTransactions().get(2)));
    }
    
}
