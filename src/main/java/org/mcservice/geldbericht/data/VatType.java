package org.mcservice.geldbericht.data;

import java.time.ZonedDateTime;

public class VatType  extends DataObject {
	
	String name=null;
	double value=0.0;
	
	public VatType(Long uid, ZonedDateTime lastChange, String name, double value) {
		super(uid, lastChange);
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public double getValue() {
		return value;
	}

}
