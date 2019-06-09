package org.mcservice.geldbericht.data;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.mcservice.geldbericht.data.Amortisation.AmortisationType;

public class Transaction  extends DataObject {
	
	int number=0;
	
	int receipts=0;
	int spending=0;
	Short accountingContraAccount=null;
	Short accountingConstGroup=null;
	Short accountingCostCenter=null;
	String voucher=null;
	LocalDate transactionDate=null;
	VatType vat=null;
	Long inventoryNumber=null;
	AmortisationType amortisationType=null;
	Long amortisationValue=null;
	String descriptionOfTransaction=null;	

	/**
	 * @param uid
	 * @param lastChange
	 * @param receipts
	 * @param spending
	 * @param accountingContraAccount
	 * @param accountingConstGroup
	 * @param accountingCostCenter
	 * @param voucher
	 * @param transactionDate
	 * @param vat
	 * @param inventoryNumber
	 * @param amortisationType
	 * @param amortisationValue
	 * @param descriptionOfTransaction
	 */
	public Transaction(Long uid, ZonedDateTime lastChange, int number, int receipts,
			int spending, Short accountingContraAccount, Short accountingConstGroup, Short accountingCostCenter,
			String voucher, LocalDate transactionDate, VatType vat, Long inventoryNumber,
			AmortisationType amortisationType, Long amortisationValue, String descriptionOfTransaction) {
		super(uid, lastChange);
		this.number=number;
		this.receipts = receipts;
		this.spending = spending;
		this.accountingContraAccount = accountingContraAccount;
		this.accountingConstGroup = accountingConstGroup;
		this.accountingCostCenter = accountingCostCenter;
		this.voucher = voucher;
		this.transactionDate = transactionDate;
		this.vat = vat;
		this.inventoryNumber = inventoryNumber;
		this.amortisationType = amortisationType;
		this.amortisationValue = amortisationValue;
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
	public int getReceipts() {
		return receipts;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param receipts the receipts to set
	 */
	public void setReceipts(int receipts) {
		if(this.receipts==receipts)
			return;
		this.receipts = receipts;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @return the spending
	 */
	public int getSpending() {
		return spending;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param spending the spending to set
	 */
	public void setSpending(int spending) {
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
	 * @return the accountingConstGroup
	 */
	public Short getAccountingConstGroup() {
		return accountingConstGroup;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param accountingConstGroup the accountingConstGroup to set
	 */
	public void setAccountingConstGroup(Short accountingConstGroup) {
		if(this.accountingConstGroup==accountingConstGroup ||
				( this.accountingConstGroup!=null && this.accountingConstGroup.equals(accountingConstGroup) )
				)
			return;
		this.accountingConstGroup = accountingConstGroup;
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
	 * @return the amortisationType
	 */
	public AmortisationType getAmortisationType() {
		return amortisationType;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param amortisationType the amortisationType to set
	 */
	public void setAmortisationType(AmortisationType amortisationType) {
		if(this.amortisationType==amortisationType ||
				( this.amortisationType!=null && this.amortisationType.equals(amortisationType) )
				)
			return;
		this.amortisationType = amortisationType;
		this.lastChange=ZonedDateTime.now();
	}
	
	/**
	 * @return the amortisationValue
	 */
	public Long getAmortisationValue() {
		return amortisationValue;
	}


	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param amortisationValue the amortisationValue to set
	 */
	public void setAmortisationValue(Long amortisationValue) {
		if(this.amortisationValue==amortisationValue ||
				( this.amortisationValue!=null && this.amortisationValue.equals(amortisationValue) )
				)
			return;
		this.amortisationValue = amortisationValue;
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
