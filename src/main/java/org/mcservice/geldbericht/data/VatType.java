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
package org.mcservice.geldbericht.data;

import java.time.ZonedDateTime;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Table(name = "VatTypes")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class VatType  extends AbstractDataObject {
	
	protected String name=null;
	protected double value=0.0;
	protected boolean defaultVatType=false;
	
	private VatType() {
		super(null,ZonedDateTime.now());
	}
	
	/**
	 * @param uid
	 * @param lastChange
	 * @param name
	 * @param value
	 */
	protected VatType(Long uid, ZonedDateTime lastChange, String name, double value, 
			boolean defaultVatType) {
		super(uid, lastChange);
		this.name = name;
		this.value = value;
		this.defaultVatType=defaultVatType;
	}
	
	/**
	 * @return the defaultVatType
	 */
	public boolean isDefaultVatType() {
		return defaultVatType;
	}

	/**
	 * @param name
	 * @param value
	 */
	public VatType(String name, double value, boolean defaultVatType) {
		super(null);
		this.name = name;
		this.value = value;
		this.defaultVatType=defaultVatType;
	}

	@Override
	public String toString() {
		return "VatType [name=" + name + "]";
	}

	/**
	 * @return the VatType name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the VatType value
	 */
	public double getValue() {
		return value;
	}

}
