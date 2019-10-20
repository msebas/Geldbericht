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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractDataObject {
	
	@Id
	@GeneratedValue(strategy= GenerationType.SEQUENCE)
	private Long uid;
	protected ZonedDateTime lastChange=ZonedDateTime.now();
	
	@Transient
	private ReentrantLock firstPersistenceLock;
	
	/**
	 * @param uid
	 * @param lastChange
	 */
	protected AbstractDataObject(Long uid, ZonedDateTime lastChange) {
		this(uid);
		this.lastChange = lastChange;
	}
	
	/**
	 * @param uid
	 */
	protected AbstractDataObject(Long uid) {
		super();
		this.uid = uid;
		if(null==this.uid) {
			firstPersistenceLock=new ReentrantLock();
		}
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractDataObject other = (AbstractDataObject) obj;
		if (lastChange == null) {
			if (other.lastChange != null)
				return false;
		} else if (!lastChange.equals(other.lastChange))
			return false;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}
	
	public class AbstractDataObjectDatabaseQueueEntry {
		
		final AbstractDataObject stateToPersist;
		final boolean delete;
				
		/**
		 * @param stateToPersist
		 * @param objectReference
		 * @param callback
		 */
		protected AbstractDataObjectDatabaseQueueEntry(AbstractDataObject stateToPersist,boolean delete) {
			this.delete=delete;
			this.stateToPersist = stateToPersist;
		}
		
		public AbstractDataObject getStateToPersist() {
			if(null==stateToPersist.getUid()) {
				firstPersistenceLock.lock();
			}
			return stateToPersist;
		}
		
		/**
		 * Updates the {@code uid} of the corresponding object to the one of the persisted state.
		 * Any inheriting class should call this method to get the UID updated on the first
		 * Persistence of an object. Additional the UID of the {@code stateToPersist} is updated
		 * to allow references to compare if changes happened while the state was in the queue. 
		 *  
		 * @param persistedState
		 */
		public void applyPersistedState(AbstractDataObject persistedState) {
			if(isDelete()) {
				return;
			}
			
			if(null != firstPersistenceLock) {
				if(null==AbstractDataObject.this.uid) {
					AbstractDataObject.this.uid=persistedState.uid;
					stateToPersist.uid=persistedState.uid;
					firstPersistenceLock.unlock();
				} else {
					if(firstPersistenceLock.isHeldByCurrentThread()) {
						firstPersistenceLock.unlock();
					}
				}
			}
			if(AbstractDataObject.this.uid!=persistedState.uid){
				throw new RuntimeException("Uid of object changed over persining process."
						+ "Probably an implementation error.");
			}
		}
		
		public boolean isDelete() {
			return delete;
		}
		
		public boolean isMerge() {
			return !delete && stateToPersist.getUid()!=null;
		}
		
		public boolean isCreate() {
			return !delete && stateToPersist.getUid()==null;
		}
		
	}
	
	public abstract List<AbstractDataObjectDatabaseQueueEntry> getPersistingList();
	
	public abstract List<AbstractDataObjectDatabaseQueueEntry> getDeleteList();

}
