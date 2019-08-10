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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
import org.mcservice.javafx.PercentageBigDecimalStringConverter;
import org.mcservice.javafx.TrimStringConverter;
import org.mcservice.javafx.control.table.TableViewColumn;
import org.mcservice.javafx.control.table.TableViewColumnOrder;
import org.mcservice.javafx.control.table.TableViewConverter;
import org.mcservice.javafx.control.table.TableViewFinalIfNotNull;

import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Table(name = "VatTypes")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class VatType  extends AbstractDataObject {
	
	@Size(min = 2, max = 256)
	@TableViewColumn(colName="Bezeichnung")
	@TableViewColumnOrder(20)
	@TableViewConverter(converter=TrimStringConverter.class)
	protected String name=null;
	
	@Size(min = 1, max = 4)
	@TableViewColumn(colName="Kurzbezeichnung")
	@TableViewColumnOrder(10)
	@TableViewConverter(converter=TrimStringConverter.class)
	protected String shortName=null;
	
	@TableViewColumn(colName="Steuersatz")
	@TableViewColumnOrder(30)
	@TableViewConverter(converter=PercentageBigDecimalStringConverter.class)
	@TableViewFinalIfNotNull("getUid")
	@Range(min=0)
	protected BigDecimal value=new BigDecimal(0);
	
	protected boolean defaultVatType=false;
	
	@TableViewColumn(colName="Ausblenden")
	@TableViewColumnOrder(40)
	protected boolean disabledVatType=false;
	
	private VatType() {
		super(null,ZonedDateTime.now());
	}
	
	/**
	 * @param uid
	 * @param lastChange
	 * @param name
	 * @param value
	 */
	public VatType(Long uid, ZonedDateTime lastChange, String name,
			String shortName, BigDecimal value,	boolean defaultVatType,
			boolean disabledVatType) {
		super(uid, lastChange);
		this.name = name;
		this.shortName = shortName;
		this.value = value;
		this.defaultVatType=defaultVatType;
		this.disabledVatType=disabledVatType;
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
	public VatType(String name, String shortName, BigDecimal value, boolean defaultVatType) {
		super(null);
		this.name = name;
		this.shortName = shortName;
		this.value = value;
		this.defaultVatType=defaultVatType;
	}

	@Override
	public String toString() {
		return shortName;
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
	public BigDecimal getValue() {
		return value;
	}

	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @return the disabledVatType
	 */
	public boolean isDisabledVatType() {
		return disabledVatType;
	}

	/**
	 * @param disabledVatType the disabledVatType to set
	 */
	public void setDisabledVatType(boolean disabledVatType) {
		this.disabledVatType = disabledVatType;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param shortName the shortName to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @param defaultVatType the defaultVatType to set
	 */
	public void setDefaultVatType(boolean defaultVatType) {
		this.defaultVatType = defaultVatType;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(BigDecimal value) {
		if(getUid()!=null)
			throw new RuntimeException("Cannot change value of VatType with UID.");
		this.value = value;
	}

}
