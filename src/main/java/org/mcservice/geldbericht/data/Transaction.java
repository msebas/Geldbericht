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

import javax.money.MonetaryAmount;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
import org.mcservice.geldbericht.data.converters.MonetaryAmountConverter;
import org.mcservice.geldbericht.data.converters.VatTypeStringConverter;
import org.mcservice.javafx.TrimStringConverter;
import org.mcservice.javafx.control.date.DayMonthFieldColumnFactory;
import org.mcservice.javafx.control.table.TableViewColumn;
import org.mcservice.javafx.control.table.TableViewColumnOrder;
import org.mcservice.javafx.control.table.TableViewConverter;
import org.mcservice.javafx.control.table.factories.SelectorColumnFactory;

@Entity
@Table(name = "Transactions")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Transaction extends AbstractDataObject implements Comparable<Transaction> {
	
	@TableViewColumn(colName="Nr.",editable = false)
	@TableViewColumnOrder(10)
	int number=0;
	
	@TableViewColumn(colName="Einnahmen")
	@TableViewColumnOrder(20)
	@Range(min=0)
	@Convert(converter = MonetaryAmountConverter.class)
	MonetaryAmount receipts;
	
	@TableViewColumn(colName="Ausgaben")
	@TableViewColumnOrder(25)
	@Range(min=0)
	@Convert(converter = MonetaryAmountConverter.class)
	MonetaryAmount spending;
	
	
	@Range(min=0,max=999999)
	@TableViewColumn(colName="Gegenkonto")
	@TableViewColumnOrder(30)
	Integer accountingContraAccount=null;
	
	@Range(min=0,max=99)
	@TableViewColumn(colName="KG")
	@TableViewColumnOrder(33)
	Integer accountingCostGroup=null;
	
	@Range(min=0,max=999)
	@TableViewColumn(colName="KST")
	@TableViewColumnOrder(36)
	Integer accountingCostCenter=null;
	
	@Size(max = 255)
	@TableViewColumn(colName="Beleg")
	@TableViewColumnOrder(40)
	@TableViewConverter(converter=TrimStringConverter.class)
	String voucher=null;
	
	@TableViewColumn(colName="Datum",fieldGenerator=DayMonthFieldColumnFactory.class)
	@TableViewColumnOrder(50)
	LocalDate transactionDate=null;
	
	@ManyToOne(targetEntity=VatType.class, fetch = FetchType.EAGER)
	@TableViewColumn(colName="Steuer",fieldGenerator=SelectorColumnFactory.class)
	@TableViewColumnOrder(60)
	@TableViewConverter(converter=VatTypeStringConverter.class)
	VatType vat=null;
		
	@TableViewColumn(colName="Inv.-Nr.")
	@TableViewColumnOrder(70)
	String inventoryNumber=null;
	//AmortisationType amortisationType=null;
	//Long amortisationValue=null;
	
	@Size(max = 255)
	@TableViewColumn(colName="Gegenstand der Buchung")
	@TableViewColumnOrder(90)
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
	public Transaction(Long uid, ZonedDateTime lastChange, int number, MonetaryAmount receipts,
			MonetaryAmount spending, Integer accountingContraAccount, Integer accountingCostGroup, Integer accountingCostCenter,
			String voucher, LocalDate transactionDate, VatType vat, String inventoryNumber,
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
			MonetaryAmount spending, Integer accountingContraAccount, Integer accountingCostGroup, Integer accountingCostCenter,
			String voucher, LocalDate transactionDate, VatType vat, String inventoryNumber,
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
		this.vat = new VatType(vat);
		this.inventoryNumber = inventoryNumber;
		//this.amortisationType = amortisationType;
		//this.amortisationValue = amortisationValue;
		this.descriptionOfTransaction = descriptionOfTransaction;
	}

	public Transaction(Transaction otherTransaction) {
		super(otherTransaction.uid, otherTransaction.lastChange);
		this.number=otherTransaction.number;
		this.receipts = otherTransaction.receipts;
		this.spending = otherTransaction.spending;
		this.accountingContraAccount = otherTransaction.accountingContraAccount;
		this.accountingCostGroup = otherTransaction.accountingCostGroup;
		this.accountingCostCenter = otherTransaction.accountingCostCenter;
		this.voucher = otherTransaction.voucher;
		this.transactionDate = otherTransaction.transactionDate;
		this.vat = otherTransaction.vat;
		this.inventoryNumber = otherTransaction.inventoryNumber;
		this.descriptionOfTransaction = otherTransaction.descriptionOfTransaction;
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
		this.number = number;
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
	public Integer getAccountingContraAccount() {
		return accountingContraAccount;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param accountingContraAccount the accountingContraAccount to set
	 */
	public void setAccountingContraAccount(Integer accountingContraAccount) {
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
	public Integer getAccountingCostGroup() {
		return accountingCostGroup;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param accountingCostGroup the accountingCostGroup to set
	 */
	public void setAccountingCostGroup(Integer accountingCostGroup) {
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
	public Integer getAccountingCostCenter() {
		return accountingCostCenter;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param accountingCostCenter the accountingCostCenter to set
	 */
	public void setAccountingCostCenter(Integer accountingCostCenter) {
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
	public String getInventoryNumber() {
		return inventoryNumber;
	}

	/**
	 * Sets lastChange to the actual time if the value is set
	 * 
	 * @param inventoryNumber the inventoryNumber to set
	 */
	public void setInventoryNumber(String inventoryNumber) {
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

	@Override
	public int compareTo(Transaction o) {
		return number-o.number;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((accountingContraAccount == null) ? 0 : accountingContraAccount.hashCode());
		result = prime * result + ((accountingCostCenter == null) ? 0 : accountingCostCenter.hashCode());
		result = prime * result + ((accountingCostGroup == null) ? 0 : accountingCostGroup.hashCode());
		result = prime * result + ((descriptionOfTransaction == null) ? 0 : descriptionOfTransaction.hashCode());
		result = prime * result + ((inventoryNumber == null) ? 0 : inventoryNumber.hashCode());
		result = prime * result + number;
		result = prime * result + ((receipts == null) ? 0 : receipts.hashCode());
		result = prime * result + ((spending == null) ? 0 : spending.hashCode());
		result = prime * result + ((transactionDate == null) ? 0 : transactionDate.hashCode());
		result = prime * result + ((vat == null) ? 0 : vat.hashCode());
		result = prime * result + ((voucher == null) ? 0 : voucher.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if(obj==null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Transaction other = (Transaction) obj;


		if (uid!=other.uid) {
			return false;
		}
		if (accountingContraAccount == null) {
			if (other.accountingContraAccount != null) {
				return false;
			}
		} else if (!accountingContraAccount.equals(other.accountingContraAccount)) {
			return false;
		}
		if (accountingCostCenter == null) {
			if (other.accountingCostCenter != null) {
				return false;
			}
		} else if (!accountingCostCenter.equals(other.accountingCostCenter)) {
			return false;
		}
		if (accountingCostGroup == null) {
			if (other.accountingCostGroup != null) {
				return false;
			}
		} else if (!accountingCostGroup.equals(other.accountingCostGroup)) {
			return false;
		}
		if (descriptionOfTransaction == null) {
			if (other.descriptionOfTransaction != null) {
				return false;
			}
		} else if (!descriptionOfTransaction.equals(other.descriptionOfTransaction)) {
			return false;
		}
		if (inventoryNumber == null) {
			if (other.inventoryNumber != null) {
				return false;
			}
		} else if (!inventoryNumber.equals(other.inventoryNumber)) {
			return false;
		}
		if (number != other.number) {
			return false;
		}
		if (receipts == null || other.receipts == null) {
			if (other.receipts != receipts) {
				return false;
			}
		} else if (!receipts.isEqualTo(other.receipts)) {
			return false;
		}
		if (spending == null || other.spending == null) {
			if (other.spending != spending) {
				return false;
			}
		} else if (!spending.isEqualTo(other.spending)) {
			return false;
		}
		if (transactionDate == null) {
			if (other.transactionDate != null) {
				return false;
			}
		} else if (!transactionDate.equals(other.transactionDate)) {
			return false;
		}
		if (vat == null) {
			if (other.vat != null) {
				return false;
			}
		} else if (!vat.equals(other.vat)) {
			return false;
		}
		if (voucher == null) {
			if (other.voucher != null) {
				return false;
			}
		} else if (!voucher.equals(other.voucher)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"Transaction [number=%s, receipts=%s, spending=%s, accountingContraAccount=%s, accountingCostGroup=%s, "
				+ "accountingCostCenter=%s, voucher=%s, transactionDate=%s, vat=%s, inventoryNumber=%s, "
				+ "descriptionOfTransaction=%s, uid=%s, lastChange=%s]",
				number, receipts, spending, accountingContraAccount, accountingCostGroup, 
				accountingCostCenter, voucher, transactionDate, vat, inventoryNumber, 
				descriptionOfTransaction, uid, lastChange);
	}
	
}
