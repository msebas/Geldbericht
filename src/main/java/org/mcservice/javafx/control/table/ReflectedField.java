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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ReflectedField<T> {
	private String fieldName=null;
	private Field field=null;
	private Method setter=null;
	private Method getter=null;
	private Class<?> clazz=null;
	private boolean reflectionDone=false;

	public ReflectedField(String fieldName, Class<?> clazz) {
		this.clazz=clazz;
		this.fieldName=fieldName;
	}
	
	public ReflectedField(Field field, Class<?> clazz) {
		this.clazz=clazz;
		this.field=field;
		this.fieldName=field.getName();
	}
	
    /**
     * Can be used to determine if a setter is present
     *
     * @return {@code true}, if a setter is present
     */
    public boolean isWritable() {
        reflectField();
        return setter != null;
    }


    /**
     * Can be used to determine if a getter is present
     *
     * @return {@code true}, if a getter is present
     */
    public boolean isReadable() {
        reflectField();
        return getter != null;
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    public Field getField() {
    	reflectField();
        return field;
    }
    
    /**
     * Returns the field name.
     *
     * @return name of the field
     */
    public String getName() {
        return fieldName;
    }

    /**
     * Returns the class of the field
     *
     * @return the class of the field
     */
    public Class<?> getFieldClass() {
        return clazz;
    }

    /**
     * Returns the type of the field.
     *
     * @return the type of the field
     */
    public Class<?> getType() {
        reflectField();
        return field.getType();
    }

    /**
     * Call the setter.
     *
     * @param obj The instance to set the field
     * @param value The new value of the field
     * @throws IllegalStateException if the field is read only
     */
    public void set(Object obj, T value) {
        if (!isWritable())
            throw new IllegalStateException("Read only field " + fieldName);
        try {
            setter.invoke(obj, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Call the getter.
     *
     * @param obj The instance to get the field
     * @return The value of the field
     * @throws IllegalStateException if the field is read only
     */
    @SuppressWarnings("unchecked")
	public T get(Object obj) {
        if (!isReadable())
            throw new IllegalStateException("Write only field " + fieldName);
        try {
            return (T) getter.invoke(obj);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return fieldName;
    }

    private void reflectField() {
        if (!reflectionDone) {
        	reflectionDone = true;
    		String upperFieldName=fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    		
    		if(field==null) {
	    		try {
					field=clazz.getDeclaredField(fieldName);
				} catch (NoSuchFieldException | SecurityException e1) {
					throw new RuntimeException(String.format("Missing field %s in class %s"
							,fieldName,clazz.getName()));
				}
    		}
    		
    		try {
    			setter=clazz.getDeclaredMethod("set"+upperFieldName,field.getType());
    			if(!Modifier.isPublic(setter.getModifiers())) {
    				setter=null;
    			}
    		} catch (NoSuchMethodException e) {
    			//Legitimate exception
    		}
    		
    		
    		try {
    			getter=clazz.getDeclaredMethod("get"+upperFieldName);
    			if(!Modifier.isPublic(getter.getModifiers()) || 
    					!getter.getReturnType().equals(field.getType())) {
    				getter=null;
    			}
    		} catch (NoSuchMethodException e) {
    			//Legitimate exception
    		}
    		
    		if(null==getter) {
        		try {
        			getter=clazz.getDeclaredMethod("is"+upperFieldName);
        			if(!Modifier.isPublic(getter.getModifiers()) || 
        					!getter.getReturnType().equals(field.getType())) {
        				getter=null;
        			}
        		} catch (NoSuchMethodException e) {
        			//Legitimate exception
        		}
    		}
        }
    }
}