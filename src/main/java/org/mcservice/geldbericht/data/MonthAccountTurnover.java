package org.mcservice.geldbericht.data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class MonthAccountTurnover extends DataObject {
	
	ArrayList<Transaction> transactions=new ArrayList<Transaction>();
	Company company=null;
	
	LocalDate month=null;
	Account account=null;
	int monthBalanceAssets=0;
	int initialAssets=0;
	int finalAssets=0;
	int monthBalanceDebt=0;
	int initialDebt=0;
	int finalDebt=0;
	
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
			Company company, LocalDate month, Account account, int monthBalanceAssets, int initialAssets,
			int finalAssets, int monthBalanceDebt, int initialDebt, int finalDebt) {
		super(uid,lastChange);
		this.transactions = transactions;
		this.company = company;
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
	 * @param company the company to set
	 */
	public void setCompany(Company company) {
		if(this.company==company || ( this.company!=null && this.company.equals(company) ) )
			return;
		this.company = company;
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
	public void setInitialAssets(int initialAssets) {
		if(this.initialAssets==initialAssets)
			return;
		this.initialAssets = initialAssets;
		this.lastChange=ZonedDateTime.now();
	}
	/**
	 * @param initialDebt the initialDebt to set
	 */
	public void setInitialDebt(int initialDebt) {
		if(this.initialDebt==initialDebt)
			return;
		this.initialDebt = initialDebt;
		this.lastChange=ZonedDateTime.now();
	}
	
	/**
	 * @return the transactions
	 */
	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}
	/**
	 * @return the company
	 */
	public Company getCompany() {
		return company;
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
	public int getMonthBalanceAssets() {
		return monthBalanceAssets;
	}
	/**
	 * @return the initialAssets
	 */
	public int getInitialAssets() {
		return initialAssets;
	}
	/**
	 * @return the finalAssets
	 */
	public int getFinalAssets() {
		return finalAssets;
	}
	/**
	 * @return the monthBalanceDebt
	 */
	public int getMonthBalanceDebt() {
		return monthBalanceDebt;
	}
	/**
	 * @return the initialDebt
	 */
	public int getInitialDebt() {
		return initialDebt;
	}
	/**
	 * @return the finalDebt
	 */
	public int getFinalDebt() {
		return finalDebt;
	}
}
