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
package org.mcservice.geldbericht;

import org.mcservice.geldbericht.database.BackgroundDbThread;
import org.mcservice.geldbericht.database.DbAbstractionLayer;

import javafx.util.Callback;

public class ControllerFactory implements Callback<Class<?>, Object>{
	
	private DbAbstractionLayer db=null;
	private BackgroundDbThread backgroundDatabase;
	private Thread backgroundThread;
	
	public ControllerFactory() {
		this(null,null);
	}

	public ControllerFactory(DbAbstractionLayer db) {
		this(db,null);
	}
	
	public ControllerFactory(DbAbstractionLayer db, BackgroundDbThread backgroundDatabase) {
		this.db=db;
		this.backgroundDatabase=backgroundDatabase;
	}
	
	public Object call(Class<?> clazz) {
		if (LoginController.class.equals(clazz))
			return new LoginController(this);
		createDB();
		if (AccountManagerController.class.equals(clazz))
			return new AccountManagerController(db);
		if (CompanyManagerController.class.equals(clazz))
			return new CompanyManagerController(db);
		if (PdfGeneratorPaneController.class.equals(clazz))
			return new PdfGeneratorPaneController(db);
		if (PrimaryController.class.equals(clazz))
			return new PrimaryController(db);
		if (TransactionInputPaneController.class.equals(clazz))
			return new TransactionInputPaneController(db,backgroundDatabase);
		if (VatTypeManagerController.class.equals(clazz))
			return new VatTypeManagerController(db);
		throw new RuntimeException("Not implemented yet");	
	}
	
	public void createDB() {
		if(null==db) {
			db=new DbAbstractionLayer();
		}		
		if(null==backgroundDatabase) {
			backgroundDatabase=new BackgroundDbThread(db);
		}
		if(null==backgroundThread) {
			backgroundThread = new Thread(backgroundDatabase);
			backgroundThread.setDaemon(true);
			backgroundThread.start();
		}
	}
	
	public DbAbstractionLayer getDB() {
		return this.db;
	}
	
	public void setDb(DbAbstractionLayer db) {
		this.db=db;
	}
	
	public void joinBackgroundThread() throws InterruptedException {
		backgroundDatabase.stopThread();
		backgroundThread.join();
	}
	
}
