package org.mcservice.javafx.control.date;

import java.lang.reflect.Field;
import java.time.LocalDate;

import org.mcservice.javafx.control.table.MemberVariable;
import org.mcservice.javafx.control.table.TableViewColumn;
import org.mcservice.javafx.control.table.factories.DefaultReflectionColumnFactory;
import org.mcservice.javafx.control.table.factories.ReflectionColumnFactory;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

public class DayMonthFieldColumnFactory<S> implements ReflectionColumnFactory<S>{
		
	public DayMonthFieldColumnFactory() {
	}
	
	@Override
	public MemberVariable<S,?> addTableColumn(Field field, Class<S> referenceClass, TableView<S> table, 
			ObjectProperty<String> memoryKeyCode){
		
		TableColumn<S,LocalDate> actColumn = new TableColumn<S,LocalDate>(getColumnName(field));
		
		StringConverter<LocalDate> formatter = new DayMonthConverter();
				
		MemberVariable<S,LocalDate> actMember = null;
		if(field.getAnnotation(TableViewColumn.class).editable()) {
			actMember = MemberVariable.<S,LocalDate>fromField(field);
			actMember.setTableColumn(actColumn);
			actColumn.setOnEditCommit(actMember);
			actColumn.setEditable(true);
		} else {
			actColumn.setEditable(false);
		}
		
		actColumn.setCellValueFactory(new PropertyValueFactory<S,LocalDate>(field.getName()));
		actColumn.setCellFactory(ExternalFieldTableCell.<S,LocalDate>forTableColumn(
				value -> {
					DayMonthField localIn=new DayMonthField();
					return new ExternalFieldTableCell.InputNode<LocalDate>(localIn, localIn, localIn);
				},
				formatter,memoryKeyCode));
		
		table.getColumns().add(actColumn);
		return actMember;
	}
	
	private static String getColumnName(Field field) {
		return DefaultReflectionColumnFactory.getColumnName(field);
	}

	@Override
	public boolean checkConstructable(Field field) {
		return field.getType()==LocalDate.class;
	}
}
