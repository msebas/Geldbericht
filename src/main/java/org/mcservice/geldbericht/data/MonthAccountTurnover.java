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
import javax.money.MonetaryAmountFactory;

import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.mcservice.geldbericht.data.converters.MonetaryAmountConverter;

@Entity
@Table(name = "MonthAccountTurnovers")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class MonthAccountTurnover extends AbstractDataObject implements Comparable<MonthAccountTurnover> {
	
	@ElementCollection(fetch = FetchType.LAZY)
	@OrderBy("number ASC")
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
	
	@Transient
	protected boolean checkTransactions=false;
	
	public static MonthAccountTurnover getEmptyMonthAccountTurnover(LocalDate month, Account account) {
		MonthAccountTurnover result=new MonthAccountTurnover();
		result.month=month;
		result.account=account;
		result.transactions=new ArrayList<Transaction>();
		MonetaryAmount balance=result.account.getBalance();
		MonetaryAmountFactory<? extends MonetaryAmount> factory = balance.getFactory().setCurrency(balance.getCurrency());
		result.monthBalanceAssets=factory.setNumber(0).create();
		result.initialAssets=balance.isPositiveOrZero()?factory.setAmount(balance).create():factory.setNumber(0).create();
		result.finalAssets=factory.setAmount(result.initialAssets).create();
		result.monthBalanceDebt=factory.setNumber(0).create();
		result.initialDebt=balance.isNegative()?factory.setAmount(balance).create():factory.setNumber(0).create();
		result.initialDebt=result.initialDebt.multiply(-1);
		result.finalDebt=factory.setAmount(result.initialDebt).create();
		
		return result;
	}
	
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
		checkTransactions=true;
		transaction.setNumber(number);
		this.transactions.set(number,transaction);
		this.lastChange=ZonedDateTime.now();
	}
	
	/**
	 * @param runningTransaction The runningTransaction to append
	 */
	public void appendTranaction(Transaction transaction) {
		checkTransactions=true;
		transaction.setNumber(this.transactions.size());
		this.transactions.add(transaction);
		this.lastChange=ZonedDateTime.now();
	}
	
	/**
	 * @param number The number at that the runningTransaction should be inserted
	 * @param runningTransaction The runningTransaction to insert
	 */
	public void insertTranaction(int number, Transaction transaction) {
		checkTransactions=true;
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
		checkTransactions=true;
		this.transactions.remove(number);
		this.lastChange=ZonedDateTime.now();
		for(int i=number;i<this.transactions.size();++i) {
			this.transactions.get(i).setNumber(i);
		}
	}
	
	/**
	 */
	public void removeAllTranactions() {
		checkTransactions=true;
		this.transactions.clear();
		this.lastChange=ZonedDateTime.now();
	}
	
	/**
	 * @return the transactions
	 */
	public List<Transaction> getTransactions() {
		checkTransactions=true;
		return transactions;
	}
	/**
	 * @return the month
	 */
	public boolean isInMonth(LocalDate date) {
		return month.getMonthValue()==date.getMonthValue() && 
				month.getYear()==date.getYear();
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
	
	/**
	 * @param initialAssets the initialAssets to set
	 */
	public void setInitialAssets(MonetaryAmount initialAssets) {
		this.initialAssets = initialAssets;
	}

	/**
	 * @param initialDebt the initialDebt to set
	 */
	public void setInitialDebt(MonetaryAmount initialDebt) {
		this.initialDebt = initialDebt;
	}

	/**
	 * Updates the internal accountings, reads all transactions if they were requested at any
	 * point by a getter, otherwise transactions are not checked. If any change to a transaction 
	 * did occur without a getter, setter or transaction manipulation method called this change
	 * is not reflected in the calculated balance. If transactions are skipped only a possible 
	 * change in the values of initial assets and debts is assumed.
	 *  
	 * @return newMonthBalance-oldMonthBalance (zero if no change did happen/cancel out), 
	 *         add it to your actual balance to update it.
	 */
	public MonetaryAmount updateBalance() {
		MonetaryAmount initialBalance=this.getInitialAssets().subtract(this.getInitialDebt());
		
		MonetaryAmount change=this.getMonthBalanceAssets().subtract(this.getMonthBalanceDebt());
		MonetaryAmountFactory<? extends MonetaryAmount> factory = 
				initialAssets.getFactory().setCurrency(initialAssets.getCurrency());
		if(checkTransactions) {
			transactions.sort(null);
			MonetaryAmount nBalanceAssets = factory.setNumber(0).create();
			MonetaryAmount nBalanceDebt = factory.setNumber(0).create();
			int i=0;
			for (Transaction transaction : transactions) {
				nBalanceAssets=nBalanceAssets.add(transaction.getReceipts());
				nBalanceDebt=nBalanceDebt.add(transaction.getSpending());
				if(transaction.getLastChange().isAfter(this.getLastChange())) {
					this.lastChange=transaction.getLastChange();
				}
				++i;
				transaction.setNumber(i);
			}
			monthBalanceAssets=nBalanceAssets;
			monthBalanceDebt=nBalanceDebt;
			//We cannot set checkTransactions to false here,
			//because a reference to a transaction might still
			//exist outside.
		}
				
		MonetaryAmount finalBalance=initialBalance.add(monthBalanceAssets).subtract(monthBalanceDebt);
		if(finalBalance.isPositiveOrZero()) {
			finalAssets=finalBalance;
			finalDebt=factory.setNumber(0).create();
		} else {
			finalAssets=factory.setNumber(0).create();
			finalDebt=finalBalance.multiply(-1);
		}
		
		return monthBalanceAssets.subtract(monthBalanceDebt).subtract(change);
	}

	@Override
	public int compareTo(MonthAccountTurnover o) {
		return o.getMonth().compareTo(month);
	}
}
