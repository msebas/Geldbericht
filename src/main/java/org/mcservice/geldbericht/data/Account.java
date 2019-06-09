package org.mcservice.geldbericht.data;

import java.time.ZonedDateTime;

public class Account extends DataObject {
	
	String accountNumber=null;
	String accountName=null;
	
	/**
	 * @param uid
	 * @param lastChange
	 * @param accountNumber
	 * @param accountName
	 */
	public Account(Long uid, ZonedDateTime lastChange, String accountNumber, String accountName) {
		super(uid,lastChange);
		this.accountNumber = accountNumber;
		this.accountName = accountName;
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
