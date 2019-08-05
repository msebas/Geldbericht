package org.mcservice.javafx.control.table;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.validation.Validation;
import javax.validation.Validator;


import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;


public class MemberVariable<S,T> implements EventHandler<CellEditEvent<S,T>>{
	public final Field field;
	public final Method setter;
	public final Method getter;
	private TableColumn<S,T> tableColumn = null;
	private Collection<S> itemsWithErrors=null;
	
	private static Validator validator=Validation.buildDefaultValidatorFactory().getValidator();
	
	private MemberVariable(Field field, Method setter, Method getter){
		this.field=field;
		this.setter=setter;
		this.getter=getter;	
	}
	
	public static <S,T> MemberVariable<S,T> fromName(String fieldName, Class<S> owningClass) throws SecurityException {
		Field field;
		try {
			field=owningClass.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(String.format("Missing field %s in class %s"
					,fieldName,owningClass.getName()));
		}
		return fromField(field); 
	}
	
	public static <S,T> MemberVariable<S,T> fromField(Field field) throws SecurityException {
		Class<?> owningClass = field.getDeclaringClass(); 
		String fieldName=field.getName();
		String upperFieldName=fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		
		try {
			return new MemberVariable<S,T>(field,
					owningClass.getMethod("set"+upperFieldName,field.getType()),
					owningClass.getMethod("get"+upperFieldName));
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(String.format("Missing getter or setter for field %s in class %s.",
					field.getName(),owningClass.getName()));
		}
		
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
	public Collection<S> getItemsWithErrors() {
		return itemsWithErrors;
	}

	/**
	 * @param itemsWithErrors the itemsWithErrors to set
	 */
	public void setItemsWithErrors(Collection<S> itemsWithErrors) {
		this.itemsWithErrors = itemsWithErrors;
	}
	
	@Override
	public void handle(CellEditEvent<S, T> editEvent) {		
		S actItem=editEvent.getRowValue();
		try {
			this.setter.invoke(actItem, editEvent.getNewValue());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			//We cannot handle here any exception, but setters should not throw exceptions and all access or
			//argument violations should fit by getter and setter definition. So just add a new level.
			throw new RuntimeException(e);
		}
		
		if(validator.validate(actItem).size()>0) {
			if (this.itemsWithErrors!=null && !this.itemsWithErrors.contains(actItem)) {
				this.itemsWithErrors.add(actItem);
			}
			if(validator.validateProperty(actItem,this.field.getName()).isEmpty()) {
				tableColumn.getTableView().getSelectionModel().selectNext();
			}
		} else {
			if (this.itemsWithErrors!=null) {
				this.itemsWithErrors.remove(actItem);
			}			
			tableColumn.getTableView().getSelectionModel().selectNext();
		}
	
	}
}