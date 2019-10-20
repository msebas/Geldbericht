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
package org.mcservice.geldbericht.data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

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
	
	protected Boolean defaultVatType=false;
	
	@TableViewColumn(colName="Ausblenden")
	@TableViewColumnOrder(40)
	protected Boolean disabledVatType=false;
	
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
			String shortName, BigDecimal value,	Boolean defaultVatType,
			Boolean disabledVatType) {
		super(uid, lastChange);
		this.name = name;
		this.shortName = shortName;
		this.value = value;
		this.defaultVatType=defaultVatType;
		this.disabledVatType=disabledVatType;
	}
	
	public VatType(VatType otherVatType) {
		super(otherVatType.getUid(), otherVatType.lastChange);
		this.name = otherVatType.name;
		this.shortName = otherVatType.shortName;
		this.value = otherVatType.value;
		this.defaultVatType = otherVatType.defaultVatType;
		this.disabledVatType = otherVatType.disabledVatType;
	}
	
	/**
	 * @return the defaultVatType
	 */
	public Boolean isDefaultVatType() {
		return defaultVatType;
	}

	/**
	 * @param name
	 * @param value
	 */
	public VatType(String name, String shortName, BigDecimal value, Boolean defaultVatType) {
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
	public Boolean isDisabledVatType() {
		return disabledVatType;
	}

	/**
	 * @param disabledVatType the disabledVatType to set
	 */
	public void setDisabledVatType(Boolean disabledVatType) {
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
	public void setDefaultVatType(Boolean defaultVatType) {
		this.defaultVatType = defaultVatType;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(BigDecimal value) {
		if (this.value==value || (this.value!=null && this.value.equals(value))) {
			return;
		}
		if(getUid()!=null)
			throw new RuntimeException("Cannot change value of VatType with UID.");
		this.value = value;
		this.lastChange = ZonedDateTime.now();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((defaultVatType == null) ? 0 : defaultVatType.hashCode());
		result = prime * result + ((disabledVatType == null) ? 0 : disabledVatType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((shortName == null) ? 0 : shortName.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj==null || getClass() != obj.getClass())
			return false;
		if (!super.equals(obj))
			return false;
		VatType other = (VatType) obj;
		if (defaultVatType == null) {
			if (other.defaultVatType != null)
				return false;
		} else if (!defaultVatType.equals(other.defaultVatType))
			return false;
		if (disabledVatType == null) {
			if (other.disabledVatType != null)
				return false;
		} else if (!disabledVatType.equals(other.disabledVatType))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (shortName == null) {
			if (other.shortName != null)
				return false;
		} else if (!shortName.equals(other.shortName))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public List<AbstractDataObjectDatabaseQueueEntry> getPersistingList() {
		return List.of(new AbstractDataObjectDatabaseQueueEntry(new VatType(this),false));
	}

	@Override
	public List<AbstractDataObjectDatabaseQueueEntry> getDeleteList() {
		if(null!=getUid()) {
			return List.of(new AbstractDataObjectDatabaseQueueEntry(new VatType(this),true));
		} else {
			return null;
		}
	}


}
