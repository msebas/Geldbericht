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
package org.mcservice.geldbericht.database;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.mcservice.geldbericht.App;
import org.mcservice.geldbericht.data.AbstractDataObject;
import org.mcservice.geldbericht.data.AbstractDataObject.AbstractDataObjectDatabaseQueueEntry;

public class BackgroundDbThread implements Runnable{

	DbAbstractionLayer db=null;
	boolean stop=false;
	BlockingQueue<List<AbstractDataObjectDatabaseQueueEntry>> queue;
	
	static int softMaxObjectsToUpdatePerTransaction=10000;
	
	public BackgroundDbThread(DbAbstractionLayer db) {
		this.db=db;
		queue=new LinkedBlockingQueue<List<AbstractDataObjectDatabaseQueueEntry>>();
	}
	
	/**
	 * Adds an object to persist to the queue. If {@code stopThread()}
	 * was called before an {@code IllegalStateException} is thrown.
	 * @param obj The object to persist
	 */
	public void addToQueue(AbstractDataObject obj,boolean remove) {
		if(obj==null)
			return;
		if(!stop) {
			List<AbstractDataObjectDatabaseQueueEntry> list;
			if(remove) {
				list = obj.getDeleteList();
			} else {
				list = obj.getPersistingList();
			}
			if (null!=list) {
				App.logger.debug(String.format("Database: Adding %d objects to %s queue.", list.size(),remove?"delete":"update"));
				queue.add(list);
			}
		} else {
			throw new IllegalStateException("Persister is stopping. "
					+ "No further persistance requests allowed.");
		}
	}
	
	/**
	 * Stops the insertion as soon as the queue becomes empty
	 * (e.g. as soon as all elements in the queue are persisted).
	 * After a call to this method any further insertion is rejected.   
	 */
	public void stopThread() {
		stop=true;
		queue.add(null);
	}

	@Override
	public void run() {
		while(!stop) {
			LinkedList<AbstractDataObjectDatabaseQueueEntry> internalQueue=new LinkedList<>();
			
			try {
				Collection<AbstractDataObjectDatabaseQueueEntry> tmp = queue.poll(30, TimeUnit.SECONDS);
				if(null!=tmp) {
					internalQueue.addAll(tmp);
				} else {
					continue;
				}
			} catch (InterruptedException e) {
				continue;
			}
			while(internalQueue.size()<softMaxObjectsToUpdatePerTransaction) {
				Collection<AbstractDataObjectDatabaseQueueEntry> tmp = queue.poll();
				if(null!=tmp) {
					internalQueue.addAll(tmp);
				} else {
					break;
				}
			}
			
			persistObjectList(internalQueue);
		}
	}

	private void persistObjectList(List<AbstractDataObjectDatabaseQueueEntry> listToPersist) {
		db.mergeDataPersistanceQueue(listToPersist);
		App.logger.debug(String.format("Database: %d queue entries processed.", listToPersist.size()));
	}
	
	
	
	

}
