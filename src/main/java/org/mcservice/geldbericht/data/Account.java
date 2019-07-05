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
import javax.persistence.Table;

import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
@Table(name = "Accounts")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Account extends AbstractDataObject {
	
	protected String accountNumber=null;
	protected String accountName=null;
	protected long balance=0;
	protected long initialBalance=0;
	@OneToMany(mappedBy="account")
	protected List<MonthAccountTurnover> balanceMonths=new ArrayList<MonthAccountTurnover>();
	@ManyToOne
	protected Company company=null;
	
	private Account() {
		super(null,ZonedDateTime.now());
	}
	
	/**
	 * @param uid
	 * @param lastChange
	 * @param accountNumber
	 * @param accountName
	 */
	protected Account(Long uid, ZonedDateTime lastChange, String accountNumber, String accountName,
			long initialBalance, Company company) {
		super(uid,lastChange);
		this.accountNumber = accountNumber;
		this.accountName = accountName;
		this.initialBalance=initialBalance;
		this.company=company;
	}
	
	/**
	 * @param uid
	 * @param lastChange
	 * @param accountNumber
	 * @param accountName
	 */
	public Account(String accountNumber, String accountName, long balance, Company company
			) {
		super(null);
		this.accountNumber = accountNumber;
		this.accountName = accountName;
		this.balance=balance;
		this.company=company;
	}

	/**
	 * @param accountNumber the accountNumber to set
	 */
	public void setAccountNumber(String accountNumber) {
		if(this.accountNumber==accountNumber ||
				( this.accountNumber!=null && this.accountNumber.equals(accountNumber) )
				)
			return;
		this.accountNumber = accountNumber;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @param accountName the accountName to set
	 */
	public void setAccountName(String accountName) {
		if(this.accountName==accountName ||
				( this.accountName!=null && this.accountName.equals(accountName) )
				)
			return;
		this.accountName = accountName; 
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @param balance the balance to set
	 */
	public void setBalance(long balance) {
		if(this.balance==balance)
			return;
		this.balance = balance; 
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @param company the company to set
	 */
	public void setCompany(Company company) {
		if(this.company==company ||
				( this.company!=null && this.company.equals(company) )
				)
			return;
		this.company = company; 
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @return the balance
	 */
	public long getBalance() {
		return balance;
	}

	/**
	 * @return the company
	 */
	public Company getCompany() {
		return company;
	}

	/**
	 * @return the accountNumber
	 */
	public String getAccountNumber() {
		return accountNumber;
	}

	/**
	 * @return the accountName
	 */
	public String getAccountName() {
		return accountName;
	}

}
