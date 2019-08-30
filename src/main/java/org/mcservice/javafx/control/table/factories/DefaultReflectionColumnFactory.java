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
package org.mcservice.javafx.control.table.factories;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.mcservice.javafx.AnnotationBasedFormatter;
import org.mcservice.javafx.control.table.MemberVariable;
import org.mcservice.javafx.control.table.TableViewColumn;
import org.mcservice.javafx.control.table.TableViewColumnOrder;
import org.mcservice.javafx.control.table.ValidatingTextFieldTableCell;

import com.sun.javafx.property.PropertyReference;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class DefaultReflectionColumnFactory<S> implements ReflectionColumnFactory<S>{
	
	protected static final List<Class<?>>implementedFields = Collections.unmodifiableList(
			Arrays.asList(String.class,Short.class,Short.TYPE,Integer.class,Integer.TYPE,
					Long.class,Long.TYPE,MonetaryAmount.class, Money.class,
					BigDecimal.class,Boolean.class,Boolean.TYPE));
	protected Class<S> referenceClass;
	protected TableView<S> table;
	protected ObjectProperty<String> memoryKeyCode;
	
	public DefaultReflectionColumnFactory() {
	}
	
	@Override
	public MemberVariable<S,?> addTableColumn(Field field, Class<S> referenceClass, TableView<S> table, 
			ObjectProperty<String> memoryKeyCode){
		this.table=table;
		this.referenceClass=referenceClass;
		this.memoryKeyCode=memoryKeyCode;
		if(!implementedFields.contains(field.getType())) {
			throw new RuntimeException(String.format("Type %s not yet implemented.",field.getType().getName()));
		}
		
		MemberVariable<S, ?> result=null;
		
		if(field.getType()==String.class) {
			result=this.<String>createAnnotationBasedValidatingTextField(field,"",false);
		}
		else if(field.getType()==Short.class || field.getType()==Short.TYPE) {
			result=this.<Short>createAnnotationBasedValidatingTextField(field,(short) 0,false);
		}
		else if(field.getType()==Integer.class || field.getType()==Integer.TYPE) {
			result=this.<Integer>createAnnotationBasedValidatingTextField(field,(int) 0,false);
		}
		else if(field.getType()==Long.class || field.getType()==Long.TYPE) {
			result=this.<Long>createAnnotationBasedValidatingTextField(field,(long) 0,false);
		}
		else if(field.getType()==MonetaryAmount.class || field.getType()==Money.class) {
			result=this.<Money>createAnnotationBasedValidatingTextField(field,Money.of(0,"EUR"),true);
		}	
		else if(field.getType()==BigDecimal.class) {
			result=this.<BigDecimal>createAnnotationBasedValidatingTextField(field,new BigDecimal(0),true);
		}
		else if(field.getType()==Boolean.class || field.getType()==Boolean.TYPE) {
			createCheckboxField(field);
		}
		return result;
	}

	private void createCheckboxField(Field field) {
		//Sets a simple checkbox for booleans, this might ignore more complicated checks and so on...
		TableColumn<S,Boolean> actColumn = new TableColumn<S,Boolean>(getColumnName(field));
						
		actColumn.setEditable(field.getAnnotation(TableViewColumn.class).editable());
		actColumn.setCellValueFactory(new PropertyValueFactory<S,Boolean>(field.getName()));
		//TODO Find out why this does not work
		//actColumn.setCellFactory(CheckBoxTableCell.forTableColumn(actColumn));
		
		//FIXME There should be a more elegant solution.
		actColumn.setCellFactory( CheckBoxTableCell.forTableColumn(new Callback<Integer, ObservableValue<Boolean>>() {
			
			class InternalBooleanProperty<K> extends SimpleBooleanProperty {
				private final K item;
				
				public InternalBooleanProperty(K referenceItem){
					this.item=referenceItem;
					this.set(propertyMethod.get(item));
				}
				
			    /**
			     * {@inheritDoc}
			     */
			    @Override
			    public void set(boolean newValue) {
			    	super.set(newValue);
			    	propertyMethod.set(item, newValue);
			    }
			}
			
			private TableView<S> localTable=table;
			private PropertyReference<Boolean> propertyMethod=new PropertyReference<Boolean>(referenceClass,field.getName());
								
			@Override
			public ObservableValue<Boolean> call(Integer param) {
				ObservableBooleanValue newObs=new InternalBooleanProperty<S>(localTable.getItems().get(param));
				return newObs;
			}
		}));
		
		table.getColumns().add(actColumn);
		//FIXME Add correct keyboard behavior.
		//FIXME Add correct treating of editable
	}
	
	private <K> MemberVariable<S, ?> createAnnotationBasedValidatingTextField(
			Field field, K defaultValue, Boolean overwrite) {
		TableColumn<S,K> actColumn = new TableColumn<S,K>(getColumnName(field));
		
		AnnotationBasedFormatter<S,K> formatter = new AnnotationBasedFormatter<S,K>(field,referenceClass,defaultValue);
		
		MemberVariable<S,K> actMember = null;
		if(field.getAnnotation(TableViewColumn.class).editable()) {
			actMember = MemberVariable.<S,K>fromField(field);
			actMember.setTableColumn(actColumn);
			actColumn.setOnEditCommit(actMember);
			actColumn.setEditable(true);
		} else {
			actColumn.setEditable(false);
		}
		
		actColumn.setCellValueFactory(new PropertyValueFactory<S,K>(field.getName()));
		actColumn.setCellFactory(ValidatingTextFieldTableCell.forTableColumn(formatter,
				this.memoryKeyCode,overwrite));
		
		table.getColumns().add(actColumn);
		return actMember;
	}

	public static String getColumnName(Field field) {
		if(field.getAnnotation(TableViewColumn.class).colName().length()>0)
			return field.getAnnotation(TableViewColumn.class).colName();
		return field.getName().replaceAll("(.)([A-Z0-9])", "$1 $2");
	}
	
	public static Comparator<Field> getFieldSorter() {
		return new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				if(o1.equals(o2))
					return 0;
				if(o1.isAnnotationPresent(TableViewColumnOrder.class)) {
					if(o2.isAnnotationPresent(TableViewColumnOrder.class)) {
						return o1.getAnnotation(TableViewColumnOrder.class).value()-
								o2.getAnnotation(TableViewColumnOrder.class).value();
					} else {
						return -1;
					}					
				} else {
					if(o2.isAnnotationPresent(TableViewColumnOrder.class)) {
						return 1;
					} else {
						return DefaultReflectionColumnFactory.getColumnName(o1).
								compareTo(DefaultReflectionColumnFactory.getColumnName(o2));
					}
				}
			}
		};
	}

	@Override
	public boolean checkConstructable(Field field) {
		return implementedFields.contains(field.getType());
	}
}
