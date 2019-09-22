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
package org.mcservice.javafx.control.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mcservice.javafx.control.table.TestTypes.getComboPopupList;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mcservice.geldbericht.data.VatType;
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



@Tag("Table")
@Tag("GUI")
class ReflectionTableViewGui1I1S1M1D1VTest extends ApplicationTest{

	ZonedDateTime mockListCreation=null;
	List<TestTypes.Test2I1S1M1D1V> data=null;
	ReflectionTableView<TestTypes.Test2I1S1M1D1V> tableView=null;
	List<VatType> vats=null;
		
	@Override 
	public void start(Stage stage) throws Exception {
		Scene scene = new Scene(new Group());
		
		stage.setTitle("Reflection Table View Test");
        stage.setWidth(500);
        stage.setHeight(100);
        
        tableView = new ReflectionTableView<TestTypes.Test2I1S1M1D1V>(TestTypes.Test2I1S1M1D1V.class);
        tableView.setItems(FXCollections.observableArrayList());
        this.data=this.tableView.getItems();

        this.tableView.getItems().add(new TestTypes.Test2I1S1M1D1V());
        this.tableView.setEditHandler();
        
        tableView.setFixedCellSize(30);
        tableView.prefWidthProperty().bind(stage.widthProperty());
        tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()).add(30));
        
        vats=List.of(new VatType(1L,ZonedDateTime.now(),"Full","19 %", BigDecimal.valueOf(0.19),false,false),
    			new VatType(1L,ZonedDateTime.now(),"Half","12 %", BigDecimal.valueOf(0.12),true,false));
        tableView.getColumnInternalLists().get(TestTypes.Test2I1S1M1D1V.class.getDeclaredField("firstVat")).
        	addAll(vats);
        
		
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
    
    @Tag("Active")
    @Test
    @Disabled
    public void manualTest() {
    	//Wait 1 h, then continue

    	sleep(3600000);
    }
    
    @Tag("Active")
    @Test
    public void checkEditSelect() throws Exception {
    	KeyCodeCombination backslash=new KeyCodeCombination(KeyCode.DIGIT7, ModifierValue.DOWN, ModifierValue.UP, 
    			ModifierValue.UP, ModifierValue.UP, ModifierValue.UP);
    	//Order: firstInt,firstMoney,firstStr,firstVat,firstDay,seconedInt
    	String[] inputStrings=new String[] {"12,34","Num","1.3","-a17.34"};
    	
    	doubleClickOn(getCell(1,0)).write(inputStrings[0]).type(KeyCode.ENTER);
    	doubleClickOn(getCell(2,0)).write(inputStrings[1]).type(KeyCode.ENTER);
    	doubleClickOn(getCell(4,0)).write(inputStrings[2]).type(KeyCode.ENTER);
    	doubleClickOn(getCell(5,0)).write(inputStrings[3]).type(KeyCode.ENTER);
    	assertEquals(-1734,tableView.getItems().get(0).getSecondInt());
    	
    	//Correct firstMoney
    	type(KeyCode.LEFT,4).type(KeyCode.NUMPAD1).write("2,56 USD").type(KeyCode.ENTER);
    	assertEquals(Money.ofMinor(Money.of(BigDecimal.ONE, "USD").getCurrency(), 1256),tableView.getItems().get(0).getFirstMoney());
    	
    	
		//Correct firstStr
    	doubleClickOn(getCell(2,0)).type(KeyCode.DELETE).write("Some String").type(KeyCode.ENTER);
    	assertEquals("Some String",tableView.getItems().get(0).getFirstStr());
    	
    	//Set firstVat
    	clickOn(getCell(3,0)).type(KeyCode.DOWN).type(KeyCode.ENTER);
    	assertEquals(vats.get(0),tableView.getItems().get(0).getFirstVat());
    	
    	//Correct firstDate
    	type(KeyCode.NUMPAD1).type(KeyCode.DELETE,5).write("7.09");
    	assertEquals(7,tableView.getItems().get(0).getFirstDay().getDayOfMonth());
    	assertEquals(9,tableView.getItems().get(0).getFirstDay().getMonthValue());
    	
    	//Delete value, should set it to 0
    	type(KeyCode.ENTER).push(backslash).type(KeyCode.ENTER);
    	assertEquals(0,tableView.getItems().get(0).getSecondInt());
    	
    	for(int i=0;i<tableView.getColumns().size();++i) {
			assertFalse(getCell(i,0).isEditing());
    	}
    }
    
    
    @Test
    public void checkCheckBoxSelectByMouse() throws Exception {
    	doubleClickOn(getCell(3,0)).clickOn(getComboPopupList(this).get(0));
    	assertTrue(vats.contains(tableView.getItems().get(0).getFirstVat()));
    }
    
    
    @Test
    public void checkCheckBoxSelectByMouseEditByKey0() throws Exception {
    	doubleClickOn(getCell(3,0)).type(KeyCode.DOWN).type(KeyCode.ENTER);
    	assertEquals(vats.get(0),tableView.getItems().get(0).getFirstVat());
    }
    
    
    @Test
    public void checkCheckBoxSelectByMouseEditByKey1() throws Exception {
    	doubleClickOn(getCell(3,0)).type(KeyCode.DOWN,2).type(KeyCode.ENTER);
    	assertEquals(vats.get(1),tableView.getItems().get(0).getFirstVat());
    }
    
    
    @Test
    public void checkCheckBoxSelectByMouseEditByKeyCancelEditModeByEsc() throws Exception {
    	doubleClickOn(getCell(3,0)).type(KeyCode.DOWN).type(KeyCode.ESCAPE);
    	assertNull(tableView.getItems().get(0).getFirstVat());
    }
    
    
    @Test
    public void checkCheckBoxSelectByMouseEditByKeyCancelEditModeByMouse() throws Exception {
    	doubleClickOn(getCell(3,0)).type(KeyCode.DOWN,2);
    	clickOn(getCell(2,0));
    	assertNull(tableView.getItems().get(0).getFirstVat());
    }
        
    
    @Test
    public void checkCheckBoxSelectByKeyEditMode0() throws Exception {
    	clickOn(getCell(2,0)).type(KeyCode.RIGHT);
    	type(KeyCode.ENTER).type(KeyCode.DOWN).type(KeyCode.ENTER);
    	assertEquals(vats.get(0),tableView.getItems().get(0).getFirstVat());
    }
    
    
    @Test
    public void checkCheckBoxSelectByKeyEditMode1() throws Exception {
    	clickOn(getCell(2,0)).type(KeyCode.RIGHT);
    	type(KeyCode.ENTER).type(KeyCode.DOWN,2).type(KeyCode.ENTER);
    	assertEquals(vats.get(1),tableView.getItems().get(0).getFirstVat());
    }
    
    
    @Test
    public void checkCheckBoxSelectByKeyCancelEditModeByEsc() throws Exception {
    	clickOn(getCell(2,0)).type(KeyCode.RIGHT);
    	type(KeyCode.ENTER).type(KeyCode.DOWN).type(KeyCode.ESCAPE);
    	assertNull(tableView.getItems().get(0).getFirstVat());
    }
    
    
    @Test
    public void checkCheckBoxSelectByKeyCancelEditModeByMouse() throws Exception {
    	clickOn(getCell(2,0)).type(KeyCode.RIGHT);
    	type(KeyCode.ENTER).type(KeyCode.DOWN);
    	clickOn(getCell(2,0));
    	assertNull(tableView.getItems().get(0).getFirstVat());
    }
}
