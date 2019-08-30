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

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.beans.property.SimpleObjectProperty;

public class DayMonthField extends AbstractTimeField implements Supplier<LocalDate>,Consumer<LocalDate>{

	public DayMonthField() {
		super(new SimpleObjectProperty<LocalDate>(LocalDate.now()),
				  "(([1-3]0|[0-2]{0,1}[1-9]|31)\\.(1[02]|[0]{0,1}[13578]))|"
				+ "(([1-3]0|[0-2]{0,1}[1-9])\\.(11|[0]{0,1}[469]))|"
				+ "((10|[0-1]{0,1}[1-9]|2[0-9])\\.([0]{0,1}2))|");
		this.setPromptText("DD.MM");
		this.filter.setCompletions(List.of("0","1","2","3","4","5","6","7","8","9","."));
	}

	@Override
	public LocalDate getDate() {
		updateActResults();
		try {
			if (foundValues!=null && foundValues.size()>=2) {
				LocalDate result=LocalDate.of(getBaseDate().getYear(),foundValues.get(1),foundValues.get(0));
				return result;
			}
		} catch (DateTimeException e){
			//This might be a legitimate exception, the regular expression does not filter
			//29th february for non leap years and 0.
			if(!e.getMessage().contains("is not a leap year"))
				throw new RuntimeException(e);
		}
		return null;
	}
	
	public void setYear(int year) {
		setBaseDate(LocalDate.of(year,1,1));
	}
	

	@Override
	public LocalDate get() {
		return getDate();
	}
	
	@Override
	public void accept(LocalDate t) {
		if(null==t)
			return;
		this.setBaseDate(t);
		this.setText(String.format("%02d.%02d",t.getDayOfMonth(),t.getMonthValue()));
	}
}
