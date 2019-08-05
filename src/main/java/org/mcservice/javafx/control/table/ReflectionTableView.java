package org.mcservice.javafx.control.table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mcservice.javafx.AnnotationBasedFormatter;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ReflectionTableView<S> extends TableView<S> {
	
	protected final Class<S> referenceClass;
	private ObjectProperty<String> memoryKeyCode = new SimpleObjectProperty<String>(this,"lastInputMemory");
	
	protected static final List<Class<?>>implementedFields = Collections.unmodifiableList(
			Arrays.asList(String.class,Integer.class,Integer.TYPE));

	public ReflectionTableView(Class<S> tableClass) {
		super();
		referenceClass=tableClass;
		init();
	}

	public ReflectionTableView(ObservableList<S> items, Class<S> tableClass) {
		super(items);
		referenceClass=tableClass;
		init();
	}
	
	private void init(){
		ArrayList<Field> fields = new ArrayList<Field>();
		for(Field field:referenceClass.getDeclaredFields()) {
			if(field.isAnnotationPresent(TableViewColumn.class)) {
				fields.add(field);
			}
		}
		
		fields.sort(new Comparator<Field>() {
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
						return getColumnName(o1).compareTo(getColumnName(o2));
					}
				}
			}
		});
		
		
		for(Field field:fields) {
			if(!implementedFields.contains(field.getType())) {
				throw new RuntimeException("Not yet implemented.");
			}
			if(field.getType()==String.class) {
				TableColumn<S,String> actColumn = new TableColumn<S,String>(getColumnName(field));
				MemberVariable<S,String> actMember = MemberVariable.<S,String>fromField(field);
				AnnotationBasedFormatter<S,String> formatter = new AnnotationBasedFormatter<S,String>(actMember.field,referenceClass,"");
				
				actMember.setTableColumn(actColumn);
				actColumn.setOnEditCommit(actMember);
				
				actColumn.setCellValueFactory(new PropertyValueFactory<S,String>(field.getName()));
				actColumn.setCellFactory(ValidatingTextFieldTableCell.forTableColumn(formatter,this.memoryKeyCode));
				
				this.getColumns().add(actColumn);
			}
			if(field.getType()==Integer.class || field.getType()==Integer.TYPE) {
				TableColumn<S,Integer> actColumn = new TableColumn<S,Integer>(getColumnName(field));
				MemberVariable<S,Integer> actMember = MemberVariable.<S,Integer>fromField(field);
				AnnotationBasedFormatter<S,Integer> formatter = new AnnotationBasedFormatter<S,Integer>(actMember.field,referenceClass,0);
				
				actMember.setTableColumn(actColumn);
				actColumn.setOnEditCommit(actMember);
				
				actColumn.setCellValueFactory(new PropertyValueFactory<S,Integer>(field.getName()));
				actColumn.setCellFactory(ValidatingTextFieldTableCell.forTableColumn(formatter,this.memoryKeyCode));
				
				this.getColumns().add(actColumn);
			}
			
		}
		
	}
	
	public void setEditHandler() {
		this.setOnKeyTyped(event -> {
			String actChar=event.getCharacter();		
			if (actChar!=null && actChar.matches("[\\w \\p{Punct}]")) {
				this.memoryKeyCode.set(actChar);
				@SuppressWarnings("unchecked")
				TablePosition<S, ?> actCell = this.getFocusModel().getFocusedCell();
				this.edit(actCell.getRow(), actCell.getTableColumn());
				this.memoryKeyCode.set(null);
			}
		});
	}
	
	private static String getColumnName(Field field) {
		if(field.getAnnotation(TableViewColumn.class).colName().length()>0)
			return field.getAnnotation(TableViewColumn.class).colName();
		return field.getName().replaceAll("(.)([A-Z0-9])", "$1 $2");
	}

}
