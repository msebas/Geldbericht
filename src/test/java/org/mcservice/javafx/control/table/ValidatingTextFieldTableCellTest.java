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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.ArgumentMatchers.any;

import java.lang.reflect.Field;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mcservice.javafx.AnnotationBasedFormatter;
import org.mcservice.javafx.control.table.ValidatingTextFieldTableCell;
import org.mockito.junit.jupiter.MockitoExtension;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

@Tag("Table")
@ExtendWith(MockitoExtension.class)
class ValidatingTextFieldTableCellTest{
		
	String outer="A String";
	Callback<String,Boolean> verifyCallback = spy(new Callback<String,Boolean>() {
		@Override
		public Boolean call(String param) {
			if (param!=null && param.equals(outer))
					return true;
			return false;
		}
	});
	SimpleObjectProperty<String> keyProperty=new SimpleObjectProperty<String>("");
	
	@BeforeEach
	public void setUp() {
		//This sets up the JavaFX environment
		@SuppressWarnings("unused")
		JFXPanel dummy=new JFXPanel();
	}
	
    @Test
    public void checkFactoryMethods() throws Exception {
    	TableCell <?,String> cell;
    	ValidatingTextFieldTableCell <?,String> advCell;
    	cell=ValidatingTextFieldTableCell.forTableColumn(verifyCallback,keyProperty).call(null);
    	
    	assertTrue(cell instanceof ValidatingTextFieldTableCell);
		advCell=(ValidatingTextFieldTableCell <?,String>) cell;
    	Field lastTypedKeyField=advCell.getClass().getDeclaredField("lastTypedKey");
    	lastTypedKeyField.setAccessible(true);
    	assertTrue(advCell.converterProperty().get() instanceof DefaultStringConverter);
    	assertEquals(verifyCallback,advCell.verifyProperty().get());
    	assertEquals(null,advCell.textFormatterProperty().get());
    	assertEquals(keyProperty,lastTypedKeyField.get(advCell));
    	
    	@SuppressWarnings("unchecked")
		StringConverter<String> mockedConverter=mock(StringConverter.class);
    	@SuppressWarnings("unchecked")
		TextFormatter<String> mockedFormatter=mock(TextFormatter.class);
    	cell=ValidatingTextFieldTableCell.forTableColumn(
    			mockedConverter,verifyCallback,keyProperty,mockedFormatter).call(null);
    	
    	assertTrue(cell instanceof ValidatingTextFieldTableCell);
		advCell=(ValidatingTextFieldTableCell <?,String>) cell;
		assertEquals(mockedConverter,advCell.converterProperty().get());
		assertEquals(verifyCallback,advCell.verifyProperty().get());
		assertEquals(verifyCallback,advCell.getVerify());
    	assertEquals(mockedFormatter,advCell.textFormatterProperty().get());
    	assertEquals(keyProperty,lastTypedKeyField.get(advCell));
    	
    	@SuppressWarnings("unchecked")
		AnnotationBasedFormatter<?,String> fmt=mock(AnnotationBasedFormatter.class);
    	doReturn(verifyCallback).when(fmt).getVerificator();
    	doReturn(mockedConverter).when(fmt).getValueConverter();
    	
    	cell=ValidatingTextFieldTableCell.forTableColumn(fmt,keyProperty,false).call(null);
    	advCell=(ValidatingTextFieldTableCell <?,String>) cell;
    	assertEquals(mockedConverter,advCell.converterProperty().get());
    	assertEquals(verifyCallback,advCell.verifyProperty().get());
    	assertEquals(fmt,advCell.textFormatterProperty().get());
    	assertEquals(keyProperty,lastTypedKeyField.get(advCell));
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void checkCancelEdit(){
    	ValidatingTextFieldTableCell <String,String> advCell;
    	advCell=(ValidatingTextFieldTableCell<String, String>) 
    			spy((ValidatingTextFieldTableCell<?,String>) 
    					ValidatingTextFieldTableCell.forTableColumn(verifyCallback,keyProperty).call(null));
    	
    	advCell.cancelEdit();
    	
    	verify((TableCell<?,?>) advCell).cancelEdit();
    	//We cannot mock the static call easily here, so just check if the 
    	//static method did what it should have done...
    	verify(advCell).setText("");
    	verify(advCell).setGraphic(null);
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void checkUpdateItem(){
    	ValidatingTextFieldTableCell <String,String> advCell;
    	advCell=(ValidatingTextFieldTableCell<String, String>) 
    			spy((ValidatingTextFieldTableCell<?,String>) 
    					ValidatingTextFieldTableCell.forTableColumn(verifyCallback,keyProperty).call(null));
    	advCell.getStyleClass().clear();
    	
    	advCell.updateItem("New Text",false);
    	
    	verify((TableCell<?,String>) advCell).setItem("New Text");
    	//We cannot mock the static call easily here, so just check if the 
    	//static method did what it should have done...
    	verify(advCell).setText("New Text");
    	verify(advCell).setGraphic(null);
    	
    	verify(verifyCallback).call("New Text");
    	assertTrue(advCell.getStyleClass().contains("cell-validation-error"));
    	
    	reset(verifyCallback);
    	advCell.updateItem("Newer Text",false);
    	verify(verifyCallback).call("Newer Text");
    	assertTrue(advCell.getStyleClass().contains("cell-validation-error"));
    	assertEquals(1,advCell.getStyleClass().size());
    	
    	reset(verifyCallback);
    	advCell.updateItem(outer,false);
    	assertFalse(advCell.getStyleClass().contains("cell-validation-error"));
    }
    
    @Test
    public void checkStartEdit(){
    	ValidatingTextFieldTableCell <String,String> advCell;
    	DefaultStringConverter mockedConverter = mock(DefaultStringConverter.class);
    	when(mockedConverter.toString(any())).thenReturn("Start");
    	advCell=spy(new ValidatingTextFieldTableCell<String, String>(
    				mockedConverter,verifyCallback,keyProperty,null,null,false));
    	TableColumn<String,String> testColumn = spy(new TableColumn<String,String>("Name"));
    	TableView<String> testView = spy(new TableView<String>());
    	testView.getColumns().add(testColumn);
    	testView.setEditable(true); testColumn.setEditable(true);
    	testColumn.setCellFactory(node -> advCell);
    	testColumn.setCellValueFactory(node -> new SimpleObjectProperty<String>(node.getValue()));
    	
    	keyProperty.set(null);
    	
    	advCell.updateTableColumn(testColumn);
    	advCell.updateTableView(testView);
    	doReturn(Boolean.valueOf(true)).when(advCell).isEditing();
    	
    	advCell.setEditable(false);
    	
    	//Check return if not editable
    	advCell.startEdit();
    	verify(advCell,never()).isEditing();
    	
    	verify(advCell,never()).isEditing();
    	
    	advCell.setEditable(true);
    	advCell.startEdit();
    	
    	verify(advCell,atLeastOnce()).isEditing();
    	
    	Node fieldNode = advCell.getGraphic();
    	assertTrue(fieldNode instanceof TextField);
    	TextField field = (TextField) fieldNode;
    	
    	assertEquals("Start",field.getText());
    	assertNull(advCell.getText());
    	assertEquals(field.getText(),field.getSelectedText());
    	assertNull(field.getTextFormatter());
    	
    	
    	
    	TextFormatter<String> actFmt = new TextFormatter<String>(mockedConverter,"DefaultValue",
    			new UnaryOperator<Change>() {
					@Override
					public Change apply(Change t) {
						return t;
					}
				});
    	
    	advCell.setTextFormatter(actFmt);
    	keyProperty.set("Stop");
    	advCell.startEdit();
    	//assertEquals("StartStop",field.getText());
    	assertEquals("",field.getSelectedText());
    	assertEquals(field.getCaretPosition(),field.getText().length());
    	//Formatters can only be used on one field, so check if this one was copied
    	assertFalse(actFmt==field.getTextFormatter());
    	assertEquals(actFmt.getValueConverter(),field.getTextFormatter().getValueConverter());
    	assertEquals(actFmt.getFilter(),field.getTextFormatter().getFilter());
    }
}
