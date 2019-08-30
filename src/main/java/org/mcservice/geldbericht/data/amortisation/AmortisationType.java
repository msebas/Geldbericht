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
package org.mcservice.geldbericht.data.amortisation;

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
