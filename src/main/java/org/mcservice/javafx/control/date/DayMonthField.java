package org.mcservice.javafx.control.date;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import javafx.beans.property.SimpleObjectProperty;

public class DayMonthField extends AbstractTimeField{

	public DayMonthField() {
		super(new SimpleObjectProperty<LocalDate>(LocalDate.now()),"([0-2]{0,1}[0-9]|3[0-1])\\.(1[0-2]|[0]{0,1}[0-9])");
		this.setPromptText("DD.MM");
		this.filter.setCompletions(List.of("0","1","2","3","4","5","6","7","8","9","."));
	}

	@Override
	public LocalDate getDate() {
		if (foundValues!=null && foundValues.size()>=2) {
			LocalDate result=LocalDate.of(getBaseDate().getYear(),foundValues.get(1),foundValues.get(0));
			return result;
		}
		return null;
	}
	
	public void setYear(int year) {
		setBaseDate(LocalDate.of(year,1,1));
	}
	
	public void setEndEditCallback(Consumer<String> endEditCallback) {
		this.filter.setEndEditCallback(endEditCallback);
	}
	
	

}
