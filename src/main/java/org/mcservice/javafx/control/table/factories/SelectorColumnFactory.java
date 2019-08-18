package org.mcservice.javafx.control.table.factories;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

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
				ComboBoxTableCell<S,Object> cell=new ComboBoxTableCell<S,Object>(list);
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
