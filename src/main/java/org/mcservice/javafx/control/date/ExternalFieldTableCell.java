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
/**
 * 
 */
package org.mcservice.javafx.control.date;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.mcservice.javafx.control.table.SupportsEndEditCallback;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ExternalFieldTableCell<S,T> extends TableCell<S,T> {
	
	public static class InputNode<K>{
		public final TextField textField;
		public final Supplier<K> valueGetter;
		public final Consumer<K> valueSetter;
		
		public InputNode(TextField inputField, Supplier<K> valueGetter, Consumer<K> valueSetter) {
			super();
			this.textField = inputField;
			this.valueGetter = valueGetter;
			this.valueSetter = valueSetter;
		}
	}
	
	public static <K,L> Callback<TableColumn<K,L>, TableCell<K,L>> forTableColumn(
			Callback<L,InputNode<L>> textFieldFactory, StringConverter<L> converter,
			ObjectProperty<String> lastTypedKey) {
		return list -> new ExternalFieldTableCell<K, L>(textFieldFactory, converter,
				lastTypedKey);
    }
	


	/***************************************************************************
	 *                                                                         *
	 * Internal Constructors                                                   *
	 *                                                                         *
	 **************************************************************************/
	
	protected ExternalFieldTableCell(Callback<T,InputNode<T>> factory, 
			StringConverter<T> converter,ObjectProperty<String> lastTypedKey){
		super();
		this.textFieldFactory=factory;
		setConverter(converter);
		setLastTypedKey(lastTypedKey);
	}

	/***************************************************************************
	 *                                                                         *
	 * Internal Variables                                                      *
	 *                                                                         *
	 **************************************************************************/

	protected Callback<T,InputNode<T>> textFieldFactory;
	protected InputNode<T> inputNode;
	
	/***************************************************************************
	 *                                                                         *
	 * Properties                                                              *
	 *                                                                         *
	 **************************************************************************/

    // --- converter
    private ObjectProperty<StringConverter<T>> converter =
            new SimpleObjectProperty<StringConverter<T>>(this, "converter");
    private ObjectProperty<String> lastTypedKey = null;
    
    public void setLastTypedKey(ObjectProperty<String> value) {
        lastTypedKey=value;
    }

    public ObjectProperty<String> getLastTypedKey() {
        return lastTypedKey;
    }
    
    /**
     * The {@link StringConverter} property to display as text if not editing.
     * @return the {@link StringConverter} property
     */
    public ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    /**
     * Sets the {@link StringConverter} to display as text if not editing.
     * @param value the {@link StringConverter} used by this cell
     */
    public void setConverter(StringConverter<T> value) {
        converterProperty().set(value);
    }

    /**
     * Returns the {@link StringConverter} to display as text if not editing.
     * @return the {@link StringConverter} used by this cell
     */
    public StringConverter<T> getConverter() {
        return converterProperty().get();
    }
    
	/***************************************************************************
	 *                                                                         *
	 * Public API                                                              *
	 *                                                                         *
	 **************************************************************************/
	
	/** {@inheritDoc} */
    @Override public void startEdit() {
        if (! isEditable()
                || ! getTableView().isEditable()
                || ! getTableColumn().isEditable()) {
            return;
        }
        super.startEdit();

        if (isEditing()) {
            if (inputNode == null) {
            	createInputField();
            }

            this.setText(null);
            this.setGraphic(inputNode.textField);
            inputNode.textField.selectAll();
            inputNode.textField.requestFocus();
            
            if(lastTypedKey!=null && lastTypedKey.get()!=null) {
            	inputNode.textField.setText(
            			inputNode.textField.getText()+lastTypedKey.get());    	
            	inputNode.textField.deselect();
            	inputNode.textField.end();
	        }
        }
    }

	/** {@inheritDoc} */
    @Override public void cancelEdit() {
        super.cancelEdit();
        this.setText(getConverter().toString(this.getItem()));
        this.setGraphic(null);
    }

    /** {@inheritDoc} */
    @Override public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
        } else {
            if (isEditing()) {
                cancelEdit();
            }
            setText(getConverter().toString(this.getItem()));
        }
        setGraphic(null);
    }
    
    private void createInputField() {
    	inputNode=textFieldFactory.call(this.getItem());
    	
    	//It is not required that the value is set by the constructor, so be sure
    	inputNode.valueSetter.accept(this.getItem());

        // keyRelease may lead to RT-34685 
        inputNode.textField.setOnAction(event -> {
            this.commitEdit(inputNode.valueGetter.get());
            event.consume();
        });
        inputNode.textField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                this.cancelEdit();
                event.consume();
            }
        });
        
        if(inputNode.textField instanceof SupportsEndEditCallback) {
        	((SupportsEndEditCallback) inputNode.textField).setEndEditCallback( 
        	actText -> {
        		this.commitEdit(inputNode.valueGetter.get());
        	});
        }
	}
}
