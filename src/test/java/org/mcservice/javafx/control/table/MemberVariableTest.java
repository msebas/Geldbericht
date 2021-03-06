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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.reset;

import java.util.ArrayList;
import java.util.Collection;

import javax.validation.constraints.Max;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mcservice.javafx.control.table.MemberVariable;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;

@Tag("Table")
@ExtendWith(MockitoExtension.class)
class MemberVariableTest{
	
	@BeforeEach
	public void setUp() {
		//This sets up the JavaFX environment
		@SuppressWarnings("unused")
		JFXPanel dummy=new JFXPanel();
	}
	
	public class allFine{
		@Max(4)
		private int myMember;
		@Max(0)
		public int otherMember=-1;
		public int getMyMember() {return myMember;}
		public void setMyMember(int myMember) {this.myMember = myMember;}
	}
        
	@Tag("Active")
	@Test
    public void constructorTests() throws Exception {
		assertEquals(allFine.class.getDeclaredField("myMember"),
    			MemberVariable.fromName("myMember",allFine.class).getField());
    }
    
    @Test
    public void handlePassThrough() throws Exception {
    	MemberVariable<allFine, Integer> act = MemberVariable.fromName("myMember",allFine.class);
    	@SuppressWarnings("unchecked")
		TableColumn<allFine,Integer> testColumn = mock(TableColumn.class);
    	@SuppressWarnings("unchecked")
		TableView<allFine> testView = mock(TableView.class);
    	@SuppressWarnings("unchecked")
		TableViewSelectionModel<allFine> testSelectionModel = mock(TableViewSelectionModel.class);
		    	
    	doReturn(testView).when(testColumn).getTableView();
    	doReturn(testSelectionModel).when(testView).getSelectionModel();
    	
    	allFine item=mock(allFine.class);
    	@SuppressWarnings("unchecked")
		CellEditEvent<allFine,Integer> testEvent=mock(CellEditEvent.class);
    	when(testEvent.getRowValue()).thenReturn(item);
    	when(testEvent.getNewValue()).thenReturn(Integer.valueOf(4));
    	
    	act.setTableColumn(testColumn);
    	
    	act.handle(testEvent);
    	
    	verify(testEvent).getRowValue();
    	verify(item).setMyMember(Integer.valueOf(4));
    	verify(testColumn).getTableView();
    	verify(testView).getSelectionModel();
    	verify(testSelectionModel).selectNext();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void handleError() throws Exception {
    	MemberVariable<allFine, Integer> act = MemberVariable.fromName("myMember",allFine.class);
    	TableColumn<allFine,Integer> testColumn = mock(TableColumn.class);
		TableView<allFine> testView = mock(TableView.class);
		TableViewSelectionModel<allFine> testSelectionModel = mock(TableViewSelectionModel.class);
		    	
    	doReturn(testView).when(testColumn).getTableView();
    	doReturn(testSelectionModel).when(testView).getSelectionModel();
    	
     	ObjectProperty<Collection<allFine>> errorList = new SimpleObjectProperty<Collection<allFine>>(this, "itemsWithErrors");
     	errorList.set(new ArrayList<allFine>());
    	
    	allFine item=new allFine();
    	item.setMyMember(0);
    	
		CellEditEvent<allFine,Integer> testEvent=mock(CellEditEvent.class);
    	when(testEvent.getRowValue()).thenReturn(item);
    	when(testEvent.getNewValue()).thenReturn(Integer.valueOf(5));
    	
    	act.setTableColumn(testColumn);
    	act.setItemsWithErrors(errorList);
    	reset(testSelectionModel);

    	//Check if error mark is set and the next field not selected.
    	act.handle(testEvent);
    	
    	verify(testEvent, times(1)).getRowValue();
    	verify(testSelectionModel, never()).selectNext();
    	assertTrue(errorList.get().contains(item));
    	assertEquals(5,item.getMyMember());
    	
    	
    	//Check if the item is not added twice to the errorList
    	mock(CellEditEvent.class);
    	when(testEvent.getRowValue()).thenReturn(item);
    	when(testEvent.getNewValue()).thenReturn(Integer.valueOf(7));
    	act.handle(testEvent);
    	
    	verify(testSelectionModel, never()).selectNext();
    	assertTrue(errorList.get().contains(item));
    	assertEquals(1,errorList.get().size());
    	assertEquals(7,item.getMyMember());
    	
    	//Check if the item is removed correctly if the error is removed
    	mock(CellEditEvent.class);
    	when(testEvent.getRowValue()).thenReturn(item);
    	when(testEvent.getNewValue()).thenReturn(Integer.valueOf(3));
    	act.handle(testEvent);
    	
    	assertFalse(errorList.get().contains(item));
    	verify(testSelectionModel).selectNext();
    	assertEquals(3,item.getMyMember());
    	
    	//Check getters to remove error messages
    	assertEquals(errorList,act.getItemsWithErrors());
    	assertEquals(testColumn,act.getTableColumn());
    	
    	//Check if select next is called when item contains errors, but field is correct
    	mock(CellEditEvent.class);
    	when(testEvent.getRowValue()).thenReturn(item);
    	when(testEvent.getNewValue()).thenReturn(Integer.valueOf(2));
    	reset(testSelectionModel);
    	item.otherMember=1;
    	act.handle(testEvent);
    	
    	verify(testSelectionModel, times(1)).selectNext();
    	assertTrue(errorList.get().contains(item));
    	assertEquals(2,item.getMyMember());
    }
    
    @Test
    public void checkExceptionReplacement() throws Exception {
    	MemberVariable<allFine, String> act = MemberVariable.fromName("myMember",allFine.class);
    	
    	allFine item=mock(allFine.class);
    	@SuppressWarnings("unchecked")
		CellEditEvent<allFine,String> testEvent=mock(CellEditEvent.class);
    	when(testEvent.getRowValue()).thenReturn(item);
    	when(testEvent.getNewValue()).thenReturn("4");
    	
    	assertThrows(RuntimeException.class,() -> act.handle(testEvent));
    	
    	verify(testEvent).getRowValue();
    	
    }
    
}
