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
package org.mcservice.javafx.control.date;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DayMonthConverterTest {

	DayMonthConverter cov=new DayMonthConverter();
	
	@Test
	void testToStringLocalDateNull() {
		assertNull(cov.toString(null));
	}
	
	@Test
	void testToStringLocalDateValue() {
		assertEquals("07.02",cov.toString(LocalDate.of(2000, 2, 7)));
	}

	@Test
	void testFromStringStringNull() {
		assertNull(cov.fromString(null));
	}
	
	@Test
	void testFromStringStringNoMatch1() {
		assertThrows(RuntimeException.class,() -> cov.fromString("01.02.03"));
	}
	
	@Test
	void testFromStringStringNoMatch2() {
		assertThrows(RuntimeException.class,() -> cov.fromString("01."));
	}
	
	static Stream<Arguments> getAllDates() {
	
        Arguments data[];
        data=new Arguments[366*4];
        for (int i = 0; i < data.length/4; i++) {
        	LocalDate d=LocalDate.ofYearDay(2008, i+1);
			data[4*i+0]=Arguments.of(String.format("%td.%tm",d,d),d);
			data[4*i+1]=Arguments.of(String.format("%td.%d",d,d.getMonthValue()),d);
			data[4*i+2]=Arguments.of(String.format("%te.%tm",d,d),d);
			data[4*i+3]=Arguments.of(String.format("%te.%d",d,d.getMonthValue()),d);
		}        
		return Stream.of(data);
    }
        
    @ParameterizedTest
    @MethodSource("getAllDates")
    public void testFromString(String s,LocalDate input) {
    	cov.setBaseDate(LocalDate.ofYearDay(2012, 1));
    	LocalDate result=LocalDate.ofYearDay(2012, input.getDayOfYear());
    	assertEquals(result,cov.fromString(s));
    }
	

	@Test
	void testBaseDate() {
		cov.setBaseDate(LocalDate.of(1987,1,1));
		assertEquals(LocalDate.of(1987,1,1),cov.getBaseDate());
	}

}
