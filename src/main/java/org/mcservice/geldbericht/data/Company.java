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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.mcservice.javafx.TrimStringConverter;
import org.mcservice.javafx.control.table.TableViewColumn;
import org.mcservice.javafx.control.table.TableViewColumnOrder;
import org.mcservice.javafx.control.table.TableViewConverter;

@Entity
@Table(name = "Companies")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Company extends AbstractDataObject{
	
	@OneToMany(mappedBy="company", fetch = FetchType.LAZY)
	List<Account> accounts=new ArrayList<Account>();
	
	@Size(min = 2, max = 256)
	@TableViewColumn(colName="Name")
	@TableViewColumnOrder(10)
	@TableViewConverter(converter=TrimStringConverter.class)
	String companyName=null;
	
	@Pattern(regexp = "[A-Za-z0-9\\-]{5}")
	@TableViewColumn(colName="Betriebsnummer")
	@TableViewColumnOrder(20)
	@TableViewConverter(converter=TrimStringConverter.class)
	String companyNumber=null;
	
	@Pattern(regexp = "[0-9]{10}")
	@TableViewColumn(colName="Buchstelle")
	@TableViewColumnOrder(30)
	@TableViewConverter(converter=TrimStringConverter.class)
	String companyBookkeepingAppointment=null;
	
	@Transient
	boolean accountsLoaded = false;
	
	private Company() {
		super(null,ZonedDateTime.now());
	}
	
	public Company(Company otherCompany) {
		super(otherCompany.getUid(),otherCompany.lastChange);
		this.companyName=otherCompany.companyName;
		this.companyNumber=otherCompany.companyNumber;
		this.companyBookkeepingAppointment=otherCompany.companyBookkeepingAppointment;
		for (Account account : otherCompany.accounts) {
			Account tmp=new Account(account);
			tmp.company=this;
			accounts.add(tmp);
		}
		accountsLoaded=true;
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
		accountsLoaded=true;
	}

	/**
	 * @param uid
	 * @param lastChange
	 * @param accounts
	 * @param companyName
	 * @param companyNumber
	 * @param companyBookkeepingAppointment
	 */
	public Company(Long uid, ZonedDateTime lastChange,ArrayList<Account> accounts, String companyName,
			String companyNumber, String companyBookkeepingAppointment) {
		super(uid,lastChange);
		this.accounts = accounts;
		this.companyName = companyName;
		this.companyNumber = companyNumber;
		this.companyBookkeepingAppointment = companyBookkeepingAppointment;
		accountsLoaded=true;
	}

	/**
	 * @return the accounts
	 */
	public List<Account> getAccounts() {
		accountsLoaded=true;
		return accounts;
	}
	
	/**
	 * @param accounts the accounts to set
	 */
	public void setAccounts(List<Account> accounts) {
		if(this.accounts==accounts ||
				( this.accounts!=null && this.accounts.equals(accounts) )
				)
			return;
		accountsLoaded=true;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((accounts == null) ? 0 : accounts.hashCode());
		result = prime * result
				+ ((companyBookkeepingAppointment == null) ? 0 : companyBookkeepingAppointment.hashCode());
		result = prime * result + ((companyName == null) ? 0 : companyName.hashCode());
		result = prime * result + ((companyNumber == null) ? 0 : companyNumber.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if(obj==null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Company other = (Company) obj;
		if (getUid()!=other.getUid()) {
			return false;
		}
		return true;			
	}
	
	public boolean equals(Object obj, boolean rec) {
		if (this == obj)
			return true;
		if(obj==null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Company other = (Company) obj;
		if (getUid()!=other.getUid()) {
			return false;
		}
		if(rec) {
			if (accounts == null || other.accounts == null) {
				if(accounts != other.accounts)
					return false;
			} else {
				//This has to be done manual, because otherwise we run into 
				//recursion problems because of backreferences
				if (accounts.size()!=other.accounts.size()) {
					return false;
				}
				Iterator<Account> otherIterator = other.accounts.iterator();
				for (Iterator<Account> iterator = accounts.iterator(); iterator.hasNext();) {
					iterator.next().equals(otherIterator.next(), false);
				}
			}
		}
		if (companyBookkeepingAppointment == null) {
			if (other.companyBookkeepingAppointment != null)
				return false;
		} else if (!companyBookkeepingAppointment.equals(other.companyBookkeepingAppointment))
			return false;
		if (companyName == null) {
			if (other.companyName != null)
				return false;
		} else if (!companyName.equals(other.companyName))
			return false;
		if (companyNumber == null) {
			if (other.companyNumber != null)
				return false;
		} else if (!companyNumber.equals(other.companyNumber))
			return false;
		return true;
	}

	@Override
	public List<AbstractDataObjectDatabaseQueueEntry> getPersistingList() {
		LinkedList<AbstractDataObjectDatabaseQueueEntry> res=new LinkedList<>();
		
		Company companyState=new Company(getUid(), lastChange, new ArrayList<Account>(), companyName, companyNumber, companyBookkeepingAppointment);
		
		if(accountsLoaded) {
			for (Account account : accounts) {
				res.addAll(account.getPersistingList(true));
				AbstractDataObjectDatabaseQueueEntry obj = res.get(res.size()-1);
				if (obj instanceof Account.AccountDatabaseQueueEntry) {
					((Account.AccountDatabaseQueueEntry) obj).addToCompany(companyState);
				}
			}
		} else {
			companyState.accounts=accounts;
		}
		
		res.add(new AbstractDataObjectDatabaseQueueEntry(companyState,false));
		return res;
	}

	@Override
	public List<AbstractDataObjectDatabaseQueueEntry> getDeleteList() {
		LinkedList<AbstractDataObjectDatabaseQueueEntry> res=new LinkedList<>();
		
		for (Account account : accounts) {
			res.addAll(account.getDeleteList());
		}
		if(this.getUid()!=null) {
			Company tmp = new Company(this);
			tmp.accounts.clear();
			AbstractDataObjectDatabaseQueueEntry me = new AbstractDataObjectDatabaseQueueEntry(tmp,true);
			res.add(me);
		}
		
		return res;
	}

	public boolean isAccountsLoaded() {
		return accountsLoaded;
	}
	
	

}
