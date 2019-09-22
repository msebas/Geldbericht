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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.beans.property.SimpleObjectProperty;

public class MonthYearField extends AbstractTimeField implements Supplier<LocalDate>,Consumer<LocalDate>{

	public MonthYearField() {
		super(new SimpleObjectProperty<LocalDate>(LocalDate.now()),MonthYearConverter.pattern);
		this.setPromptText("MM.YY (1950-2049)");
		this.filter.setCompletions(List.of("0","1","2","3","4","5","6","7","8","9","."));
		setBaseDate(LocalDate.of(2004, 01, 01));
	}

	@Override
	public LocalDate getDate() {
		updateActResults();
		if (foundValues!=null && foundValues.size()>=2) {
			int year=foundValues.get(1);
			if(year<50)
				year+=2000;
			else
				year+=1900;
			LocalDate result=LocalDate.of(year,foundValues.get(0),getBaseDate().getDayOfMonth());
			return result;
		}
		return null;
	}
	
	public void setDay(int day) {
		setBaseDate(LocalDate.of(2004,1,day));
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
		this.setText(String.format("%tm.%ty",t,t));
	}
}
