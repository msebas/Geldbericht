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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.money.MonetaryAmount;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "MonthAccountTurnovers")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class MonthAccountTurnover extends AbstractDataObject {
	
	@ElementCollection
	List<Transaction> transactions=new ArrayList<Transaction>();
	LocalDate month=null;
	@ManyToOne
	Account account=null;
	@Convert(converter = MonetaryAmountConverter.class)
	MonetaryAmount monthBalanceAssets;
	@Convert(converter = MonetaryAmountConverter.class)
	MonetaryAmount initialAssets;
	@Convert(converter = MonetaryAmountConverter.class)
	MonetaryAmount finalAssets;
	@Convert(converter = MonetaryAmountConverter.class)
	MonetaryAmount monthBalanceDebt;
	@Convert(converter = MonetaryAmountConverter.class)
	MonetaryAmount initialDebt;
	@Convert(converter = MonetaryAmountConverter.class)
	MonetaryAmount finalDebt;
	
	private MonthAccountTurnover() {
		super(null,ZonedDateTime.now());
	}
	/**
	 * @param uid
	 * @param lastChange
	 * @param transactions
	 * @param company
	 * @param month
	 * @param account
	 * @param monthBalanceAssets
	 * @param initialAssets
	 * @param finalAssets
	 * @param monthBalanceDebt
	 * @param initialDebt
	 * @param finalDebt
	 */
	public MonthAccountTurnover(Long uid, ZonedDateTime lastChange, ArrayList<Transaction> transactions,
			LocalDate month, Account account, MonetaryAmount monthBalanceAssets, MonetaryAmount initialAssets,
			MonetaryAmount finalAssets, MonetaryAmount monthBalanceDebt, MonetaryAmount initialDebt, MonetaryAmount finalDebt) {
		super(uid,lastChange);
		this.transactions = transactions;
		this.month = month;
		this.account = account;
		this.monthBalanceAssets = monthBalanceAssets;
		this.initialAssets = initialAssets;
		this.finalAssets = finalAssets;
		this.monthBalanceDebt = monthBalanceDebt;
		this.initialDebt = initialDebt;
		this.finalDebt = finalDebt;
	}

	/**
	 * @param number The number of the runningTransaction to update
	 * @param runningTransaction The runningTransaction to update
	 */
	public void updateTranaction(int number, Transaction transaction) {
		if(this.transactions.get(number).equals(transaction))
			return;
		transaction.setNumber(number);
		this.transactions.set(number,transaction);
		this.lastChange=ZonedDateTime.now();
	}
	
	/**
	 * @param runningTransaction The runningTransaction to append
	 */
	public void appendTranaction(Transaction transaction) {
		transaction.setNumber(this.transactions.size());
		this.transactions.add(transaction);
		this.lastChange=ZonedDateTime.now();
	}
	
	/**
	 * @param number The number at that the runningTransaction should be inserted
	 * @param runningTransaction The runningTransaction to insert
	 */
	public void insertTranaction(int number, Transaction transaction) {
		this.transactions.add(number,transaction);
		this.lastChange=ZonedDateTime.now();
		for(int i=number;i<this.transactions.size();++i) {
			this.transactions.get(i).setNumber(i);
		}
	}

	/**
	 * @param number The number of the runningTransaction the should be removed
	 */
	public void removeTranaction(int number) {
		this.transactions.remove(number);
		this.lastChange=ZonedDateTime.now();
		for(int i=number;i<this.transactions.size();++i) {
			this.transactions.get(i).setNumber(i);
		}
	}
	
	/**
	 */
	public void removeAllTranactions() {
		this.transactions.clear();
		this.lastChange=ZonedDateTime.now();
	}
	
	/**
	 * @param month the month to set
	 */
	public void setMonth(LocalDate month) {
		if(this.month==month ||
				( this.month!=null && this.month.equals(month) )
				)
			return;
		this.month = month;
		this.lastChange=ZonedDateTime.now();
	}
	/**
	 * @param account the account to set
	 */
	public void setAccount(Account account) {
		if(this.account==account ||
				( this.account!=null && this.account.equals(account) )
				)
			return;
		this.account = account;
		this.lastChange=ZonedDateTime.now();
	}
	/**
	 * @param initialAssets the initialAssets to set
	 */
	public void setInitialAssets(MonetaryAmount initialAssets) {
		if(this.initialAssets==initialAssets)
			return;
		this.initialAssets = initialAssets;
		this.lastChange=ZonedDateTime.now();
	}
	/**
	 * @param initialDebt the initialDebt to set
	 */
	public void setInitialDebt(MonetaryAmount initialDebt) {
		if(this.initialDebt==initialDebt)
			return;
		this.initialDebt = initialDebt;
		this.lastChange=ZonedDateTime.now();
	}
	
	/**
	 * @return the transactions
	 */
	public List<Transaction> getTransactions() {
		return transactions;
	}
	/**
	 * @return the month
	 */
	public LocalDate getMonth() {
		return month;
	}
	/**
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}
	/**
	 * @return the monthBalanceAssets
	 */
	public MonetaryAmount getMonthBalanceAssets() {
		return monthBalanceAssets;
	}
	/**
	 * @return the initialAssets
	 */
	public MonetaryAmount getInitialAssets() {
		return initialAssets;
	}
	/**
	 * @return the finalAssets
	 */
	public MonetaryAmount getFinalAssets() {
		return finalAssets;
	}
	/**
	 * @return the monthBalanceDebt
	 */
	public MonetaryAmount getMonthBalanceDebt() {
		return monthBalanceDebt;
	}
	/**
	 * @return the initialDebt
	 */
	public MonetaryAmount getInitialDebt() {
		return initialDebt;
	}
	/**
	 * @return the finalDebt
	 */
	public MonetaryAmount getFinalDebt() {
		return finalDebt;
	}
}
