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

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mcservice.geldbericht.data.converters.AccountStringConverter;
import org.mcservice.geldbericht.data.converters.CompanyStringConverter;
import org.mcservice.geldbericht.data.converters.VatTypeStringConverter;

import javafx.util.StringConverter;

class ConvertersTest {
	
	static Stream<Arguments> getConverters() {
        return Stream.of(
        		Arguments.of(AccountStringConverter.class),
        		Arguments.of(CompanyStringConverter.class),
        		Arguments.of(VatTypeStringConverter.class));
    }
        
	@ParameterizedTest
    @MethodSource("getConverters")
    public void checkThrow(Class<? extends StringConverter<?>> clazz) throws Exception {
    	StringConverter<?> st=clazz.getConstructor().newInstance();
    	assertThrows(RuntimeException.class,() -> st.fromString(null));
    	assertThrows(RuntimeException.class,() -> st.fromString("something"));
    }
	
	@ParameterizedTest
    @MethodSource("getConverters")
    public void checkNull(Class<? extends StringConverter<?>> clazz) throws Exception {
    	StringConverter<?> st=clazz.getConstructor().newInstance();
    	assertNull(st.toString(null));
    }

	@Test
	void checkAccountFormat() {
		AccountStringConverter st=new AccountStringConverter();
		Account acc=new Account("number", "name", null,null);
		assertEquals("name (number)",st.toString(acc));
	}
	
	@Test
	void checkCompanyFormat() {
		CompanyStringConverter st=new CompanyStringConverter();
		Company company=new Company("name", "number", null);
		assertEquals("name (Nr.: number)",st.toString(company));
	}
	
	@Test
	void checkVatTypeFormat() {
		VatTypeStringConverter st=new VatTypeStringConverter();
		VatType vat=new VatType("name", null, BigDecimal.valueOf(4.5),null);
		assertEquals("name (4,50%)",st.toString(vat));
	}

}
