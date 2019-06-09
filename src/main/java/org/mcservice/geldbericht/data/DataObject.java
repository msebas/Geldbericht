package org.mcservice.geldbericht.data;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public abstract class DataObject {
	
	@Id
	final Long uid;
	ZonedDateTime lastChange=ZonedDateTime.now();
	
	/**
	 * @param uid
	 * @param lastChange
	 */
	protected DataObject(Long uid, ZonedDateTime lastChange) {
		super();
		this.uid = uid;
		this.lastChange = lastChange;
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
