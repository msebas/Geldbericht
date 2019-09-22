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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mcservice.javafx.control.table.factories.ColumnFactoryWithInternalList;
import org.mcservice.javafx.control.table.factories.DefaultReflectionColumnFactory;
import org.mcservice.javafx.control.table.factories.ReflectionColumnFactory;

import javafx.scene.control.TablePosition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class ReflectionTableView<S> extends TableView<S> {
	
	protected final Class<S> referenceClass;
	private ObjectProperty<String> memoryKeyCode = new SimpleObjectProperty<String>(this,"lastInputMemory");
	private ObjectProperty<Collection<S>> itemsWithErrors = new SimpleObjectProperty<Collection<S>>(this, "itemsWithErrors");
	private List<MemberVariable<S,?>> members = new ArrayList<MemberVariable<S,?>>();
	private Map<Field,ObservableList<Object>> columnInternalLists = new HashMap<Field,ObservableList<Object>>();

	public ReflectionTableView(Class<S> tableClass) {
		super();
		referenceClass = tableClass;
		init();
	}

	public ReflectionTableView(ObservableList<S> items, Class<S> tableClass) {
		super(items);
		referenceClass = tableClass;
		init();
	}
	
	private void init(){
		ArrayList<Field> fields = new ArrayList<Field>();
		for(Field field:referenceClass.getDeclaredFields()) {
			if(field.isAnnotationPresent(TableViewColumn.class)) {
				fields.add(field);
			}
		}
		
		fields.sort(DefaultReflectionColumnFactory.getFieldSorter());
		
		for(Field field:fields) {
			try {
				@SuppressWarnings("unchecked")
				ReflectionColumnFactory<S> factory = field.getAnnotation(TableViewColumn.class).
							fieldGenerator().getDeclaredConstructor().newInstance();
				
				if(!factory.checkConstructable(field)) {
					throw new RuntimeException(String.format("Type %s is not supported by factory class"
							+ " %s.",field.getType().getName(),factory.getClass().getName()));
				}
				
				MemberVariable<S, ?> member=factory.addTableColumn(field, referenceClass, this, memoryKeyCode);
				if(member!=null)
					members.add(member);
				
				if(factory instanceof ColumnFactoryWithInternalList) {
					columnInternalLists.put(field,((ColumnFactoryWithInternalList) factory).getList());
				}
				
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		
		for (MemberVariable<S, ?> member : members) {
			member.setItemsWithErrors(itemsWithErrorsProperty());
		}
		
		setItemsWithErrors(FXCollections.observableArrayList());
		setEditable(true);
        getSelectionModel().cellSelectionEnabledProperty().set(true);
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

	public Map<Field,ObservableList<Object>> getColumnInternalLists() {
		return columnInternalLists;
	}

}
