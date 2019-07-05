/*******************************************************************************
 * Copyright (C) 2019 Sebastian Müller <sebastian.mueller@mcservice.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
