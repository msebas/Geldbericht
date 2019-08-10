package org.mcservice.javafx.control.table;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.mcservice.javafx.AnnotationBasedFormatter;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

public class ReflectionTableView<S> extends TableView<S> {
	
	protected final Class<S> referenceClass;
	private ObjectProperty<String> memoryKeyCode = new SimpleObjectProperty<String>(this,"lastInputMemory");
	private ObjectProperty<Collection<S>> itemsWithErrors = new SimpleObjectProperty<Collection<S>>(this, "itemsWithErrors");
	private List<MemberVariable<S,?>> members = new ArrayList<MemberVariable<S,?>>();
	
	protected static final List<Class<?>>implementedFields = Collections.unmodifiableList(
			Arrays.asList(String.class,Integer.class,Integer.TYPE,MonetaryAmount.class, Money.class,
					BigDecimal.class,Boolean.class,Boolean.TYPE));

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
				throw new RuntimeException(String.format("Type %s not yet implemented.",field.getType().getName()));
			}
			if(field.getType()==String.class) {
				this.<String>createAnnotationBasedValidatingTextField(field,"",false);
			}
			else if(field.getType()==Integer.class || field.getType()==Integer.TYPE) {
				this.<Integer>createAnnotationBasedValidatingTextField(field,0,false);
			}
			else if(field.getType()==MonetaryAmount.class || field.getType()==Money.class) {
				this.<Money>createAnnotationBasedValidatingTextField(field,Money.of(0,"EUR"),true);
			}	
			else if(field.getType()==BigDecimal.class) {
				this.<BigDecimal>createAnnotationBasedValidatingTextField(field,new BigDecimal(0),true);
			}
			else if(field.getType()==Boolean.class || field.getType()==Boolean.TYPE) {
				//Sets a simple checkbox for booleans, this might ignore more complicated checks and so on...
				TableColumn<S,Boolean> actColumn = new TableColumn<S,Boolean>(getColumnName(field));
								
				actColumn.setEditable(field.getAnnotation(TableViewColumn.class).editable());
				actColumn.setCellValueFactory(new PropertyValueFactory<S,Boolean>(field.getName()));
				actColumn.setCellFactory(CheckBoxTableCell.forTableColumn(actColumn));
				
				//This does not work
//				//This adds a listener that toggles the checkbox if space is hit and the box is selected.
//				actColumn.setCellFactory(new Callback<TableColumn<S, Boolean>, TableCell<S, Boolean>>() {
//					Callback<TableColumn<S, Boolean>, TableCell<S, Boolean>> superCallback=
//							CheckBoxTableCell.forTableColumn(actColumn);
//					@Override
//					public TableCell<S, Boolean> call(TableColumn<S, Boolean> param) {
//						TableCell<S, Boolean> result=superCallback.call(param);
//						if(result!=null) {
//							result.setOnKeyReleased(new EventHandler<KeyEvent>() {
//								CheckBoxTableCell<S, Boolean> cell=(CheckBoxTableCell<S, Boolean>) result;
//								@Override
//								public void handle(KeyEvent event) {
//									System.out.println("Happen");
//									if(event.getCode()==KeyCode.SPACE) {
//										Node chk=cell.getGraphic();
//										if (null!=chk && chk instanceof CheckBox) {
//											((CheckBox) chk).fire();
//										}
//									}
//								}
//							});
//						}
//						return result;
//					}					
//				});
				
				this.getColumns().add(actColumn);
			}
		}
		
		for (MemberVariable<S, ?> member : members) {
			member.setItemsWithErrors(itemsWithErrorsProperty());
		}
		
		setItemsWithErrors(FXCollections.observableArrayList());
		setEditable(true);
        getSelectionModel().cellSelectionEnabledProperty().set(true);
	}

	private <K> void createAnnotationBasedValidatingTextField(Field field, K defaultValue, Boolean overwrite) {
		TableColumn<S,K> actColumn = new TableColumn<S,K>(getColumnName(field));
		
		AnnotationBasedFormatter<S,K> formatter = new AnnotationBasedFormatter<S,K>(field,referenceClass,defaultValue);
		
		if(field.getAnnotation(TableViewColumn.class).editable()) {
			MemberVariable<S,K> actMember = MemberVariable.<S,K>fromField(field);
			actMember.setTableColumn(actColumn);
			actColumn.setOnEditCommit(actMember);
			members.add(actMember);
			actColumn.setEditable(true);
		} else {
			actColumn.setEditable(false);
		}
		
		actColumn.setCellValueFactory(new PropertyValueFactory<S,K>(field.getName()));
		actColumn.setCellFactory(ValidatingTextFieldTableCell.forTableColumn(formatter,
				this.memoryKeyCode,overwrite));
		
		this.getColumns().add(actColumn);
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ReflectionTableView<?> valueOf(String value) throws ClassNotFoundException{
		return new ReflectionTableView(Class.forName(value));
	}

	public final void setItemsWithErrors(Collection<S> value) { 
		itemsWithErrorsProperty().set(value); 
	}
    
	public final Collection<S> getItemsWithErrors() {
    	return itemsWithErrors.get(); 
    }
    
    public final ObjectProperty<Collection<S>> itemsWithErrorsProperty() {
    	return itemsWithErrors; 
    }
    
    public void addEditCommitListener(ItemUpdateListener listener) {
    	for(MemberVariable<S, ?> m:this.members)
    		m.addListener(listener);
	}

    public void removeEditCommitListener(ItemUpdateListener listener) {
    	for(MemberVariable<S, ?> m:this.members)
    		m.removeListener(listener);
	}

}
