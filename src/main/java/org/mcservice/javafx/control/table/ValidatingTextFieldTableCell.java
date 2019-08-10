/**
 * 
 */
package org.mcservice.javafx.control.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.mcservice.javafx.AnnotationBasedFormatter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * A class like {@link javafx.scene.control.cell.TextFieldTableCell<S,T>} that adds on top a validation,
 * the ability to set a {@link TextFormatter<T>} for the internal TextField and 
 * a key memory to prevent lost keys on state changes.
 * 
 * The validation is provided via a {@link Callback<T,Boolean>} that is checked 
 * to return true on validation success. 
 * If this validation fails the "cell-validation-error" class is added to 
 * the cell. If the validation does not fail the class is removed if present.
 * The user is responsible to configure an appropriate CSS configuration if 
 * required.
 * 
 * The {@link TextFormatter<T>} has to be copied for each created {@link TextField}.
 * To do this a new TextFormatter is created.
 *  
 * Differing from the original {@link TextFieldTableCell<S,T>} this class opens 
 * its internal {@link TextField} to inheriting classes via a protected getter 
 * method.
 *
 * @param <S> The type of the TableView generic type
 * @param <T> The type of the elements contained within the TableColumn.
 */
public class ValidatingTextFieldTableCell<S,T> extends TextFieldTableCell<S,T> {

	/***************************************************************************
	 *                                                                         *
	 * Static cell factories                                                   *
	 *                                                                         *
	 **************************************************************************/

	/**
	 * Provides a {@link TextField} that allows editing of the cell content when
	 * the cell is double-clicked, or when
	 * {@link TableView#edit(int, javafx.scene.control.TableColumn)} is called.
	 * This method will only  work on {@link TableColumn} instances which are of
	 * type String.
	 *
	 * @param <S> The type of the TableView generic type
	 * @return A {@link Callback} that can be inserted into the
	 *      {@link TableColumn#cellFactoryProperty() cell factory property} of a
	 *      TableColumn, that enables textual editing of the content.
	 */
	public static <S> Callback<TableColumn<S,String>, TableCell<S,String>> forTableColumn(
			Callback<String,Boolean> verify, final ObjectProperty<String> lastTypedKey) {
		return forTableColumn(new DefaultStringConverter(),verify,lastTypedKey,null);
	}

	/**
	 * Provides a {@link TextField} that allows editing of the cell content when
	 * the cell is double-clicked, or when
	 * {@link TableView#edit(int, javafx.scene.control.TableColumn) } is called.
	 * This method will work  on any {@link TableColumn} instance, regardless of
	 * its generic type. However, to enable this, a {@link StringConverter} must
	 * be provided that will convert the given String (from what the user typed
	 * in) into an instance of type T. This item will then be passed along to the
	 * {@link TableColumn#onEditCommitProperty()} callback.
	 *
	 * @param <S> The type of the TableView generic type
	 * @param <T> The type of the elements contained within the TableColumn
	 * @param converter A {@link StringConverter} that can convert the given String
	 *      (from what the user typed in) into an instance of type T.
	 * @return A {@link Callback} that can be inserted into the
	 *      {@link TableColumn#cellFactoryProperty() cell factory property} of a
	 *      TableColumn, that enables textual editing of the content.
	 */
	public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
			final StringConverter<T> converter,final Callback<T,Boolean> verify,
			final ObjectProperty<String> lastTypedKey, final TextFormatter<T> textFormatter) {
		return list -> new ValidatingTextFieldTableCell<S,T>(converter,verify,lastTypedKey,textFormatter,null,false);
	}
	
	/**
	 *
	 * @param <S> The type of the TableView generic type
	 * @param <T> The type of the elements contained within the TableColumn
	 * @return A {@link Callback} that can be inserted into the
	 *      {@link TableColumn#cellFactoryProperty() cell factory property} of a
	 *      TableColumn, that enables textual editing of the content.
	 */
	public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
			AnnotationBasedFormatter<S,T> textFormatter, final ObjectProperty<String> lastTypedKey, 
			final Boolean allwaysOverwrite) {
		return list -> new ValidatingTextFieldTableCell<S,T>(textFormatter,lastTypedKey,allwaysOverwrite);
	}
	
	/***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a TextFieldTableCell that provides a {@link TextField} when put
     * into editing mode that allows editing of the cell content. This method
     * will work on any TableColumn instance, regardless of its generic type.
     * However, to enable this, a {@link StringConverter} must be provided that
     * will convert the given String (from what the user typed in) into an
     * instance of type T. This item will then be passed along to the
     * {@link TableColumn#onEditCommitProperty()} callback.
     *
     * @param converter A {@link StringConverter converter} that can convert
     *      the given String (from what the user typed in) into an instance of
     *      type T.
     */
	public ValidatingTextFieldTableCell(StringConverter<T> converter,Callback<T,Boolean> verify,
			ObjectProperty<String> lastTypedKey, TextFormatter<T> textFormatter, 
			Callback<S,Boolean> editableCallback,Boolean allwaysOverwrite) {
		super(converter);
		setVerify(verify);
		setConverter(converter);
		setLastTypedKey(lastTypedKey);
		setTextFormatter(textFormatter);
		setEditableCallback(editableCallback);
		setAllwaysOverwrite(allwaysOverwrite);
	}
	
	public ValidatingTextFieldTableCell(AnnotationBasedFormatter<S, T> textFormatter,
			ObjectProperty<String> lastTypedKey,Boolean allwaysOverwrite) {
		super(textFormatter.getValueConverter());
		setVerify(textFormatter.getVerificator());
		setLastTypedKey(lastTypedKey);
		setTextFormatter(textFormatter);
		setEditableCallback(textFormatter.getEditableCallback());
		setAllwaysOverwrite(allwaysOverwrite);
	}

	/***************************************************************************
	 *                                                                         *
	 * Properties                                                                  *
	 *                                                                         *
	 **************************************************************************/

	private ObjectProperty<Callback<T,Boolean>> verify = 
			new SimpleObjectProperty<Callback<T,Boolean>>(this, "verify");
	private ObjectProperty<Callback<S,Boolean>> editableCallback = 
			new SimpleObjectProperty<Callback<S,Boolean>>(this, "editableCallback");
	private ObjectProperty<String> lastTypedKey = null;
	private ObjectProperty<Boolean> allwaysOverwrite = 
			new SimpleObjectProperty<Boolean>(this, "allwaysOverwrite");
	private ObjectProperty<TextFormatter<T>> textFormatter =
				new SimpleObjectProperty<TextFormatter<T>>(this, "formatter");
	
	/**
	 * @return the verify
	 */
	public final Callback<T, Boolean> getVerify() {
		return verify.get();
	}
	
	/**
	 * @return the verifyProperty
	 */
	public final ObjectProperty<Callback<T, Boolean>> verifyProperty() {
		return verify;
	}

	/**
	 * @return the editableCallback
	 */
	public final Boolean getAllwaysOverwrite() {
		return allwaysOverwrite.get();
	}
	
	/**
	 * @return the AllwaysOverwriteProperty
	 */
	public final ObjectProperty<Boolean> allwaysOverwriteProperty() {
		return allwaysOverwrite;
	}

	/**
	 * @param verify the AllwaysOverwrite to set
	 */
	public final void setAllwaysOverwrite(Boolean allwaysOverwrite) {
		allwaysOverwriteProperty().set(allwaysOverwrite);
	}
	
	/**
	 * @return the editableCallback
	 */
	public final Callback<S, Boolean> getEditableCallback() {
		return editableCallback.get();
	}
	
	/**
	 * @return the editableCallbackProperty
	 */
	public final ObjectProperty<Callback<S, Boolean>> editableCallbackProperty() {
		return editableCallback;
	}

	/**
	 * @param verify the editableCallback to set
	 */
	public final void setEditableCallback(Callback<S, Boolean> editableCallback) {
		editableCallbackProperty().set(editableCallback);
	}
	
	/**
	 * @return the verify
	 */
	public final TextFormatter<T> getTextFormatter() {
		return textFormatter.get();
	}
	
	/**
	 * @return the verifyProperty
	 */
	public final ObjectProperty<TextFormatter<T>> textFormatterProperty() {
		return textFormatter;
	}
	
	/**
	 * @param textFormatter the new value of the {@link TextFormatter} property
	 */
	public final void setTextFormatter(TextFormatter<T> textFormatter) {
		this.textFormatter.set(textFormatter);
	}

	/**
	 * This method might not work as expected. An exception is thrown if 
	 * the correct text field could not be found.
	 * 
	 * @return the textField
	 */
	public final TextField getTextField() {
		if(!isEditing()) {
			return null;
		}
		Node tmpField=this.getGraphic();
		if (null==tmpField)
			return null;
		if (tmpField instanceof TextField)
			return (TextField) tmpField;
		else {
			Collection<Node> possFields;
			if (tmpField instanceof Pane) {
				possFields=new ArrayList<Node>();
				for (Node tmpNode: ((Pane) tmpField).getChildren()) {
					if(tmpNode instanceof TextField)
						possFields.add(tmpField);
				}
			} else {
				possFields=tmpField.lookupAll(".text-field");
			}
			if(possFields.size()==1) {
				tmpField=possFields.iterator().next();
				if (tmpField instanceof TextField)
					return (TextField) tmpField;
			}
		}
		
		throw new RuntimeException("Could not identify text field.");
	}

	public void setLastTypedKey(ObjectProperty<String> lastTypedKey) {
		this.lastTypedKey = lastTypedKey;
	}

	/**
	 * @param verify the verify to set
	 */
	public final void setVerify(Callback<T, Boolean> verify) {
		verifyProperty().set(verify);
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
			TextField textField=getTextField();
			if(getTextFormatter()!=null) {
				//The default value is passed here only for convenience,
				//it is overwritten one line below.
				textField.setTextFormatter(new TextFormatter<T>(
						getTextFormatter().getValueConverter(),
						getTextFormatter().getValue(),getTextFormatter().getFilter()));
			}

	        if(lastTypedKey.get()!=null) {
	        	if(getAllwaysOverwrite()!=null && getAllwaysOverwrite()) {
	        		textField.setText(lastTypedKey.get());
	        	} else {
	        		textField.setText(textField.getText()+lastTypedKey.get());
	        	}	        	
	        	textField.deselect();
	        	textField.end();
	        }
		}
	}

	/** {@inheritDoc} */
	@Override public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);

		if(!this.verify.get().call(item)) {
			if(!this.getStyleClass().contains("cell-validation-error")) {
				this.getStyleClass().add("cell-validation-error");
			}
		} else {
			this.getStyleClass().remove("cell-validation-error");	
		}
		
		if(getEditableCallback()!=null && this.getTableRow()!=null) {
			this.setEditable(getEditableCallback().call(this.getTableRow().getItem()));
		}
	}


}
