package org.mcservice.javafx;

import javafx.util.StringConverter;

public class ToStringConverter extends StringConverter<Object>{

	@Override
	public String toString(Object object) {
		return object.toString();
	}

	@Override
	public Object fromString(String string) {
		throw new RuntimeException("Not Implemented.");
	}


}
