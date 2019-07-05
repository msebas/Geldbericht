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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Companies")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Company extends AbstractDataObject{
	
	@OneToMany(mappedBy="company")
	List<Account> accounts=new ArrayList<Account>();
	String companyName=null;
	String companyNumber=null;
	String companyBookkeepingAppointment=null;
	
	private Company() {
		super(null,ZonedDateTime.now());
	}

	/**
	 * @param uid
	 * @param lastChange
	 * @param accounts
	 * @param companyName
	 * @param companyNumber
	 * @param companyBookkeepingAppointment
	 */
	public Company(String companyName, String companyNumber, String companyBookkeepingAppointment) {
		super(null);
		this.companyName = companyName;
		this.companyNumber = companyNumber;
		this.companyBookkeepingAppointment = companyBookkeepingAppointment;
	}
	
	/**
	 * @param uid
	 * @param lastChange
	 * @param accounts
	 * @param companyName
	 * @param companyNumber
	 * @param companyBookkeepingAppointment
	 */
	public Company(ArrayList<Account> accounts, String companyName,
			String companyNumber, String companyBookkeepingAppointment) {
		super(null);
		this.accounts = accounts;
		this.companyName = companyName;
		this.companyNumber = companyNumber;
		this.companyBookkeepingAppointment = companyBookkeepingAppointment;
	}

	/**
	 * @return the accounts
	 */
	public List<Account> getAccounts() {
		return accounts;
	}
	
	/**
	 * @param accounts the accounts to set
	 */
	public void setAccounts(ArrayList<Account> accounts) {
		if(this.accounts==accounts ||
				( this.accounts!=null && this.accounts.equals(accounts) )
				)
			return;
		this.accounts = accounts;
		this.lastChange=ZonedDateTime.now();
	}
	
	/**
	 * @return the companyName
	 */
	public String getCompanyName() {
		return companyName;
	}
	/**
	 * @param companyName the companyName to set
	 */
	public void setCompanyName(String companyName) {
		if(this.companyName==companyName ||
				( this.companyName!=null && this.companyName.equals(companyName) )
				)
			return;
		this.companyName = companyName;
		this.lastChange=ZonedDateTime.now();
	}
	/**
	 * @return the companyNumber
	 */
	public String getCompanyNumber() {
		return companyNumber;
	}
	/**
	 * @param companyNumber the companyNumber to set
	 */
	public void setCompanyNumber(String companyNumber) {
		if(this.companyNumber==companyNumber ||
				( this.companyNumber!=null && this.companyNumber.equals(companyNumber) )
				)
			return;
		this.companyNumber = companyNumber;
		this.lastChange=ZonedDateTime.now();
	}
	/**
	 * @return the companyBookkeepingAppointment
	 */
	public String getCompanyBookkeepingAppointment() {
		return companyBookkeepingAppointment;
	}
	/**
	 * @param companyBookkeepingAppointment the companyBookkeepingAppointment to set
	 */
	public void setCompanyBookkeepingAppointment(String companyBookkeepingAppointment) {
		if(this.companyBookkeepingAppointment==companyBookkeepingAppointment ||
				( this.companyBookkeepingAppointment!=null && this.companyBookkeepingAppointment.equals(companyBookkeepingAppointment) )
				)
			return;
		this.companyBookkeepingAppointment = companyBookkeepingAppointment;
		this.lastChange=ZonedDateTime.now();
	}

}
