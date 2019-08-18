package org.mcservice.geldbericht.data.converters;

import org.mcservice.geldbericht.data.Account;

import javafx.util.StringConverter;

public class AccountStringConverter extends StringConverter<Account>{

	@Override
	public String toString(Account account) {
		return String.format("%s (%s)",account.getAccountName(),account.getAccountNumber());
	}

	@Override
	public Account fromString(String string) {
		throw new RuntimeException("Not Implemented.");
	}
}
