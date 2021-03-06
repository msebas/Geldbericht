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

import javax.money.MonetaryAmount;
import javax.money.MonetaryAmountFactory;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.mcservice.geldbericht.data.converters.MonetaryAmountConverter;
import org.mcservice.javafx.TrimStringConverter;
import org.mcservice.javafx.control.table.TableViewColumn;
import org.mcservice.javafx.control.table.TableViewColumnOrder;
import org.mcservice.javafx.control.table.TableViewConverter;
import org.mcservice.javafx.control.table.TableViewFinalIfNotNull;

import org.hibernate.validator.constraints.Range;

import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;


@Entity
@Table(name = "Accounts")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Account extends AbstractDataObject{
	
	
	@Pattern(regexp = "[A-Za-z0-9]{5}")
	@TableViewColumn(colName="Kontennummer")
	@TableViewColumnOrder(10)
	@TableViewConverter(converter=TrimStringConverter.class)
	protected String accountNumber=null;
	
	@Size(min = 2, max = 255)
	@TableViewColumn(colName="Kontenname")
	@TableViewColumnOrder(20)
	@TableViewConverter(converter=TrimStringConverter.class)
	protected String accountName=null;
	
	@TableViewColumn(colName="Kontostand", editable=false)
	@TableViewColumnOrder(30)
	@Convert(converter = MonetaryAmountConverter.class)
	protected MonetaryAmount balance;
	
	@TableViewColumn(colName="Initialer Kontostand")
	@TableViewColumnOrder(40)
	@TableViewFinalIfNotNull("getUid")
	@Range(min=0)
	@Convert(converter = MonetaryAmountConverter.class)
	protected MonetaryAmount initialBalance;

	@OneToMany(mappedBy="account", fetch = FetchType.LAZY)
	@OrderBy("month ASC")
	protected List<MonthAccountTurnover> balanceMonths=new ArrayList<MonthAccountTurnover>();
	@ManyToOne
	protected Company company=null;
	
	@Transient
	protected boolean monthsLoaded=false;
	@Transient
	protected boolean changed=false;
	
	private Account() {
		super(null,ZonedDateTime.now());
	}
		
	/**
	 * @param uid
	 * @param lastChange
	 * @param accountNumber
	 * @param accountName
	 */
	public Account(Long uid, ZonedDateTime lastChange, String accountNumber, String accountName,
			MonetaryAmount initialBalance, Company company, List<MonthAccountTurnover> balanceMonths) {
		super(uid,lastChange);
		this.accountNumber = accountNumber;
		this.accountName = accountName;
		this.initialBalance=initialBalance;
		this.balance=initialBalance;
		this.company=company;
		if(uid==null) {
			this.changed=true;
		}
		this.balanceMonths=balanceMonths;
		if(this.balanceMonths!=null) {
			this.monthsLoaded=true;
		}
			
	}
	
	
	public Account(Account otherAccount) {
		super(otherAccount.getUid(),otherAccount.lastChange);
		this.accountNumber = otherAccount.accountNumber;
		this.accountName = otherAccount.accountName;
		this.initialBalance=otherAccount.initialBalance;
		this.balance=otherAccount.balance;
		this.company=otherAccount.company;
		this.monthsLoaded=true;
		this.changed=otherAccount.changed;
		for (MonthAccountTurnover month : otherAccount.balanceMonths) {
			MonthAccountTurnover tmp = new MonthAccountTurnover(month);
			tmp.account=this;
			balanceMonths.add(tmp);
		}
	}
	
	/**
	 * @param uid
	 * @param lastChange
	 * @param accountNumber
	 * @param accountName
	 */
	public Account(String accountNumber, String accountName, MonetaryAmount initialBalance, Company company
			) {
		super(null);
		this.accountNumber = accountNumber;
		this.accountName = accountName;
		this.initialBalance=initialBalance;
		this.balance=initialBalance;
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
		this.changed=true;
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
		this.changed=true;
		this.accountName = accountName; 
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @param company the company to set
	 */
	public void setCompany(Company company) {
		if(this.company==company ||
				( this.company!=null && this.company.equals(company,false) )
				)
			return;
		this.changed=true;
		this.company = company; 
		this.lastChange=ZonedDateTime.now();
	}


	/**
	 * @param initialBalance the initialBalance to set
	 */
	public void setInitialBalance(MonetaryAmount initialBalance) {
		if(this.initialBalance == initialBalance)
			return;
		if(this.balanceMonths.size()>0) {
			throw new RuntimeException("Initial balance could not be changed after a month was booked.");
		}
		this.changed=true;
		this.balance=initialBalance;
		this.initialBalance = initialBalance;
		this.lastChange = ZonedDateTime.now();
	}
	
	/**
	 * @return the initialBalance
	 */
	public MonetaryAmount getInitialBalance() {
		return initialBalance;
	}

	/**
	 * @return the balance
	 */
	public MonetaryAmount getBalance() {
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

	/**
	 * @return the balanceMonths
	 */
	public List<MonthAccountTurnover> getBalanceMonths() {
		monthsLoaded=true;
		return balanceMonths;
	}

	/**
	 * @param balanceMonths the balanceMonths to set
	 */
	public void setBalanceMonths(List<MonthAccountTurnover> balanceMonths) {
		if(null==balanceMonths) {
			this.balanceMonths = balanceMonths;
			changed=true;
			return;
		}
		if(null==this.balanceMonths || !this.balanceMonths.containsAll(balanceMonths) || this.balanceMonths.size()!=balanceMonths.size()) {
			this.balanceMonths = balanceMonths;
			updateBalance();
		} else {
			this.balanceMonths = balanceMonths;
		}
		monthsLoaded=true;
		changed=true;
	}
	
	public void addBalanceMonth(MonthAccountTurnover turnover) {
		monthsLoaded=true;
		changed=true;
		balanceMonths.add(turnover);
		updateBalance();
		this.lastChange = ZonedDateTime.now();
	}
	
	/**
	 * Updates the internal balance, recursively updates all registered MonthAccountTurnover if 
	 * the list getter or setter were called , otherwise transactions are not checked. If any change to a transaction 
	 * did occur without a getter, setter or transaction manipulation method called this change
	 * is not reflected in the calculated balance. If transactions are skipped only a possible 
	 * change in the values of initial assets and debts is assumed.
	 *  
	 * @return newMonthBalance-oldMonthBalance (zero if no change did happen/cancel out), 
	 *         add it to your actual balance to update it.
	 */	
	public MonetaryAmount updateBalance() {
		if(null==balanceMonths) {
			return null;
		}
		if(!monthsLoaded) {
			return null;
		}
		
		balanceMonths.sort(null);
		MonetaryAmountFactory<? extends MonetaryAmount> factory = initialBalance.getFactory().setCurrency(initialBalance.getCurrency());
		
		MonetaryAmount nBalance = factory.setAmount(initialBalance).create();
		
		for (MonthAccountTurnover month:balanceMonths) {
			//First update 
			if(nBalance.isPositiveOrZero()) {
				month.setInitialAssets(nBalance);
				month.setInitialDebt(factory.setNumber(0).create());
			} else {
				month.setInitialAssets(factory.setNumber(0).create());
				month.setInitialDebt(nBalance.multiply(-1));
			}
			month.updateBalance();
			nBalance=nBalance.add(month.getMonthBalanceAssets())
					.subtract(month.getMonthBalanceDebt());
			
			if(month.isChanged())
				changed=true;
			if(month.getLastChange().isAfter(this.getLastChange())) {
				this.lastChange=month.getLastChange();
			}
		}
		
		MonetaryAmount result=nBalance.subtract(balance);
		balance=nBalance;
		
		return result;
	}

	@Override
	public int hashCode() {
		return (int) (getUid()==null ? 0 : getUid());
	}

	/**
     * Indicates whether some other object is "identical to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * similar to the equals method of {@code Object} implementation,
     * taking only identical object instances and the {@code uid} into 
     * account. Use {@code equals(Object obj,boolean rec)} with 
     * {@code rec} set to {@code true} to make a deep comparison.
     *
     * @param   obj   the reference object with which to compare.
     * @return  {@code true} if this object is the same as the obj
     *          argument; {@code false} otherwise.
     * @see     #hashCode()
     * @see     java.util.HashMap
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if(obj==null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
				
		if (getUid()==other.getUid()) {
			return true;
		}
		
		return false;
	}
			
	public boolean equals(Object obj,boolean rec) {
		if (this == obj)
			return true;
		if(obj==null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
				
		if (getUid()!=other.getUid()) {
			return false;
		}
		if (accountName == null) {
			if (other.accountName != null)
				return false;
		} else if (!accountName.equals(other.accountName))
			return false;
		if (accountNumber == null) {
			if (other.accountNumber != null)
				return false;
		} else if (!accountNumber.equals(other.accountNumber))
			return false;
		if (balance == null) {
			if (other.balance != null)
				return false;
		} else if (!balance.equals(other.balance))
			return false;
		if(rec) {
			if (balanceMonths == null || other.balanceMonths==null) {
				if (other.balanceMonths != balanceMonths)
					return false;
			} else {
				//This has to be done manual, because otherwise we run into 
				//infinite recursive call problems because of backreferences
				if (balanceMonths.size()!=other.balanceMonths.size()) {
					return false;
				}
				Iterator<MonthAccountTurnover> otherIterator = other.balanceMonths.iterator();
				for (Iterator<MonthAccountTurnover> iterator = balanceMonths.iterator(); iterator.hasNext();) {
					iterator.next().equals(otherIterator.next(), false);
				}
			}
		}
		if (company == null) {
			if (other.company != null)
				return false;
		} else if (rec && !company.equals(other.company,false))
			return false;
		if (initialBalance == null) {
			if (other.initialBalance != null)
				return false;
		} else if (!initialBalance.equals(other.initialBalance))
			return false;
		return true;
	}

	public boolean isMonthsLoaded() {
		return monthsLoaded;
	}
	
	public boolean isChanged() {
		return changed;
	}
	
	public class AccountDatabaseQueueEntry extends AbstractDataObjectDatabaseQueueEntry{

		protected AccountDatabaseQueueEntry(Account stateToPersist, boolean delete) {
			super(stateToPersist,delete);
		}

		public void addToCompany(Company company) {
			if(stateToPersist instanceof Account) {
				company.getAccounts().add((Account) stateToPersist);
			}
		}
		
		@Override
		public void applyPersistedState(AbstractDataObject persistedState) {
			super.applyPersistedState(persistedState);
			if(changed) {
				if(Account.this.monthsLoaded) {
					if(Account.this.equals(persistedState,true)) {
						changed=false;
					}
				} else {
					if(Account.this.equals(persistedState,false)) {
						changed=false;
					}
				}
			}
		}
	}

	@Override
	public List<AbstractDataObjectDatabaseQueueEntry> getPersistingList() {
		return getPersistingList(false);
	}
	
	List<AbstractDataObjectDatabaseQueueEntry> getPersistingList(boolean forceAddAccount) {
		LinkedList<AbstractDataObjectDatabaseQueueEntry> res=new LinkedList<>();
		updateBalance();
		
		List<MonthAccountTurnover> months = null;
		if(monthsLoaded) {
			months = addChangedMonthsAndGetMonthList(res);
		}
		
		if(forceAddAccount || changed) {
			Account persistAccount=new Account(getUid(), lastChange, accountNumber, accountName, initialBalance, company,null);
			persistAccount.balance=balance;
			persistAccount.balanceMonths=null==months?balanceMonths:months;
			res.add(new AccountDatabaseQueueEntry(persistAccount,false));
		}
		
		if(res.size()>0) {
			return res;
		} else {
			return null;
		}
	}

	List<MonthAccountTurnover> addChangedMonthsAndGetMonthList(LinkedList<AbstractDataObjectDatabaseQueueEntry> res) {
		List<MonthAccountTurnover> months;
		months=new ArrayList<>();
		for (MonthAccountTurnover month : balanceMonths) {
			List<Transaction> transactions = null;
			if(month.isTransactionsLoaded()) {
				transactions=new ArrayList<>();
				for (Transaction transaction : month.getTransactions()) {
					Transaction persistTransaction = new Transaction(transaction);
					if(res!=null && transaction.isChanged()) {
						res.add(transaction.new TransactionDatabaseQueueEntry(persistTransaction,false));
					}
					transactions.add(persistTransaction);
				}
			}
			MonthAccountTurnover persistMonth=new MonthAccountTurnover(month, transactions);
			if(res!=null && month.isChanged()) {
				res.add(month.new MonthAccountTurnoverDatabaseQueueEntry(persistMonth,false));
			}
			months.add(persistMonth);
		}
		return months;
	}

	@Override
	public List<AbstractDataObjectDatabaseQueueEntry> getDeleteList() {
		LinkedList<AbstractDataObjectDatabaseQueueEntry> res=new LinkedList<>();
		
		for (MonthAccountTurnover month : balanceMonths) {
			res.addAll(month.getDeleteList());
		}
		if(this.getUid()!=null) {
			Account tmp = new Account(this);
			tmp.balanceMonths.clear();
			AccountDatabaseQueueEntry me = new AccountDatabaseQueueEntry(tmp,true);
			res.add(me);
		}
				
		if(res.size()>0) {
			return res;
		} else {
			return null;
		}
	}
	
}
