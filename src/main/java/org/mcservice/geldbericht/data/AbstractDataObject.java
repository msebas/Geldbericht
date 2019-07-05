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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractDataObject {
	
	@Id
	@GeneratedValue(strategy= GenerationType.SEQUENCE)
	protected final Long uid;
	protected ZonedDateTime lastChange=ZonedDateTime.now();
	
	/**
	 * @param uid
	 * @param lastChange
	 */
	protected AbstractDataObject(Long uid, ZonedDateTime lastChange) {
		super();
		this.uid = uid;
		this.lastChange = lastChange;
	}
	
	/**
	 * @param uid
	 */
	protected AbstractDataObject(Long uid) {
		super();
		this.uid = uid;
	}
	/**
	 * @return the uid
	 */
	public Long getUid() {
		return uid;
	}
	/**
	 * @return the lastChange
	 */
	public ZonedDateTime getLastChange() {
		return lastChange;
	}
	
	

}
