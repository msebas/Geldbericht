/**
 * 
 */
package org.mcservice.javafx;


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Cell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * A class like {@link TextFieldTableCell<S,T>} that adds on top a validation
 * via a {@link Callback<T,Boolean>} that is checked to return true. 
 *
 * @param <S> The type of the TableView generic type
 * @param <T> The type of the elements contained within the TableColumn.
 * @since JavaFX 2.2
 */
public class ValidatingTextFieldTableCell<S,T> extends TableCell<S,T> {

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
		return list -> new ValidatingTextFieldTableCell<S,T>(converter,verify,lastTypedKey,textFormatter);
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
	 * @return A {@link Callback} that can be inserted into the
	 *      {@link TableColumn#cellFactoryProperty() cell factory property} of a
	 *      TableColumn, that enables textual editing of the content.
	 */
	public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
			AnnotationBasedFormatter<S,T> textFormatter, final ObjectProperty<String> lastTypedKey) {
		return list -> new ValidatingTextFieldTableCell<S,T>(textFormatter,lastTypedKey);
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
    
	/**
	 * @param converter
	 */
	public ValidatingTextFieldTableCell(StringConverter<T> converter,Callback<T,Boolean> verify,
			ObjectProperty<String> lastTypedKey, TextFormatter<T> textFormatter) {
		this.getStyleClass().add("text-field-table-cell");
		setVerify(verify);
		setConverter(converter);
		this.setLastTypedKey(lastTypedKey);
		this.setTextFormatter(textFormatter);
	}
	
	public ValidatingTextFieldTableCell(AnnotationBasedFormatter<S, T> textFormatter,
			ObjectProperty<String> lastTypedKey) {
		this.getStyleClass().add("text-field-table-cell");
		setVerify(textFormatter.getVerificator());
		setConverter(textFormatter.getValueConverter());
		this.setLastTypedKey(lastTypedKey);
		this.setTextFormatter(textFormatter);
		
	}

	/***************************************************************************
	 *                                                                         *
	 * Properties                                                                  *
	 *                                                                         *
	 **************************************************************************/

	private ObjectProperty<Callback<T,Boolean>> verify = 
			new SimpleObjectProperty<Callback<T,Boolean>>(this, "verify");
	private ObjectProperty<StringConverter<T>> converter =
            new SimpleObjectProperty<StringConverter<T>>(this, "converter");
	private TextField textField;
	private ObjectProperty<String> lastTypedKey = null;
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
	 * @return the textField
	 */
	protected final TextField getTextField() {
		return textField;
	}

	public void setLastTypedKey(ObjectProperty<String> lastTypedKey) {
		this.lastTypedKey = lastTypedKey;
	}

	/**
     * The {@link StringConverter} property.
     * @return the {@link StringConverter} property
     */
    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    /**
     * Sets the {@link StringConverter} to be used in this cell.
     * @param value the {@link StringConverter} to be used in this cell
     */
    public final void setConverter(StringConverter<T> value) {
        converterProperty().set(value);
    }

    /**
     * Returns the {@link StringConverter} used in this cell.
     * @return the {@link StringConverter} used in this cell
     */
    public final StringConverter<T> getConverter() {
        return converterProperty().get();
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
			if (textField == null) {
				textField = CellUtils.createTextField(this, getConverter());
			}
			if(this.textFormatter.get() instanceof AnnotationBasedFormatter) {
				//FIXME This does not work
				//@SuppressWarnings("unchecked")
				//AnnotationBasedFormatter<S,T> actFormatter=((AnnotationBasedFormatter<S,T>) textFormatter.get());
				//actFormatter.setCallback(c -> {this.commitEdit(this.converter.get().fromString(c));});
			}
			
			textField.setTextFormatter(new TextFormatter<T>(getTextFormatter().getValueConverter(),
		    		getTextFormatter().getValue(),getTextFormatter().getFilter()));
			
		    textField.setText(CellUtils.getItemText((Cell<T>)this, getConverter()));
	        this.setText(null);
	        this.setGraphic(textField);
	        if(lastTypedKey.get()!=null) {
	        	textField.setText(textField.getText()+lastTypedKey.get());
	        }

	        // requesting focus so that key input can immediately go into the TextField (see RT-28132)
	        textField.requestFocus();
	        if(lastTypedKey.get()!=null) {
	        	textField.deselect();
	        	textField.end();
	        } else {
	        	textField.selectAll();
	        }
		}
	}

	/** {@inheritDoc} */
	@Override public void cancelEdit() {
		super.cancelEdit();
		CellUtils.cancelEdit(this, getConverter(), null);
	}

	/** {@inheritDoc} */
	@Override public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		CellUtils.updateItem(this, getConverter(), null, null, textField);

		if(!this.verify.get().call(item)) {
			if(!this.getStyleClass().contains("cell-validation-error")) {
				this.getStyleClass().add("cell-validation-error");
			}
		} else {
			this.getStyleClass().remove("cell-validation-error");	
		}
	}


}
