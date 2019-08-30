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
package org.mcservice.geldbericht.data.converters;

import org.mcservice.geldbericht.data.Account;

import javafx.util.StringConverter;

public class AccountStringConverter extends StringConverter<Account>{

	@Override
	public String toString(Account account) {
		if(null==account)
			return null;
		return String.format("%s (%s)",account.getAccountName(),account.getAccountNumber());
	}

	@Override
	public Account fromString(String string) {
		throw new RuntimeException("Not Implemented.");
	}
}
