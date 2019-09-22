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
package org.mcservice.javafx.control.date;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.util.StringConverter;

public class DayMonthConverter extends StringConverter<LocalDate>{
	
	private LocalDate baseDate=LocalDate.of(2004, 1, 1);
	private final Matcher matcher=Pattern.compile("([0-2]{0,1}[0-9]|3[0-1])\\.(1[0-2]|[0]{0,1}[0-9])").matcher("");
	
	@Override
	public String toString(LocalDate object) {
		if(object==null)
			return null;
		return String.format("%02d.%02d",object.getDayOfMonth(),object.getMonthValue());
	}

	@Override
	public LocalDate fromString(String string) {
		if(string==null)
			return null;
		matcher.reset(string);
		if(!matcher.matches() || matcher.groupCount()!=2)
			throw new RuntimeException("Format exception, %s does not fit to the format DD.MM");
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
