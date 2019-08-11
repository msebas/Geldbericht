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
import java.math.BigDecimal;
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
import org.mcservice.geldbericht.VatTypeManagerController;
import org.mcservice.geldbericht.data.VatType;
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
import javafx.scene.control.CheckBox;
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


@Tag("GUI")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
//@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class VatTypeManagerTest extends MockedApplicationTest{

	@Retention(RetentionPolicy.RUNTIME)
	private @interface CreateVatTypes {
		   int value() default 3;
		   boolean manyDefault() default false;
		   int disabled() default 0;
		}
	@Retention(RetentionPolicy.RUNTIME)
	private @interface CreateVatType {
		String name() default "Voll";
		String shortName() default "19 %";
		double value() default 0.19;
		boolean insertError() default false;
		boolean defaultValue() default true;
		boolean disabled() default false;
	}
	
	@Mock
	DbAbstractionLayer db;
	
	String okText=javafx.scene.control.ButtonType.OK.getText();
	String cancelText=javafx.scene.control.ButtonType.CANCEL.getText();
	
	ZonedDateTime mockListCreation=null;
	List<VatType> vatTypes=null;
	TableView<VatType> tableView=null;
	ComboBox<VatType> vatTypeSelector=null;
	Label changesLabel=null;
	int allVatTypes=0;
		
	@Override 
	public void start(Stage stage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("VatTypeManager.fxml"));
		VatTypeManagerController controller = new VatTypeManagerController(db);
		fxmlLoader.setController(controller);
		Scene scene = new Scene(fxmlLoader.load());
		stage.setScene(scene);
        stage.show();
    }

	public List<VatType> createVatTypes(int n,boolean manyDefault){
		List<VatType> result = new ArrayList<VatType>(n);
		if(n==0)
			return result;
		for (int j = 0; j < n; j++) {
			result.add(new VatType(String.format("VatType %d",allVatTypes+j),
					String.format("%d",allVatTypes+j),new BigDecimal(0),manyDefault ? j%2==1 : false));
		}
		allVatTypes+=n;
		return result;
	}
	
	@BeforeEach 
	public void initMock(TestInfo testInfo) {
		ArrayList<VatType> act=new ArrayList<VatType>();
		mockListCreation=ZonedDateTime.now();
		if (testInfo.getTestMethod().get()!=null) {
			Method actTest=testInfo.getTestMethod().get();
			for (Annotation annotation : actTest.getAnnotations()) {
				if (annotation.annotationType().equals(CreateVatTypes.class)) {
					List<VatType> res = createVatTypes(((CreateVatTypes) annotation).value(),
							((CreateVatTypes) annotation).manyDefault());
					for (int i = 0; i < ((CreateVatTypes) annotation).disabled(); i++) {
						res.get(i).setDisabledVatType(true);
					}
					act.addAll(res);
					
				}
				
				if (annotation.annotationType().equals(CreateVatType.class)) {
					VatType tmp=new VatType((long) 1,mockListCreation,
							((CreateVatType) annotation).name(),
							((CreateVatType) annotation).shortName(),
							new BigDecimal(((CreateVatType) annotation).value()),
							((CreateVatType) annotation).defaultValue(),
							((CreateVatType) annotation).disabled());
					if(((CreateVatType) annotation).insertError()) {
						tmp.setShortName("Far Too Long");
					}
					act.add(tmp);
				}
			}
		}
		mockListCreation=ZonedDateTime.now();
		when(db.getVatTypes()).thenReturn(act);
		vatTypes=act;
	}
    
    @AfterFXInitBeforeEach
    public void setArgs() {
    	Node tmpNode=lookup("#vatTypeTableView").query();
		if (tmpNode instanceof TableView<?>) {
			@SuppressWarnings("unchecked")
			TableView<VatType> tmpView =(TableView<VatType>) tmpNode;
			tableView=tmpView;
		}
		tmpNode=lookup("#defaultSelector").query();
		if (tmpNode instanceof ComboBox<?>) {
			@SuppressWarnings("unchecked")
			ComboBox<VatType> tmpView =(ComboBox<VatType>) tmpNode;
			vatTypeSelector=tmpView;
		}
		tmpNode=lookup("#changesLabel").query();
		if (tmpNode instanceof Label) {
			@SuppressWarnings("unchecked")
			Label tmpView =(Label) tmpNode;
			changesLabel=tmpView;
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
	
	@Tag("Active")
    @Test
    @Disabled
    @CreateVatTypes
    public void manual() {
    	sleep(3600000);
    }
    
    @Test
    @CreateVatTypes(1)
    public void checkAutofillSingleVatType() {
    	assertTrue(vatTypeSelector.getValue().equals(vatTypes.get(0)));
    	Node t=lookup("#addButton").query();
    	assertTrue(t instanceof Button);
    	assertFalse(((Button) t).isDisabled());
    	
    	t=lookup("#persistButton").query();
    	assertTrue(t instanceof Button);
    	assertFalse(((Button) t).isDisabled());
    	
    	t=lookup("#changesLabel").query();
    	assertTrue(t instanceof Label);
    	assertEquals("Ungespeicherte Änderungen",((Label) t).getText());
    	
    	t=lookup("#persistButton").query();
    	assertTrue(t instanceof Button);
    	assertFalse(((Button) t).isDisabled());
    	t=lookup("#cancelButton").query();
    	
    	clickOn(t);
    	sleep(200);
    	
    	verify(db,times(0)).manageVatTypes(eq(tableView.getItems()),any());
    	assertFalse(tableView.getScene().getWindow().isShowing());
    }

    @Test
    @CreateVatTypes(2)
    public void checkAutofillTwoVatTypesNoChangesPersistAndClose() {
    	assertTrue(vatTypeSelector.getValue()==vatTypes.get(0));
    	
    	Node t=lookup("#persistButton").query();
    	assertTrue(t instanceof Button);
    	assertFalse(((Button) t).isDisabled());
    	
    	((Button) t).setDisable(false);
    	clickOn(t);
    	
    	verify(db).manageVatTypes(eq(tableView.getItems()),any());
    	assertFalse(tableView.getScene().getWindow().isShowing());
    }
    
    @Test
    @CreateVatTypes(value=4, disabled=2)
    public void checkAutofillWithDisabledVatTypes() {
    	assertTrue(vatTypeSelector.getValue()==vatTypes.get(2));
    	
    	Node t=lookup("#persistButton").query();
    	assertTrue(t instanceof Button);
    	assertFalse(((Button) t).isDisabled());
    }
    
    @Tag("Active")
    @Test
    @CreateVatTypes(value=4, disabled=4)
    public void checkAutofillAllDisabledVatTypes() {
    	assertTrue(vatTypeSelector.getValue()==null);
    	
    	Node t=lookup("#persistButton").query();
    	assertTrue(t instanceof Button);
    	assertTrue(((Button) t).isDisabled());
    	
    	for (int i = 0; i < 4; i++) {
			Node graphic=getCell(3,i).getGraphic();
			assertTrue(vatTypes.get(i).isDisabledVatType());
			assertTrue(graphic instanceof CheckBox);
			assertTrue(((CheckBox) graphic).isSelected());
		}
    }
    
    @Test
    @CreateVatTypes(value=4,manyDefault=true)
    public void selectAVatTypeSelectNextVatType() {
    	assertEquals(4,tableView.getItems().size());
    	
    	for (int i = 0; i < vatTypes.size(); i++) {
			assertEquals(i==1,vatTypes.get(i).isDefaultVatType());
		}
    	
    	for (int i = 0; i < vatTypes.size(); i++) {
    		clickOn(vatTypeSelector);
        	LabeledText l = getPopupList().get(i);
        	clickOn(l);
        	
        	assertTrue(vatTypeSelector.getValue()==this.vatTypes.get(i));
        	
        	for (int j = 0; j < vatTypes.size(); j++) {
    			assertEquals(j==i,vatTypes.get(j).isDefaultVatType());
    		}
    	}
    }
    
    @Test
    public void ClickAddButtonFillNewVatType() {
    	clickOn("#addButton");
    	assertEquals(1,tableView.getItems().size());
    	assertEquals("",tableView.getItems().get(0).getName());
    	assertEquals("",tableView.getItems().get(0).getShortName());
    	
    	doubleClickOn(getCell(0, 0)).write("12 %").type(KeyCode.ENTER);
    	write("VatType Name").type(KeyCode.ENTER);
    	write("1.234,567 %").type(KeyCode.ENTER);
    	
    	assertEquals(1,tableView.getItems().size());
    	assertEquals("VatType Name",tableView.getItems().get(0).getName());
    	assertEquals("12 %",tableView.getItems().get(0).getShortName());
    	assertEquals(12.34567,tableView.getItems().get(0).getValue().doubleValue());
    	assertFalse(tableView.getItems().get(0).isDisabledVatType());
    }
    
    
    @Test
    public void EmptyTableClickAddButtonFillNewDisabledVatType() {
    	clickOn("#addButton");
    	assertEquals(1,tableView.getItems().size());
    	assertEquals("",tableView.getItems().get(0).getName());
    	assertEquals("",tableView.getItems().get(0).getShortName());
    	assertEquals(vatTypeSelector.getValue(),tableView.getItems().get(0));
    	assertEquals("1 fehlerhafte Einträge",changesLabel.getText());
    	
    	doubleClickOn(getCell(0, 0)).write("12 %").type(KeyCode.ENTER);
    	write("VatType Name").type(KeyCode.ENTER);
    	write("12 %").type(KeyCode.ENTER);
    	
    	clickOn(getCell(3,0));

    	assertEquals(1,tableView.getItems().size());
    	assertEquals("VatType Name",tableView.getItems().get(0).getName());
    	assertEquals("12 %",tableView.getItems().get(0).getShortName());
    	assertEquals(0.12,tableView.getItems().get(0).getValue().doubleValue());
    	assertTrue(tableView.getItems().get(0).isDisabledVatType());
    }
    
    @Test
    @CreateVatType()
    public void PrefilledTableClickAddButtonFillNewEnabledVatType() {
    	assertEquals(vatTypeSelector.getValue(),tableView.getItems().get(0));
    	assertEquals("Keine Änderungen",changesLabel.getText());
    	
    	clickOn("#addButton");
    	assertEquals(2,tableView.getItems().size());
    	assertEquals("",tableView.getItems().get(1).getName());
    	assertEquals("",tableView.getItems().get(1).getShortName());
    	assertFalse(tableView.getItems().get(1).isDisabledVatType());
    	assertEquals("1 fehlerhafte Einträge",changesLabel.getText());
    	assertEquals(vatTypeSelector.getValue(),tableView.getItems().get(0));
    	
    	doubleClickOn(getCell(0, 1)).write("12 %").type(KeyCode.ENTER);
    	write("VatType Name").type(KeyCode.ENTER);
    	write("12 %").type(KeyCode.ENTER);
    	
    	clickOn(getCell(3,1));
    	clickOn(getCell(3,1));

    	assertEquals(2,tableView.getItems().size());
    	assertEquals("VatType Name",tableView.getItems().get(1).getName());
    	assertEquals("12 %",tableView.getItems().get(1).getShortName());
    	assertEquals(0.12,tableView.getItems().get(1).getValue().doubleValue());
    	assertFalse(tableView.getItems().get(1).isDisabledVatType());
    }
    
    
    @Test
    @CreateVatType(insertError=true)
    public void CorrectVatTypeClickPersistButton() {
    	assertTrue(getCell(0,0).getStyleClass().contains("cell-validation-error"));
    	assertEquals(vatTypeSelector.getValue(),tableView.getItems().get(0));
    	assertEquals("1 fehlerhafte Einträge",changesLabel.getText());
    	
    	Node t=lookup("#persistButton").query();
    	assertTrue(t instanceof Button);
    	assertTrue(((Button) t).isDisabled());
    	
    	
    	doubleClickOn(getCell(1, 0)).write(" Stuff").type(KeyCode.ENTER);
    	
    	assertTrue(((Button) t).isDisabled());
    	
    	type(KeyCode.LEFT).type(KeyCode.LEFT).type(KeyCode.ENTER);
    	type(KeyCode.BACK_SPACE,3);
    	write("12 %").type(KeyCode.ENTER);
    	
    	assertEquals("Ungespeicherte Änderungen",changesLabel.getText());
    	assertFalse(((Button) t).isDisabled());
    	assertEquals("12 %",tableView.getItems().get(0).getShortName());
    	
    	clickOn(t);
    	
    	verify(db).manageVatTypes(eq(tableView.getItems()),any());
    	assertFalse(tableView.getScene().getWindow().isShowing());
    }    
    
}
