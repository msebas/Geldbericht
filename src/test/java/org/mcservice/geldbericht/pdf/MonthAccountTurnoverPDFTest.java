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
package org.mcservice.geldbericht.pdf;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.Transaction;
import org.mcservice.geldbericht.data.VatType;

class MonthAccountTurnoverPDFTest {
	
	MonthAccountTurnover month=null;
	
	@BeforeEach
	public void setup() {
		Company company=new Company(1L, ZonedDateTime.now(), null, "Company 1", "55555", "4444064444");
		Account account=new Account(2L, ZonedDateTime.now(), "4444", "Account 1", Money.of(2200, "EUR"),company);
		company.setAccounts(Collections.singletonList(account));
		VatType vat=new VatType("Full Vat Name", "12%", new BigDecimal(0.12), true);
		
		this.month=new MonthAccountTurnover(3L, ZonedDateTime.now(), new ArrayList<Transaction>(), 
				LocalDate.of(2019, 04, 01), account, Money.of(0, "EUR"), Money.of(0, "EUR"), 
				Money.of(0, "EUR"), Money.of(0, "EUR"), Money.of(0, "EUR"), Money.of(0, "EUR"));
		
		for (int i = 0; i < 46; i++) {
			month.getTransactions().add(new Transaction(i, Money.of(i<30?0:2*i*i, "EUR"), Money.of(i*i-i*4+14, "EUR"), 
					4444,22,333, String.format("Beleg %d",i), LocalDate.of(2019, 04+i/27, i%27+3), 
					vat, "9999L", String.format("description of transaction %d",i)));
		}
		
		account.addBalanceMonth(month);
		account.updateBalance();		
	}

	@Test
	void testCreatePdf() throws IOException {
		
		MonthAccountTurnoverPDF testObj=new MonthAccountTurnoverPDF(month);
		FileOutputStream fo=new FileOutputStream("/tmp/testout.pdf");
		fo.write(testObj.getPdf());
		fo.close();
	}

}
