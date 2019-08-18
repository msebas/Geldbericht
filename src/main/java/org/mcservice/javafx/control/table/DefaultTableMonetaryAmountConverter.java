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
		if (string==null || string.strip().length()==0)
			return null;
		m.reset(string);
		if(m.matches()) {
			string=string.strip().concat(" "+defaultCurrency);
		}
		Money res=Money.parse(string, moneyFormatter);
		return res;
	}

}
