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
		setBaseDate(LocalDate.of(2000, 01, 01));
	}

	@Override
	public LocalDate getDate() {
		if (foundValues!=null && foundValues.size()>=2) {
			int year=foundValues.get(2);
			if(year<50)
				year+=2000;
			else
				year+=1900;
			LocalDate result=LocalDate.of(year,foundValues.get(1),getBaseDate().getDayOfMonth());
			return result;
		}
		return null;
	}
	
	public void setDay(int day) {
		setBaseDate(LocalDate.of(2000,1,day));
	}
	
	@Override
	public LocalDate get() {
		return getDate();
	}

	@Override
	public void accept(LocalDate t) {
		this.setBaseDate(t);
		this.setText(String.format("%2.d.%2.d",t.getMonthValue(),t.getYear()<2000?t.getYear()-1900:t.getYear()-2000));
	}
}
