package org.mcservice.geldbericht.data.Amortisation;

import java.time.Duration;

public abstract class AmortisationType {
	
	Long uid=null;
	String name=null;
	
	public Long getUid() {
		return uid;
	}
	
	public String getName() {
		return name;
	}
	
	public AmortisationType(Long uid, String name) {
		super();
		this.uid = uid;
		this.name = name;
	}

	public double getValueAfterAmortisation(double value,Duration timediff) {
		return value-this.getAmortisation(value,timediff);
	}
	
	public abstract double getAmortisation(double value,Duration timediff);

}
