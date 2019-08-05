package org.mcservice.javafx.control.date;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import javafx.beans.property.SimpleObjectProperty;

public class MonthYearField extends AbstractTimeField{

	public MonthYearField() {
		super(new SimpleObjectProperty<LocalDate>(LocalDate.now()),"(1[0-2]|[0]{0,1}[0-9])\\.([0-9]{1,2})");
		this.setPromptText("MM.YY (1950-2049)");
		this.filter.setCompletions(List.of("0","1","2","3","4","5","6","7","8","9","."));
		setBaseDate(LocalDate.of(2000, 01, 01));
	}

	@Override
	public LocalDate getDate() {
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
		setBaseDate(LocalDate.of(2000,1,day));
	}
	
	public void setEndEditCallback(Consumer<String> endEditCallback) {
		this.filter.setEndEditCallback(endEditCallback);
	}
}
