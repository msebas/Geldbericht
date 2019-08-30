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

import org.mcservice.geldbericht.database.DbAbstractionLayer;

import javafx.util.Callback;

public class ControllerFactory implements Callback<Class<?>, Object>{
	
	private DbAbstractionLayer db=null;
	
	public ControllerFactory() {
		this(null);
	}
	
	public ControllerFactory(DbAbstractionLayer db) {
		this.db=db;
	}
	
	
	public Object call(Class<?> clazz) {
		createDb();
		if (AccountManagerController.class.equals(clazz))
			return new AccountManagerController(db);
		if (CompanyManagerController.class.equals(clazz))
			return new CompanyManagerController(db);
		if (PrimaryController.class.equals(clazz))
			return new PrimaryController(db);
		if (TransactionInputPaneController.class.equals(clazz))
			return new TransactionInputPaneController(db);
		if (VatTypeManagerController.class.equals(clazz))
			return new VatTypeManagerController(db);
		throw new RuntimeException("Not implemented yet");	
	}
	
	private void createDb() {
		if(null!=db) {
			return;
		}
		db=new DbAbstractionLayer("/tmp/geldberichtTestDB.sqlite");		
	}
	
	public void setDb(DbAbstractionLayer db) {
		this.db=db;
	}
	
}
