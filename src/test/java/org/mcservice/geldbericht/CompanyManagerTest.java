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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.testfx.util.BoundsQueryUtils;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;


import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
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
class CompanyManagerTest extends MockedApplicationTest{
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface CreateCompanies {
		   int value() default 3;
		}
	@Retention(RetentionPolicy.RUNTIME)
	private @interface CreateCompany {
		String name();
		String number();
		String appointment();
	}
	@Retention(RetentionPolicy.RUNTIME)
	private @interface EnableManageCompany {}
	
	@Mock
	DbAbstractionLayer db;
	
	ZonedDateTime mockListCreation=null;
	List<Company> companies=null;
	TableView<Company> tableView=null;
		
	@Override 
	public void start(Stage stage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("CompanyManager.fxml"));
		CompanyManagerController controller = new CompanyManagerController(db);
		fxmlLoader.setController(controller);
		Scene scene = new Scene(fxmlLoader.load());
		stage.setScene(scene);
        stage.show();
    }

	@BeforeEach 
	public void initMock(TestInfo testInfo) {
		ArrayList<Company> act=new ArrayList<Company>();
		if (testInfo.getTestMethod().get()!=null) {
			Method actTest=testInfo.getTestMethod().get();
			for (Annotation annotation : actTest.getAnnotations()) {
				if (annotation.annotationType().equals(CreateCompanies.class)) {
					for (int i=0;i<((CreateCompanies) annotation).value();++i) {
						act.add(new Company(String.format("Company Name %d",i),
								String.format("%05d",i),
								String.format("%010d",i)));						
					}
				}
				
				if (annotation.annotationType().equals(CreateCompany.class)) {
					act.add(new Company(((CreateCompany) annotation).name(),
							((CreateCompany) annotation).number(),
							((CreateCompany) annotation).appointment()));
				}
				
				/** FIXME Remove when main GUI tests are implemented
				if (annotation.annotationType().equals(EnableManageCompany.class)) {
					when(db.manageCompanies((List<Company>) any(List.class),any(ZonedDateTime.class))).thenAnswer(
							new Answer<List<Company> >() {
								int c=0;
						        public List<Company> answer(InvocationOnMock invocation) throws Exception {
						            Object[] args = invocation.getArguments();
						            List<Company> called=(List<Company>) args[0];
						            List<Company> result=new ArrayList<Company>();
						            Company.class.getDeclaredField("id").setAccessible(true);
						            for(int i=0;i<called.size();++i) {
							            result.add(i,new Company(called.get(i).getCompanyName(),
											            		 called.get(i).getCompanyNumber(),
											            		 called.get(i).getCompanyBookkeepingAppointment()));
										Company.class.getDeclaredField("id").setInt(result.get(i), c++);
						            }
						            Company.class.getDeclaredField("id").setAccessible(false);
						            return result;
						        }
							});
				}
				*/
			}
		}
		mockListCreation=ZonedDateTime.now();
		
		when(db.getCompanies()).thenReturn(act);
		
	}
    
    @AfterFXInitBeforeEach
    public void setArgs() {
    	Node tmpNode=lookup("#companyTableView").query();
		if (tmpNode instanceof TableView<?>) {
			@SuppressWarnings("unchecked")
			TableView<Company> tmpView =(TableView<Company>) tmpNode;
			List<Company> clist =tmpView.getItems();
			
			companies=clist;
			tableView=tmpView;
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
    
    static Stream<Arguments> columnNumberGetterNameProvider() {
        return Stream.of(
        		Arguments.of(0,"TestName",null,"getCompanyName",true),
        		Arguments.of(0,"T",null,"getCompanyName",false),
        		Arguments.of(1,"TN5",null,"getCompanyNumber",false),
        		Arguments.of(1,"TNmb5",null,"getCompanyNumber",true),
        		Arguments.of(1,"TestNumber","TestN","getCompanyNumber",true),
        		Arguments.of(2,"1234567890",null,"getCompanyBookkeepingAppointment",true),
        		Arguments.of(2,"Appo1ntm3n","13","getCompanyBookkeepingAppointment",false),
        		Arguments.of(2,"Apples01234","01234","getCompanyBookkeepingAppointment",false),
        		Arguments.of(2,"12345678901234","1234567890","getCompanyBookkeepingAppointment",true)
        		);
    }
        
    
    @Test
    @Disabled
    public void manual() {
    	sleep(3600000);
    }
   
    @ParameterizedTest
    @MethodSource("columnNumberGetterNameProvider")
    @CreateCompanies(0)
    public void checkInputToObject(int column,String testString,String resultString,String getter,boolean valid) throws Exception {
    	if(resultString==null)
    		resultString=testString;
    	clickOn("#addButton");
    	doubleClickOn(getCell(column,0)).write(" "+testString+" ").type(KeyCode.ENTER);
    	
    	assertEquals(resultString,Company.class.getMethod(getter).invoke(tableView.getItems().get(0)));
    	
    	verifyThat("#changesLabel",hasText("1 fehlerhafte Einträge"));
    	
    	for(int i=0;i<tableView.getColumns().size();++i) {
    		if (i!=column || !valid)
    			assertTrue(getCell(i,0).getStyleClass().contains("cell-validation-error"));
    		else
    			assertFalse(getCell(i,0).getStyleClass().contains("cell-validation-error"));
    	}
    	assertTrue(lookup("#persistButton").queryButton().isDisabled());
    }
    
    
    @Test
    //@Tag("Active")
    public void checkEditSelect() throws Exception {
    	clickOn("#addButton");
    	KeyCodeCombination ctrlA=new KeyCodeCombination(KeyCode.A, ModifierValue.UP, ModifierValue.DOWN, 
    			ModifierValue.UP, ModifierValue.UP, ModifierValue.UP);
    	KeyCodeCombination backslash=new KeyCodeCombination(KeyCode.DIGIT7, ModifierValue.DOWN, ModifierValue.UP, 
    			ModifierValue.UP, ModifierValue.UP, ModifierValue.UP);
    	String[] inputStrings=new String[] {"Comapny & Söhnė",
    										"Num",
    										"0123456789"};
    	
    	doubleClickOn(getCell(0,0)).write(inputStrings[0], 5).type(KeyCode.ENTER);
    	doubleClickOn(getCell(1,0)).write(inputStrings[1], 5).type(KeyCode.ENTER);
    	doubleClickOn(getCell(2,0)).write(inputStrings[2], 5).type(KeyCode.ENTER);
    	
    	//Correct companyNumberCell
    	clickOn(getCell(1,0)).type(KeyCode.NUMPAD3).type(KeyCode.NUMPAD2)
    			.type(KeyCode.ENTER).type(KeyCode.RIGHT).type(KeyCode.ENTER);
    	
		//Correct companyBookkeepingAppointmentCell
    	type(KeyCode.LEFT).type(KeyCode.DELETE)
    			.push(ctrlA).type(KeyCode.RIGHT)
    			.type(KeyCode.NUMPAD0).type(KeyCode.ENTER);
    	
    	//Correct company name
    	type(KeyCode.LEFT).type(KeyCode.LEFT).push(backslash)
    			.type(KeyCode.UP).type(KeyCode.RIGHT,3).type(KeyCode.P)
    			.type(KeyCode.RIGHT,2).type(KeyCode.BACK_SPACE).type(KeyCode.ENTER);
    	
    	for(int i=0;i<tableView.getColumns().size();++i) {
			assertFalse(getCell(i,0).isEditing());
    	}
    	assertEquals("Company & Söhnė/",tableView.getItems().get(0).getCompanyName());
    	assertEquals("Num32",tableView.getItems().get(0).getCompanyNumber());
    	assertEquals("1234567890",tableView.getItems().get(0).getCompanyBookkeepingAppointment());
    }
    
    @Test
    //@Tag("Active")
    public void checkInputChain() throws Exception {
    	clickOn("#addButton");
    	String[] inputStrings=new String[] {"#Company & Söhnė",
    										"Num32 ",
    										"DIGIT1","234567890"};
    	
    	clickOn(getCell(0,0)).write(inputStrings[0]).type(KeyCode.ENTER)
    	.write(inputStrings[1]).type(KeyCode.ENTER)
    	.type(KeyCode.valueOf(inputStrings[2])).write(inputStrings[3]).type(KeyCode.ENTER);
    	
    	verifyThat("#changesLabel",hasText("Ungespeicherte Änderungen"));
    	assertFalse(lookup("#persistButton").queryButton().isDisabled());
    	for(int i=0;i<tableView.getColumns().size();++i) {
			assertFalse(getCell(i,0).getStyleClass().contains("cell-validation-error"));
    	}
    	assertEquals("#Company & Söhnė",tableView.getItems().get(0).getCompanyName());
    	assertEquals("Num32",tableView.getItems().get(0).getCompanyNumber());
    	assertEquals("1234567890",tableView.getItems().get(0).getCompanyBookkeepingAppointment());
    }
    
    @Test
    @CreateCompanies(0)
    public void checkLabelChangedOnInput() {
    	verifyThat("#changesLabel",hasText("Keine Änderungen"));
    	
    	clickOn("#addButton");
    	doubleClickOn(getCell(1,0)).write("Number").type(KeyCode.ENTER);
    	    	
    	verifyThat("#changesLabel",hasText("1 fehlerhafte Einträge"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0,1 })
    @CreateCompanies(1)
    public void checkColumnsFillTableAfterResize(int i) {
		TableColumn<?,?> resizeCol=tableView.getColumns().get(i);
		double owidth=resizeCol.getWidth();
		Bounds b = BoundsQueryUtils.boundsOnScreen(resizeCol.getStyleableNode());
		Point2D point=new Point2D(b.getMaxX(),b.getMinY()+10);
		moveTo(point);
		press(MouseButton.PRIMARY);
		moveBy(50,0);
		release(MouseButton.PRIMARY);
		
		assertNotEquals(owidth,resizeCol.getWidth());
    	double width=0;
    	for (TableColumn<?,?> col:tableView.getColumns()) {
    		width+=col.getWidth();
		}
    	double tableWidth=tableView.getWidth();
    	assertEquals(width,tableWidth,(width+tableWidth)/200);
	}

    @Test
    @CreateCompanies(0)
    public void checkColumnsFillTableInitial() {
    	double width=0;
    	for (TableColumn<?,?> col:tableView.getColumns()) {
    		width+=col.getWidth();
		}
    	double tableWidth=tableView.getWidth();
    	assertEquals(width,tableWidth,(width+tableWidth)/200);
    }

    @Test
    @CreateCompanies(0)
    public void checkAddCompanyChanges() {
    	assertEquals(0,companies.size());
    	clickOn("#addButton");
    	assertEquals(1,companies.size());
    }
    
    @Test
    @CreateCompanies(2)
    public void checkCancelCallNoChanges() {
    	companies=new ArrayList<Company>(companies);
    	
    	clickOn("#cancelButton");
    	verify(db,times(0)).manageCompanies(anyList(),any(ZonedDateTime.class));
    }
	
    @Test
    @CreateCompanies(2)
    public void checkPersistCallNoChanges() {
    	companies=new ArrayList<Company>(companies);
    	when(db.manageCompanies(anyList(),any(ZonedDateTime.class))).thenReturn(null);
    	
    	clickOn("#persistButton");
    	verify(db,times(1)).manageCompanies(eq(companies), any(ZonedDateTime.class));
    }

    @Test
    @CreateCompany(appointment = "Appointment 1", name = "Name", number = "Number")
    public void checkOne() {
    	assertEquals(1,companies.size());
    	db.getCompanies().containsAll(companies);
    }
}
