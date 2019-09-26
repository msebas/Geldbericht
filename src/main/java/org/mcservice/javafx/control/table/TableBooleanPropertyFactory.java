package org.mcservice.javafx.control.table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class TableBooleanPropertyFactory<S> implements Callback<Integer, ObservableValue<Boolean>>, ItemUpdateProvider {
	private TableView<S> localTable;
	private ReflectedField<Boolean> propertyMethod;
	private List<ItemUpdateListener> listeners;
	
	public TableBooleanPropertyFactory(Field field,TableView<S> table, Class<S> referenceClass) {
		localTable=table;
		propertyMethod = new ReflectedField<Boolean>(field,referenceClass);
		listeners=new ArrayList<ItemUpdateListener>();
	}

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
	    	boolean changed=!(propertyMethod.get(item)==newValue);
	    	super.set(newValue);
	    	propertyMethod.set(item, newValue);
	    	callListeners(changed);
	    }
	}

	@Override
	public ObservableValue<Boolean> call(Integer param) {
		ObservableBooleanValue newObs=new InternalBooleanProperty<S>(localTable.getItems().get(param));
		return newObs;
	}
	
	private void callListeners(boolean trueChange) {
		for (ItemUpdateListener itemUpdateListener : listeners) {
			itemUpdateListener.changed(trueChange);
		}
	}

	@Override
	public void addListener(ItemUpdateListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(ItemUpdateListener listener) {
		this.listeners.remove(listener);
	}
}
