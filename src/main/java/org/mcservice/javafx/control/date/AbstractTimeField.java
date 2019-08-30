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
import java.time.chrono.Chronology;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mcservice.javafx.BaseMatcherCallbackFilter;
import org.mcservice.javafx.control.table.SupportsEndEditCallback;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public abstract class AbstractTimeField extends TextField implements SupportsEndEditCallback {
	
	private ObjectProperty<LocalDate> baseDate;
	private ObjectProperty<Chronology> chronology;
	protected List<Integer> foundValues;
	protected BaseMatcherCallbackFilter filter;
	private Matcher validNumber=Pattern.compile("[ ]{0,}[+-]{0,1}[ ]{0,}[0-9]{1,}") .matcher(""); 
		
	private AbstractTimeField() {
		super();
		this.chronology=new SimpleObjectProperty<Chronology>(Chronology.ofLocale(Locale.getDefault()));
	}
	
	protected AbstractTimeField(ObjectProperty<LocalDate> baseDate, String pattern) {
		this();
		this.baseDate=baseDate;
		filter=new BaseMatcherCallbackFilter(Pattern.compile(pattern));;
		this.setTextFormatter(new TextFormatter<LocalDate>(null,null,filter));
		filter.setMatchCallback(bool -> setError(bool));
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
		} else if(getDate()==null){
			if (!this.getStyleClass().contains("field-validation-error"))
				this.getStyleClass().add("field-validation-error");
		} else {
			this.getStyleClass().remove("field-validation-error");
		}
	}
	
	protected void updateActResults() {
		List<String> matchedGroups=filter.getMatchedGroups();
		if(matchedGroups!=null && matchedGroups.size()>0) {
			if(null==foundValues) {
				foundValues=new ArrayList<Integer>(matchedGroups.size());
			}
			foundValues.clear();
			for (String act: matchedGroups) {
				if(act!=null) {
					validNumber.reset(act);
					if(validNumber.matches()) {
						foundValues.add(Integer.valueOf(act));
					}
				}
			}
		} else {
			foundValues=null;
		}
	}
	
	public void setEndEditCallback(Consumer<String> endEditCallback) {
		this.filter.setEndEditCallback(endEditCallback);
	}

}
