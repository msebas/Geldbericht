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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Validation;
import javax.validation.Validator;

import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;


public class MemberVariable<S,T> implements EventHandler<CellEditEvent<S,T>>, ItemUpdateProvider{
	private ReflectedField<T> field;
	private TableColumn<S,T> tableColumn = null;
	private ObjectProperty<? extends Collection<S>> itemsWithErrors=null;
	private List<ItemUpdateListener> listeners;
	
	private static Validator validator=Validation.buildDefaultValidatorFactory().getValidator();
	
	private MemberVariable(String fieldName, Class<S> clazz){
		field=new ReflectedField<T>(fieldName,clazz);				
		listeners=new ArrayList<ItemUpdateListener>();
	}
	
	private MemberVariable(Field field, Class<S> clazz){
		this.field=new ReflectedField<T>(field,clazz);				
		this.listeners=new ArrayList<ItemUpdateListener>();
	}
	
	public static <S,T> MemberVariable<S,T> fromName(String fieldName, Class<S> owningClass) throws SecurityException {
		return new MemberVariable<S,T>(fieldName,owningClass);
	}
	
	@SuppressWarnings("unchecked")
	public static <S,T> MemberVariable<S,T> fromField(Field field) throws SecurityException {
		Class<S> owningClass = (Class<S>) field.getDeclaringClass(); 
		return new MemberVariable<S,T>(field,owningClass);
		
	}

	public TableColumn<S,T> getTableColumn() {
		return tableColumn;
	}

	public void setTableColumn(TableColumn<S,T> tableColumn) {
		this.tableColumn = tableColumn;
	}
	
	/**
	 * @return the itemsWithErrors
	 */
	public ObjectProperty<? extends Collection<S>> getItemsWithErrors() {
		return itemsWithErrors;
	}

	/**
	 * @param itemsWithErrors the itemsWithErrors to set
	 */
	public void setItemsWithErrors(ObjectProperty<? extends Collection<S>> itemsWithErrors) {
		this.itemsWithErrors = itemsWithErrors;
	}
	
	@Override
	public void handle(CellEditEvent<S, T> editEvent) {		
		S actItem=editEvent.getRowValue();
		boolean trueChange=false;
		try {
			trueChange=!nullEquals(field.get(actItem),editEvent.getNewValue());
			field.set(actItem, editEvent.getNewValue());
		} catch (IllegalArgumentException e) {
			//We cannot handle here any exception, but setters should not throw exceptions and all access or
			//argument violations should fit by getter and setter definition. So just add a new level.
			throw new RuntimeException(e);
		}
		
		if(validator.validate(actItem).size()>0) {
			if (this.itemsWithErrors!=null && !this.itemsWithErrors.get().contains(actItem)) {
				this.itemsWithErrors.get().add(actItem);
			}
			if(validator.validateProperty(actItem,field.getName()).isEmpty()) {
				tableColumn.getTableView().getSelectionModel().selectNext();
			}
		} else {
			if (this.itemsWithErrors!=null) {
				this.itemsWithErrors.get().remove(actItem);
			}			
			tableColumn.getTableView().getSelectionModel().selectNext();
		}
		
		for (ItemUpdateListener itemUpdateListener : listeners) {
			itemUpdateListener.changed(trueChange);
		}

	}
	
	private boolean nullEquals(Object a,Object b) {
		if(a==null || b==null)
			return a==b;
		return a.equals(b);
	}
	
	public void addListener(ItemUpdateListener listener) {
		this.listeners.add(listener);
	}

    public void removeListener(ItemUpdateListener listener) {
		this.listeners.remove(listener);
	}
    
    public Class<?> getType(){
    	return field.getType();
    }
    
    public Field getField() {
        return field.getField();
    }
}
