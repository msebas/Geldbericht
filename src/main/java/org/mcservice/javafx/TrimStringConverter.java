package org.mcservice.javafx;

import javafx.util.StringConverter;

/**
 * {@link StringConverter} implementing trimmed {@link String} values.
 */
public class TrimStringConverter extends StringConverter<String> {

	/** {@inheritDoc} */
    @Override 
    public String toString(String value) {
    	if(value==null)
    		return "";
    	else
    		return value.trim();
    }

    /** {@inheritDoc} */
    @Override 
    public String fromString(String value) {
        return value;
    }

}
