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
	
	boolean monthBlocked=false;
	@Transient
	protected boolean transactionsLoaded=false;
	

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
		result.monthBlocked=false;
		
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
		this.monthBlocked = false;
	}

	public MonthAccountTurnover(MonthAccountTurnover otherMonthAccountTurnover) {
		super(otherMonthAccountTurnover.uid,otherMonthAccountTurnover.lastChange);
		this.month = otherMonthAccountTurnover.month;
		this.account = otherMonthAccountTurnover.account;
		this.monthBalanceAssets = otherMonthAccountTurnover.monthBalanceAssets;
		this.initialAssets = otherMonthAccountTurnover.initialAssets;
		this.finalAssets = otherMonthAccountTurnover.finalAssets;
		this.monthBalanceDebt = otherMonthAccountTurnover.monthBalanceDebt;
		this.initialDebt = otherMonthAccountTurnover.initialDebt;
		this.finalDebt = otherMonthAccountTurnover.finalDebt;
		this.monthBlocked = otherMonthAccountTurnover.monthBlocked;
		for (Transaction transaction : otherMonthAccountTurnover.transactions) {
			Transaction tmp = new Transaction(transaction);
			this.transactions.add(tmp);
		}		
	}

	/**
	 * @param number The number of the runningTransaction to update
	 * @param runningTransaction The runningTransaction to update
	 */
	public void updateTranaction(int number, Transaction transaction) {
		if(this.transactions.get(number).equals(transaction))
			return;
		transactionsLoaded=true;
		transaction.setNumber(number);
		this.transactions.set(number,transaction);
		updateBalance();
	}
	
	/**
	 * @param runningTransaction The runningTransaction to append
	 */
	public void appendTranaction(Transaction transaction) {
		transactionsLoaded=true;
		transaction.setNumber(this.transactions.size());
		this.transactions.add(transaction);
		updateBalance();
	}
	
	/**
	 * @param number The number at that the runningTransaction should be inserted
	 * @param runningTransaction The runningTransaction to insert
	 */
	public void insertTranaction(int number, Transaction transaction) {
		transactionsLoaded=true;
		this.transactions.add(number,transaction);
		updateBalance();
	}

	/**
	 * @param number The number of the runningTransaction the should be removed
	 */
	public void removeTranaction(int number) {
		transactionsLoaded=true;
		this.transactions.remove(number);
		updateBalance();
	}
	
	/**
	 */
	public void removeAllTranactions() {
		transactionsLoaded=true;
		this.transactions.clear();
		this.lastChange=ZonedDateTime.now();
	}
	
	/**
	 * @return the transactions
	 */
	public List<Transaction> getTransactions() {
		transactionsLoaded=true;
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
		if(this.initialAssets==initialAssets ||
				( this.initialAssets!=null && this.initialAssets.equals(initialAssets) )
				)
			return;
		this.initialAssets = initialAssets; 
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @param initialDebt the initialDebt to set
	 */
	public void setInitialDebt(MonetaryAmount initialDebt) {
		if(this.initialDebt==initialDebt ||
				( this.initialDebt!=null && this.initialDebt.equals(initialDebt) )
				)
			return;
		this.initialDebt = initialDebt; 
		this.lastChange=ZonedDateTime.now();
	}
	
	/**
	 * @return the transactionsLoaded
	 */
	public boolean isTransactionsLoaded() {
		return transactionsLoaded;
	}

	/**
	 * Updates the internal accountings, reads all transactions if they were requested at any
	 * point by a getter, otherwise transactions are not checked. If any change to a transaction 
	 * did occur without a getter, setter or transaction manipulation method called this change
	 * is not reflected in the calculated balance. If transactions are skipped only a possible 
	 * change in the values of initial assets and debts is assumed.
	 *  
	 * @return true if any relevant change did occur
	 */
	public boolean updateBalance() {
		MonetaryAmount initialBalance=this.getInitialAssets().subtract(this.getInitialDebt());
		
		MonetaryAmount change=this.getMonthBalanceAssets().subtract(this.getMonthBalanceDebt());
		boolean changed=false;
		MonetaryAmountFactory<? extends MonetaryAmount> factory = 
				initialAssets.getFactory().setCurrency(initialAssets.getCurrency());
		if(transactionsLoaded) {
			transactions.sort(null);
			
			MonetaryAmount nBalanceAssets = factory.setNumber(0).create();
			MonetaryAmount nBalanceDebt = factory.setNumber(0).create();
			int i=0;
			for (Transaction transaction : transactions) {
				nBalanceAssets=nBalanceAssets.add(transaction.getReceipts());
				nBalanceDebt=nBalanceDebt.add(transaction.getSpending());
				if(transaction.getLastChange().isAfter(this.getLastChange())) {
					this.lastChange=transaction.getLastChange();
					changed=true;
				}
				++i;
				transaction.setNumber(i);
			}
			monthBalanceAssets=nBalanceAssets;
			monthBalanceDebt=nBalanceDebt;
			
			//We cannot set transactionsLoaded to false here,
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
		
		return monthBalanceAssets.subtract(monthBalanceDebt).subtract(change).isZero() && changed;
	}

	@Override
	public int compareTo(MonthAccountTurnover o) {
		return -o.getMonth().compareTo(month);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (int) (prime * result + ((account == null) ? 0 : account.getUid()));
		result = prime * result + ((finalAssets == null) ? 0 : finalAssets.hashCode());
		result = prime * result + ((finalDebt == null) ? 0 : finalDebt.hashCode());
		result = prime * result + ((initialAssets == null) ? 0 : initialAssets.hashCode());
		result = prime * result + ((initialDebt == null) ? 0 : initialDebt.hashCode());
		result = prime * result + ((month == null) ? 0 : month.hashCode());
		result = prime * result + ((monthBalanceAssets == null) ? 0 : monthBalanceAssets.hashCode());
		result = prime * result + ((monthBalanceDebt == null) ? 0 : monthBalanceDebt.hashCode());
		result = prime * result + (monthBlocked ? 1231 : 1237);
		result = prime * result + ((transactions == null) ? 0 : transactions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, true);
	}
		
	boolean equals(Object obj,boolean rec) {
		if (this == obj)
			return true;
		if (obj==null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MonthAccountTurnover other = (MonthAccountTurnover) obj;
		if (this.uid!=other.uid)
			return false;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (rec && !account.equals(other.account))
			return false;
		if (finalAssets == null) {
			if (other.finalAssets != null)
				return false;
		} else if (!finalAssets.equals(other.finalAssets))
			return false;
		if (finalDebt == null) {
			if (other.finalDebt != null)
				return false;
		} else if (!finalDebt.equals(other.finalDebt))
			return false;
		if (initialAssets == null) {
			if (other.initialAssets != null)
				return false;
		} else if (!initialAssets.equals(other.initialAssets))
			return false;
		if (initialDebt == null) {
			if (other.initialDebt != null)
				return false;
		} else if (!initialDebt.equals(other.initialDebt))
			return false;
		if (month == null) {
			if (other.month != null)
				return false;
		} else if (!month.equals(other.month))
			return false;
		if (monthBalanceAssets == null) {
			if (other.monthBalanceAssets != null)
				return false;
		} else if (!monthBalanceAssets.equals(other.monthBalanceAssets))
			return false;
		if (monthBalanceDebt == null) {
			if (other.monthBalanceDebt != null)
				return false;
		} else if (!monthBalanceDebt.equals(other.monthBalanceDebt))
			return false;
		if (monthBlocked != other.monthBlocked)
			return false;
		if (transactions == null) {
			if (other.transactions != null)
				return false;
		} else if (!transactions.equals(other.transactions))
			return false;
		return true;
	}

	/**
	 * @return the monthBlocked
	 */
	public boolean isMonthBlocked() {
		return monthBlocked;
	}

	/**
	 * @param monthBlocked the monthBlocked to set
	 */
	public void setMonthBlocked(boolean monthBlocked) {
		if(this.monthBlocked == monthBlocked)
			return;
		this.monthBlocked = monthBlocked;
		this.lastChange=ZonedDateTime.now();
	}
}
