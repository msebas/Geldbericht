package org.mcservice.geldbericht;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static java.lang.Math.pow;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.mcservice.AfterFXInitBeforeEach;
import org.mcservice.MockedApplicationTest;
import org.mcservice.geldbericht.App;
import org.mcservice.geldbericht.CompanyManagerController;
import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.testfx.util.BoundsQueryUtils;

import com.sun.javafx.scene.control.LabeledText;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;


import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.ModifierValue;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

@Tag("Active")
@Tag("GUI")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
//@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class AccountManagerTest extends MockedApplicationTest{

	@Retention(RetentionPolicy.RUNTIME)
	private @interface CreateCompanies {
		   int value() default 3;
		}
	@Retention(RetentionPolicy.RUNTIME)
	private @interface CreateCompany {
		String name() default "Company & Co GmbH";
		String number() default "12345";
		String appointment() default "1234567890";
		int accountNumber() default 2;
		boolean insertError() default false;
		boolean skip() default false;
	}
	
	@Mock
	DbAbstractionLayer db;
	
	String okText=javafx.scene.control.ButtonType.OK.getText();
	String cancelText=javafx.scene.control.ButtonType.CANCEL.getText();
	
	ZonedDateTime mockListCreation=null;
	List<Company> companies=null;
	TableView<Account> tableView=null;
	ComboBox<Company> companySelector=null;
	int allAccounts=0;
		
	@Override 
	public void start(Stage stage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("AccountManager.fxml"));
		AccountManagerController controller = new AccountManagerController(db);
		fxmlLoader.setController(controller);
		Scene scene = new Scene(fxmlLoader.load());
		stage.setScene(scene);
        stage.show();
    }

	public List<Account> createAccounts(int n,Company company){
		List<Account> result = new ArrayList<Account>(n);
		if(n==0)
			return result;
		for (int j = 0; j < n-1; j++) {
			result.add(new Account(String.format("%05d",allAccounts+j),
								String.format("Account %5d",allAccounts+j),
								Money.of(allAccounts+j,"EUR"), company));
		}
		result.add(new Account(Long.valueOf(12), ZonedDateTime.now(),
				String.format("%05d",allAccounts+n-1),
				String.format("Account UID  %5d",allAccounts+n-1),
				Money.of(allAccounts+n-1,"EUR"), company));	
		allAccounts+=n;
		return result;
	}
	
	@BeforeEach 
	public void initMock(TestInfo testInfo) {
		ArrayList<Company> act=new ArrayList<Company>();
		mockListCreation=ZonedDateTime.now();
		if (testInfo.getTestMethod().get()!=null) {
			Method actTest=testInfo.getTestMethod().get();
			for (Annotation annotation : actTest.getAnnotations()) {
				if (annotation.annotationType().equals(CreateCompanies.class)) {
					for (int i=0;i<((CreateCompanies) annotation).value();++i) {
						Company tmp=new Company((long) i,mockListCreation,null,String.format("Company Name %d",i),
								String.format("%05d",i),
								String.format("%010d",i));
						tmp.setAccounts(createAccounts(3, tmp));
						act.add(tmp);
					}
				}
				
				if (annotation.annotationType().equals(CreateCompany.class)) {
					Company tmp=new Company((long) 1,mockListCreation,null,
							((CreateCompany) annotation).name(),
							((CreateCompany) annotation).number(),
							((CreateCompany) annotation).appointment());
					List<Account> accounts=createAccounts(((CreateCompany) annotation).accountNumber(),tmp);
					if(((CreateCompany) annotation).insertError() && ((CreateCompany) annotation).accountNumber()>0) {
						accounts.get(0).setAccountNumber("dfg");
					}
					tmp.setAccounts(accounts);
					act.add(tmp);
				}
			}
		}
		mockListCreation=ZonedDateTime.now();
		when(db.getCompanies()).thenReturn(act);
		companies=act;
	}
    
    @AfterFXInitBeforeEach
    public void setArgs() {
    	Node tmpNode=lookup("#accountTableView").query();
		if (tmpNode instanceof TableView<?>) {
			@SuppressWarnings("unchecked")
			TableView<Account> tmpView =(TableView<Account>) tmpNode;
			tableView=tmpView;
		}
		tmpNode=lookup("#companySelector").query();
		if (tmpNode instanceof ComboBox<?>) {
			@SuppressWarnings("unchecked")
			ComboBox<Company> tmpView =(ComboBox<Company>) tmpNode;
			companySelector=tmpView;
		}
    }
    
    public TableCell<?, ?> getCell(int columnIndex, int rowIndex) {
        TableRow<?> row = null;
        for (Node actNode : tableView.lookupAll(".table-row-cell")) {
            TableRow<?> actRow = (TableRow<?>) actNode;
            if (actRow.getIndex() == rowIndex) {
                row = actRow;
                break;
            }
        }
        for (Node actNode : row.lookupAll(".table-cell")) {
            TableCell<?, ?> cell = (TableCell<?, ?>) actNode;
            if (tableView.getColumns().indexOf(cell.getTableColumn()) == columnIndex) {
            	return cell;
            }
        }
        return null;
    }
    
	private List<LabeledText> getPopupList() {
		Node t=lookup(".combo-box-popup").query();
    	Set<Node> b=t.lookupAll(".text");
    	List<LabeledText> l=new ArrayList<LabeledText>();
    	for (Node labeledText : b) {
			if(labeledText instanceof LabeledText && ((LabeledText) labeledText).getText().length()!=0) {
				l.add((LabeledText) labeledText);
			}
		}
		return l;
	}
    
    @Test
    @Disabled
    @CreateCompanies
    public void manual() {
    	sleep(3600000);
    }
    
    @Test
    @CreateCompanies(1)
    public void checkAutofillSingleCompany() {
    	assertTrue(companySelector.getValue().equals(companies.get(0)));
    	assertTrue(tableView.getItems().containsAll(companies.get(0).getAccounts()));
    	assertEquals(tableView.getItems().size(),companies.get(0).getAccounts().size());
    	Node t=lookup("#addButton").query();
    	assertTrue(t instanceof Button);
    	assertFalse(((Button) t).isDisabled());
    	
    	t=lookup("#persistButton").query();
    	assertTrue(t instanceof Button);
    	assertTrue(((Button) t).isDisabled());
    	
    	t=lookup("#changesLabel").query();
    	assertTrue(t instanceof Label);
    	assertEquals("Keine Änderungen",((Label) t).getText());
    	
    	t=lookup("#persistButton").query();
    	assertTrue(t instanceof Button);
    	assertTrue(((Button) t).isDisabled());
    	t=lookup("#cancelButton").query();
    	
    	clickOn(t);
    	sleep(200);
    	
    	verify(db,times(0)).manageAccounts(eq(tableView.getItems()),any());
    	assertFalse(tableView.getScene().getWindow().isShowing());
    }
    
    @Test
    @CreateCompanies(2)
    public void checkNoAutofillTwoCompaniesPersistAndClose() {
    	assertTrue(companySelector.getValue()==null);
    	assertTrue(tableView.getItems()==null);
    	Node t=lookup("#addButton").query();
    	assertTrue(t instanceof Button);
    	assertTrue(((Button) t).isDisabled());
    	
    	t=lookup("#persistButton").query();
    	assertTrue(t instanceof Button);
    	assertTrue(((Button) t).isDisabled());
    	
    	((Button) t).setDisable(false);
    	clickOn(t);
    	
    	verify(db).manageAccounts(eq(tableView.getItems()),any());
    	assertFalse(tableView.getScene().getWindow().isShowing());
    }
    
    @Test
    @CreateCompanies()
    public void selectACompanySelectNextCompany() {
    	clickOn(companySelector);
    	LabeledText l = getPopupList().get(0);
    	clickOn(l);
    	    	
    	assertTrue(companySelector.getValue()==this.companies.get(0));
    	assertTrue(tableView.getItems().containsAll(companies.get(0).getAccounts()));
    	assertEquals(tableView.getItems().size(),companies.get(0).getAccounts().size());

    	clickOn(companySelector);
    	l = getPopupList().get(1);
    	clickOn(l);
    	
    	assertTrue(companySelector.getValue()==this.companies.get(1));
    	assertTrue(tableView.getItems().containsAll(companies.get(1).getAccounts()));
    	assertEquals(tableView.getItems().size(),companies.get(1).getAccounts().size());
    	
    	clickOn(companySelector);
    	l = getPopupList().get(1);
    	clickOn(l);
    	
    	assertTrue(companySelector.getValue()==this.companies.get(1));
    	assertTrue(tableView.getItems().containsAll(companies.get(1).getAccounts()));
    	assertEquals(tableView.getItems().size(),companies.get(1).getAccounts().size());
    }
    
    @Test
    @CreateCompany(accountNumber=0)
    public void ClickAddButtonFillNewAccount() {
    	clickOn("#addButton");
    	assertEquals(1,tableView.getItems().size());
    	assertEquals("",tableView.getItems().get(0).getAccountName());
    	assertEquals("",tableView.getItems().get(0).getAccountNumber());
    	assertEquals(Money.of(0,"EUR"),tableView.getItems().get(0).getInitialBalance());
    	assertEquals(companies.get(0),tableView.getItems().get(0).getCompany());
    	
    	doubleClickOn(getCell(0, 0)).write("12345").type(KeyCode.ENTER);
    	write("Account Name").type(KeyCode.ENTER).type(KeyCode.RIGHT);
    	write("1.234,567").type(KeyCode.ENTER);
    	
    	assertEquals(1,tableView.getItems().size());
    	assertEquals("Account Name",tableView.getItems().get(0).getAccountName());
    	assertEquals("12345",tableView.getItems().get(0).getAccountNumber());
    	assertEquals(Money.of(1234.56,"EUR"),tableView.getItems().get(0).getInitialBalance());
    	assertEquals(Money.of(1234.56,"EUR"),tableView.getItems().get(0).getBalance());
    	assertEquals(companies.get(0),tableView.getItems().get(0).getCompany());
    }
    
    @Tag("Active")
    @Test
    @CreateCompany(accountNumber=1,insertError=true)
    public void ClickAddButtonCorrectAccount() {
    	assertTrue(getCell(0,0).getStyleClass().contains("cell-validation-error"));
    	Node t=lookup("#persistButton").query();
    	assertTrue(t instanceof Button);
    	assertTrue(((Button) t).isDisabled());
    	
    	doubleClickOn(getCell(1, 0)).write(" Stuff").type(KeyCode.ENTER);
    	
    	assertTrue(((Button) t).isDisabled());
    	
    	type(KeyCode.LEFT).type(KeyCode.LEFT).type(KeyCode.ENTER);
    	type(KeyCode.BACK_SPACE,3);
    	write("12345").type(KeyCode.ENTER);
    	
    	assertFalse(((Button) t).isDisabled());
    	
    	assertEquals("12345",tableView.getItems().get(0).getAccountNumber());
    	
    	clickOn(t);
    	
    	verify(db).manageAccounts(eq(tableView.getItems()),any());
    	assertFalse(tableView.getScene().getWindow().isShowing());
    }

   
    @Test
    @CreateCompanies()
    public void CheckMessageUnsavedChangesCancel() {
    	clickOn(companySelector);
    	LabeledText l = getPopupList().get(0);
    	clickOn(l);
    	
    	clickOn("#addButton");
    	doubleClickOn(getCell(0, 3)).write("12345",2).type(KeyCode.ENTER);
    	write("Account Name",2).type(KeyCode.ENTER).type(KeyCode.RIGHT);
    	
    	Set<Node> bsMain=lookup(".button").queryAll();
    	
    	clickOn(companySelector);
    	l = getPopupList().get(1);
    	clickOn(l);
    	
    	sleep(200);
    	
    	Set<Node> bsAll=lookup(".button").queryAll();
    	List<String> buttonTexts = new ArrayList<String>(5);
    	Button bc= null;
    	for (Node node : bsAll) {
			assertTrue(node instanceof Button);
			if (bsMain.contains(node))
				continue;
			buttonTexts.add(((Button) node).getText());
			if(((Button) node).getText().equals(cancelText))
				bc=(Button) node;
		}
    	
    	assertTrue(buttonTexts.contains(okText));
    	assertTrue(buttonTexts.contains(cancelText));
    	assertEquals(2,buttonTexts.size());
    	
    	clickOn(bc);
    	assertEquals(4,tableView.getItems().size());
    	assertEquals(companies.get(0),companySelector.getValue());
    }
    
    @Test
    @CreateCompanies()
    public void CheckMessageUnsavedChangesOK() {
    	clickOn(companySelector);
    	LabeledText l = getPopupList().get(0);
    	clickOn(l);
    	
    	clickOn("#addButton");
    	doubleClickOn(getCell(0, 3)).write("12345",2).type(KeyCode.ENTER);
    	write("Account Name",2).type(KeyCode.ENTER).type(KeyCode.RIGHT);
    	
    	Set<Node> bsMain=lookup(".button").queryAll();
    	
    	clickOn(companySelector);
    	l = getPopupList().get(1);
    	clickOn(l);
    	
    	sleep(200);
    	
    	Set<Node> bsAll=lookup(".button").queryAll();
    	List<String> buttonTexts = new ArrayList<String>(5);
    	Button bc= null;
    	for (Node node : bsAll) {
			assertTrue(node instanceof Button);
			if (bsMain.contains(node))
				continue;
			buttonTexts.add(((Button) node).getText());
			if(((Button) node).getText().equals(okText))
				bc=(Button) node;
		}
    	assertTrue(buttonTexts.contains(okText));
    	assertTrue(buttonTexts.contains(cancelText));
    	assertEquals(2,buttonTexts.size());
    	
    	clickOn(bc);
    	assertEquals(3,tableView.getItems().size());
    	assertEquals(companies.get(1),companySelector.getValue());
    }
    
    
}
