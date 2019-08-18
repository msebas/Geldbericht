package org.mcservice.geldbericht.data.converters;

import org.mcservice.geldbericht.data.VatType;

import javafx.util.StringConverter;

public class VatTypeStringConverter extends StringConverter<VatType>{

	@Override
	public String toString(VatType vatType) {
		return String.format("%s (%.2f%%)",vatType.getName(),vatType.getValue());
	}

	@Override
	public VatType fromString(String string) {
		throw new RuntimeException("Not Implemented.");
	}
}
