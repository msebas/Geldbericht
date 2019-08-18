package org.mcservice.javafx.control.date;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.util.StringConverter;

public class MonthYearConverter extends StringConverter<LocalDate>{
	public static String pattern="(1[0-2]|[0]{0,1}[0-9])\\.([0-9]{1,2})";
	private LocalDate baseDate=LocalDate.of(2000, 1, 1);
	private final Matcher matcher=Pattern.compile(pattern).matcher("");
	
	@Override
	public String toString(LocalDate object) {
		return null==object?null:String.format("%02d.%02d",object.getMonthValue(),object.getYear()<2000?object.getYear()-1900:object.getYear()-2000);
	}

	@Override
	public LocalDate fromString(String string) {
		if (null==string)
			return null;
		matcher.reset(string);
		if(!matcher.matches() || matcher.groupCount()!=2)
			return null;
		
		int year=Integer.valueOf(matcher.group(2));
		if(year<50)
			year+=2000;
		else
			year+=1900;
		LocalDate result=LocalDate.of(year,Integer.valueOf(matcher.group(1)),getBaseDate().getDayOfMonth());
		return result;
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
