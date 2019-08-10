package org.mcservice.javafx.control.date;

import java.time.LocalDate;
import java.time.chrono.Chronology;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.mcservice.javafx.BaseMatcherCallbackFilter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public abstract class AbstractTimeField extends TextField {
	
	private ObjectProperty<LocalDate> baseDate;
	private ObjectProperty<Chronology> chronology;
	protected List<Integer> foundValues;
	protected PatternFilter filter;
		
	private AbstractTimeField() {
		super();
	}
	
	protected AbstractTimeField(ObjectProperty<LocalDate> baseDate, String pattern) {
		this();
		this.baseDate=baseDate;
		this.chronology=new SimpleObjectProperty<Chronology>(Chronology.ofLocale(Locale.getDefault()));
		filter=new PatternFilter(pattern);
		this.setTextFormatter(new TextFormatter<LocalDate>(null,null,filter));
		filter.setMatchCallback(bool -> setError(bool));
		filter.setMatchedGroupsCallback(list -> setActResults(list));
	}

	public LocalDate getBaseDate() {
		return baseDate.get();
	}
	
	protected void setBaseDate(LocalDate baseDate) {
		this.baseDate.set(baseDate);
	}	

	protected ObjectProperty<LocalDate> baseDateProperty() {
		return this.baseDate;
	}
	
	public Chronology getChronology() {
		return chronology.get();
	}

	protected void setChronology(Chronology chronology) {
		this.chronology.set(chronology);
	}	

	protected ObjectProperty<Chronology> chronologyProperty() {
		return this.chronology;
	}
	
	public abstract LocalDate getDate();
	
	private void setError(boolean noErrorPresent) {
		if (!noErrorPresent) {
			if (!this.getStyleClass().contains("field-validation-error"))
				this.getStyleClass().add("field-validation-error");
			foundValues=null;
		} else {
			this.getStyleClass().remove("field-validation-error");
		}
	}
	
	private void setActResults(List<String> matchedGroups) {
		if(matchedGroups.size()>0) {
			if(null==foundValues || foundValues.size()!=matchedGroups.size()) {
				foundValues=new ArrayList<Integer>(matchedGroups.size());
			}
			foundValues.clear();
			for (String act: matchedGroups) {
				foundValues.add(Integer.valueOf(act));
			}
		} else {
			foundValues=null;
		}
	}
	
	protected class PatternFilter extends BaseMatcherCallbackFilter{
		protected PatternFilter(String pattern) {
			super(Pattern.compile(pattern));
		}

	}

}