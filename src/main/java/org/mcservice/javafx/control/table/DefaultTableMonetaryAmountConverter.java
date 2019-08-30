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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;

import org.javamoney.moneta.Money;

import javafx.util.StringConverter;

public class DefaultTableMonetaryAmountConverter extends StringConverter<MonetaryAmount> {

	MonetaryAmountFormat moneyFormatter = MonetaryFormats.getAmountFormat(Locale.GERMANY);
	Matcher m=Pattern.compile("[0-9]{1,3}([\\\\. ][0-9]{3}){0,}(,[0-9]{0,2})?[ ]{0,}").matcher("");
	String defaultCurrency = "EUR";
		
	@Override
	public String toString(MonetaryAmount object) {
		if (object==null)
			return null;
		return moneyFormatter.format((MonetaryAmount)object);
	}

	@Override
	public MonetaryAmount fromString(String string) {
		if (string==null)
			return null;
		if (string.strip().length()==0)
			return Money.of(0, defaultCurrency);
		m.reset(string.strip());
		if(m.matches()) {
			string=string.strip().concat(" "+defaultCurrency);
		}
		Money res=Money.parse(string, moneyFormatter);
		return res;
		
	}

}
