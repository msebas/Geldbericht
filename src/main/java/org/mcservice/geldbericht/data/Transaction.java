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

import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Transactions")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Transaction  extends AbstractDataObject {
	
	int number=0;
	
	MonetaryAmount receipts;
	MonetaryAmount spending;
	Short accountingContraAccount=null;
	Short accountingCostGroup=null;
	Short accountingCostCenter=null;
	String voucher=null;
	LocalDate transactionDate=null;
	@ManyToOne(targetEntity=VatType.class)
	VatType vat=null;
	Long inventoryNumber=null;
	//AmortisationType amortisationType=null;
	//Long amortisationValue=null;
	String descriptionOfTransaction=null;	

	private Transaction() {
		super(null,ZonedDateTime.now());
	}
	
	/**
	 * @param uid
	 * @param lastChange
	 * @param receipts
	 * @param spending
	 * @param accountingContraAccount
	 * @param accountingCostGroup
	 * @param accountingCostCenter
	 * @param voucher
	 * @param transactionDate
	 * @param vat
	 * @param inventoryNumber
	 * @param amortisationType
	 * @param amortisationValue
	 * @param descriptionOfTransaction
	 */
	protected Transaction(Long uid, ZonedDateTime lastChange, int number, MonetaryAmount receipts,
			MonetaryAmount spending, Short accountingContraAccount, Short accountingCostGroup, Short accountingCostCenter,
			String voucher, LocalDate transactionDate, VatType vat, Long inventoryNumber,
			//AmortisationType amortisationType, Long amortisationValue, 
			String descriptionOfTransaction) {
		super(uid, lastChange);
		this.number=number;
		this.receipts = receipts;
		this.spending = spending;
		this.accountingContraAccount = accountingContraAccount;
		this.accountingCostGroup = accountingCostGroup;
		this.accountingCostCenter = accountingCostCenter;
		this.voucher = voucher;
		this.transactionDate = transactionDate;
		this.vat = vat;
		this.inventoryNumber = inventoryNumber;
		//this.amortisationType = amortisationType;
		//this.amortisationValue = amortisationValue;
		this.descriptionOfTransaction = descriptionOfTransaction;
	}
	
	/**
	 * @param uid
	 * @param receipts
	 * @param spending
	 * @param accountingContraAccount
	 * @param accountingCostGroup
	 * @param accountingCostCenter
	 * @param voucher
	 * @param transactionDate
	 * @param vat
	 * @param inventoryNumber
	 * @param amortisationType
	 * @param amortisationValue
	 * @param descriptionOfTransaction
	 */
	public Transaction(int number, MonetaryAmount receipts,
			MonetaryAmount spending, Short accountingContraAccount, Short accountingCostGroup, Short accountingCostCenter,
			String voucher, LocalDate transactionDate, VatType vat, Long inventoryNumber,
			//AmortisationType amortisationType, Long amortisationValue, 
			String descriptionOfTransaction) {
		super(null);
		this.number=number;
		this.receipts = receipts;
		this.spending = spending;
		this.accountingContraAccount = accountingContraAccount;
		this.accountingCostGroup = accountingCostGroup;
		this.accountingCostCenter = accountingCostCenter;
		this.voucher = voucher;
		this.transactionDate = transactionDate;
		this.vat = vat;
		this.inventoryNumber = inventoryNumber;
		//this.amortisationType = amortisationType;
		//this.amortisationValue = amortisationValue;
		this.descriptionOfTransaction = descriptionOfTransaction;
	}

	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @param number the number to set
	 */
	public void setNumber(int number) {
		if(this.number==number)
			return;
		this.number = number;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @return the receipts
	 */
	public MonetaryAmount getReceipts() {
		return receipts;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param receipts the receipts to set
	 */
	public void setReceipts(MonetaryAmount receipts) {
		if(this.receipts==receipts)
			return;
		this.receipts = receipts;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @return the spending
	 */
	public MonetaryAmount getSpending() {
		return spending;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param spending the spending to set
	 */
	public void setSpending(MonetaryAmount spending) {
		if(this.spending==spending)
			return;
		this.spending = spending;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @return the accountingContraAccount
	 */
	public Short getAccountingContraAccount() {
		return accountingContraAccount;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param accountingContraAccount the accountingContraAccount to set
	 */
	public void setAccountingContraAccount(Short accountingContraAccount) {
		if(this.accountingContraAccount==accountingContraAccount ||
				( this.accountingContraAccount!=null && this.accountingContraAccount.equals(accountingContraAccount) )
				)
			return;
		this.accountingContraAccount = accountingContraAccount;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @return the accountingCostGroup
	 */
	public Short getAccountingCostGroup() {
		return accountingCostGroup;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param accountingCostGroup the accountingCostGroup to set
	 */
	public void setAccountingCostGroup(Short accountingCostGroup) {
		if(this.accountingCostGroup==accountingCostGroup ||
				( this.accountingCostGroup!=null && this.accountingCostGroup.equals(accountingCostGroup) )
				)
			return;
		this.accountingCostGroup = accountingCostGroup;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @return the accountingCostCenter
	 */
	public Short getAccountingCostCenter() {
		return accountingCostCenter;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param accountingCostCenter the accountingCostCenter to set
	 */
	public void setAccountingCostCenter(Short accountingCostCenter) {
		if(this.accountingCostCenter==accountingCostCenter ||
				( this.accountingCostCenter!=null && this.accountingCostCenter.equals(accountingCostCenter) )
				)
			return;
		this.accountingCostCenter = accountingCostCenter;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @return the voucher
	 */
	public String getVoucher() {
		return voucher;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param voucher the voucher to set
	 */
	public void setVoucher(String voucher) {
		if(this.voucher==voucher ||
				( this.voucher!=null && this.voucher.equals(voucher) )
				)
			return;
		this.voucher = voucher;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @return the transactionDate
	 */
	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param transactionDate the transactionDate to set
	 */
	public void setTransactionDate(LocalDate transactionDate) {
		if(this.transactionDate==transactionDate ||
				( this.transactionDate!=null && this.transactionDate.equals(transactionDate) )
				)
			return;
		this.transactionDate = transactionDate;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @return the vat
	 */
	public VatType getVat() {
		return vat;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param vat the vat to set
	 */
	public void setVat(VatType vat) {
		if(this.vat==vat || ( this.vat!=null && this.vat.equals(vat) ) )
			return;
		this.vat = vat;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @return the inventoryNumber
	 */
	public Long getInventoryNumber() {
		return inventoryNumber;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param inventoryNumber the inventoryNumber to set
	 */
	public void setInventoryNumber(Long inventoryNumber) {
		if(this.inventoryNumber==inventoryNumber ||
				( this.inventoryNumber!=null && this.inventoryNumber.equals(inventoryNumber) )
				)
			return;
		this.inventoryNumber = inventoryNumber;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @return the descriptionOfTransaction
	 */
	public String getDescriptionOfTransaction() {
		return descriptionOfTransaction;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param descriptionOfTransaction the descriptionOfTransaction to set
	 */
	public void setDescriptionOfTransaction(String descriptionOfTransaction) {
		if(this.descriptionOfTransaction==descriptionOfTransaction ||
				( this.descriptionOfTransaction!=null && this.descriptionOfTransaction.equals(descriptionOfTransaction) )
				)
			return;
		this.descriptionOfTransaction = descriptionOfTransaction;
		this.lastChange=ZonedDateTime.now();
	}
	
}
