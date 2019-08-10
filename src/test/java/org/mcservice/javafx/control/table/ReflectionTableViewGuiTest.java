package org.mcservice.javafx.control.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mcservice.javafx.control.table.ReflectionTableView;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.ModifierValue;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


@Tag("Active")
@Tag("Table")
@Tag("GUI")
class ReflectionTableViewGuiTest extends ApplicationTest{

	ZonedDateTime mockListCreation=null;
	List<TestTypes.Test3S> companies=null;
	ReflectionTableView<TestTypes.Test3S> tableView=null;
		
	@Override 
	public void start(Stage stage) throws Exception {
		Scene scene = new Scene(new Group());
		
		stage.setTitle("Reflection Table View Test");
        stage.setWidth(500);
        stage.setHeight(300);
        
        tableView = new ReflectionTableView<TestTypes.Test3S>(TestTypes.Test3S.class);
        tableView.setItems(FXCollections.observableArrayList());
        this.companies=this.tableView.getItems();

        this.tableView.getItems().add(new TestTypes.Test3S());
        this.tableView.setEditHandler();
        
        tableView.setFixedCellSize(30);
        tableView.prefWidthProperty().bind(stage.widthProperty());
        tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()).add(30));
		
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().add(tableView);
 
        ((Group) scene.getRoot()).getChildren().addAll(vbox);
		
        stage.setScene(scene);
        stage.show();
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
    
    @Test
    @Disabled
    public void manualTest() {
    	//Wait 1 h, then continue

    	sleep(3600000);
    }
    
    static Stream<Arguments> columnNumberGetterNameProvider() {
        return Stream.of(
        		Arguments.of(0,"TestName",null,"getFirstString",true),
        		Arguments.of(0,"T",null,"getFirstString",false),
        		Arguments.of(1,"TN5",null,"getSecondString",false),
        		Arguments.of(1,"TNmb5",null,"getSecondString",true),
        		Arguments.of(1,"TestNumber","TestN","getSecondString",true),
        		Arguments.of(2,"1234567890",null,"getThirdString",true),
        		Arguments.of(2,"Appo1ntm3n","13","getThirdString",false),
        		Arguments.of(2,"Apples01234","01234","getThirdString",false),
        		Arguments.of(2,"12345678901234","1234567890","getThirdString",true)
        		);
    }
        
    @ParameterizedTest
    @MethodSource("columnNumberGetterNameProvider")
    public void checkInputToObject(int column,String testString,String resultString,String getter,boolean valid) throws Exception {
    	if(resultString==null)
    		resultString=testString;
    	doubleClickOn(getCell(column,0)).write(" "+testString+" ").type(KeyCode.ENTER);
    	
    	assertEquals(resultString,TestTypes.Test3S.class.getMethod(getter).invoke(tableView.getItems().get(0)));
    	
    	for(int i=0;i<tableView.getColumns().size();++i) {
    		if (i!=column)
    			assertTrue(getCell(i,0).getStyleClass().contains("cell-validation-error"));
    		else if (!valid)
    			assertTrue(getCell(i,0).getStyleClass().contains("cell-validation-error"));
    		else
    			assertFalse(getCell(i,0).getStyleClass().contains("cell-validation-error"));
    	}
    }
    
    
    @Test
    public void checkEditSelect() throws Exception {
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
    	assertEquals("Company & Söhnė/",tableView.getItems().get(0).getFirstString());
    	assertEquals("Num32",tableView.getItems().get(0).getSecondString());
    	assertEquals("1234567890",tableView.getItems().get(0).getThirdString());
    }
    
    @Test
    public void checkInputChain() throws Exception {
    	String[] inputStrings=new String[] {"#Company & Söhnė",
    										"Num32 ",
    										"DIGIT1","234567890"};
    	
    	clickOn(getCell(0,0)).write(inputStrings[0]).type(KeyCode.ENTER)
    	.write(inputStrings[1]).type(KeyCode.ENTER)
    	.type(KeyCode.valueOf(inputStrings[2])).write(inputStrings[3]).type(KeyCode.ENTER);
    	
    	for(int i=0;i<tableView.getColumns().size();++i) {
			assertFalse(getCell(i,0).getStyleClass().contains("cell-validation-error"));
    	}
    	assertEquals("#Company & Söhnė",tableView.getItems().get(0).getFirstString());
    	assertEquals("Num32",tableView.getItems().get(0).getSecondString());
    	assertEquals("1234567890",tableView.getItems().get(0).getThirdString());
    }    
}
