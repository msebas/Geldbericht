package org.mcservice.javafx.control.date;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.util.StringConverter;

public class DayMonthConverter extends StringConverter<LocalDate>{
	
	private LocalDate baseDate=LocalDate.of(2000, 1, 1);
	private final Matcher matcher=Pattern.compile("([0-2]{0,1}[0-9]|3[0-1])\\.(1[0-2]|[0]{0,1}[0-9])").matcher("");
	
	@Override
	public String toString(LocalDate object) {
		return String.format("%02d.%02d",object.getDayOfMonth(),object.getMonthValue());
	}

	@Override
	public LocalDate fromString(String string) {
		matcher.reset(string);
		if(!matcher.matches() || matcher.groupCount()!=2)
			throw new RuntimeException("Format exception, %s does nto fit to the format DD.MM");
		return LocalDate.of(baseDate.getYear(), Integer.valueOf(matcher.group(2)), Integer.valueOf(matcher.group(1)));
	}

	/**
	 * @return the baseDate
	 */
	public LocalDate getBaseDate() {
		return baseDate;
	}

	/**
	 * @param baseDate the baseDate to set
	 */
	public void setBaseDate(LocalDate baseDate) {
		this.baseDate = baseDate;
	}

}
