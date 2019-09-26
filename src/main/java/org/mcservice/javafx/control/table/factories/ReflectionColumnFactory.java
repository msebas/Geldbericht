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

import org.mcservice.javafx.control.table.ItemUpdateProvider;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TableView;

public interface ReflectionColumnFactory<S> {
	
	public boolean checkConstructable(Field field);
	
	public ItemUpdateProvider addTableColumn(Field field, Class<S> referenceClass, TableView<S> table,
					ObjectProperty<String> memoryKeyCode);
	
	
	
}
