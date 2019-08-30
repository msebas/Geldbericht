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
import java.lang.reflect.InvocationTargetException;

import org.mcservice.javafx.control.table.AutoShowComboBoxTableCell;
import org.mcservice.javafx.control.table.MemberVariable;
import org.mcservice.javafx.control.table.TableViewColumn;
import org.mcservice.javafx.control.table.TableViewConverter;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class SelectorColumnFactory<S> implements ReflectionColumnFactory<S>,ColumnFactoryWithInternalList{
	
	ObservableList<Object> optionList;
	
	public SelectorColumnFactory() {
	}
	
	@Override
	public MemberVariable<S,?> addTableColumn(Field field, Class<S> referenceClass, TableView<S> table, 
			ObjectProperty<String> memoryKeyCode){
		
		TableColumn<S,Object> actColumn = new TableColumn<S,Object>(getColumnName(field));
		
		MemberVariable<S,Object> actMember = null;
		if(field.getAnnotation(TableViewColumn.class).editable()) {
			actMember = MemberVariable.<S,Object>fromField(field);
			actMember.setTableColumn(actColumn);
			actColumn.setOnEditCommit(actMember);
			actColumn.setEditable(true);
		} else {
			actColumn.setEditable(false);
		}
		optionList=FXCollections.observableArrayList();
		
		final StringConverter<?> localConverter;
		if(field.isAnnotationPresent(TableViewConverter.class)) {
			try {
				localConverter=(StringConverter<?>) field.getAnnotation(TableViewConverter.class).converter().
						getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		} else {
			localConverter=null;
		}
		
		actColumn.setCellValueFactory(new PropertyValueFactory<S,Object>(field.getName()));
		actColumn.setCellFactory(new Callback<TableColumn<S,Object>, TableCell<S,Object>>() {
			private ObservableList<Object> list=optionList;
			private StringConverter<?> converter=localConverter;
		
			@SuppressWarnings("unchecked")
			@Override
			public TableCell<S, Object> call(TableColumn<S, Object> param) {
				final ComboBoxTableCell<S,Object> cell=new AutoShowComboBoxTableCell<S,Object>(list);
				cell.setConverter((StringConverter<Object>) converter);
				
				return cell;
			}
		});
		
		table.getColumns().add(actColumn);
		return null;
	}


	private static String getColumnName(Field field) {
		return DefaultReflectionColumnFactory.getColumnName(field);
	}

	@Override
	public boolean checkConstructable(Field field) {
		return true;
	}

	@Override
	public ObservableList<Object> getList() {
		return optionList;
	}
}
