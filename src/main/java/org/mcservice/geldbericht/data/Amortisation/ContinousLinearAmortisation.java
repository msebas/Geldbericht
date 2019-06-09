package org.mcservice.geldbericht.data.Amortisation;

import java.time.Duration;

public class ContinousLinearAmortisation extends AmortisationType{

	double rate=0;
	
	public ContinousLinearAmortisation(Long uid, String name,double rate) {
		super(uid, name);
		this.rate=rate;
	}
	
	public double getAmortisation(double value,Duration timediff) {
		if (rate*timediff.getSeconds()>value)
			return value;
		return rate*timediff.getSeconds();
	}
}
