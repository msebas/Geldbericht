package org.mcservice.javafx.control.table.factories;

import java.lang.reflect.Field;

import org.mcservice.javafx.control.table.MemberVariable;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TableView;

public interface ReflectionColumnFactory<S> {
	
	public boolean checkConstructable(Field field);
	
	public MemberVariable<S,?> addTableColumn(Field field, Class<S> referenceClass, TableView<S> table,
					ObjectProperty<String> memoryKeyCode);
	
	
	
}
