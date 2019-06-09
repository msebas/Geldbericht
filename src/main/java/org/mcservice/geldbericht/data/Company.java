package org.mcservice.geldbericht.data;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "Companys")
public class Company extends DataObject{
	
	ArrayList<Account> accounts=new ArrayList<Account>();
	
	String companyName=null;
	String companyNumber=null;
	String companyBookkeepingAppointment=null;
	
	/**
	 * @param uid
	 * @param lastChange
	 * @param accounts
	 * @param companyName
	 * @param companyNumber
	 * @param companyBookkeepingAppointment
	 */
	public Company(Long uid, ZonedDateTime lastChange, ArrayList<Account> accounts, String companyName,
			String companyNumber, String companyBookkeepingAppointment) {
		super(uid,lastChange);
		this.accounts = accounts;
		this.companyName = companyName;
		this.companyNumber = companyNumber;
		this.companyBookkeepingAppointment = companyBookkeepingAppointment;
	}

	/**
	 * @return the accounts
	 */
	public ArrayList<Account> getAccounts() {
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
