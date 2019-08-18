package org.mcservice.geldbericht.data.converters;

import org.mcservice.geldbericht.data.Company;

import javafx.util.StringConverter;

public class CompanyStringConverter extends StringConverter<Company>{

	@Override
	public String toString(Company company) {
		return String.format("%s (Nr.: %s)",company.getCompanyName(),company.getCompanyNumber());
	}

	@Override
	public Company fromString(String string) {
		throw new RuntimeException("Not Implemented.");
	}
}
