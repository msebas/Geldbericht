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

class MonthYearConverterTest {

	MonthYearConverter cov=new MonthYearConverter();
	
	@Test
	void testToStringLocalDateNull() {
		assertNull(cov.toString(null));
	}
	
	@Test
	void testToStringLocalDateValue() {
		assertEquals("07.02",cov.toString(LocalDate.of(2002, 7, 1)));
	}

	@Test
	void testFromStringStringNull() {
		assertNull(cov.fromString(null));
	}
	
	@Test
	void testFromStringStringNoMatch1() {
		assertNull(cov.fromString("01.02.03"));
	}
	
	@Test
	void testFromStringStringNoMatch2() {
		assertNull(cov.fromString("01."));
	}
	
	static Stream<Arguments> getAllDates() {
	
        Arguments data[];
        data=new Arguments[100*12*4];
        for (int i = -50; i < 50; i++) {
        	for(int j=1; j<13;++j) {
            	LocalDate d=LocalDate.of(2000+i,j,1);
    			data[12*4*(50+i)+4*j-4]=Arguments.of(String.format("%02d.%02d",j,i<0?100+i:i),d);
    			data[12*4*(50+i)+4*j-3]=Arguments.of(String.format("%02d.%d",j,i<0?100+i:i),d);
    			data[12*4*(50+i)+4*j-2]=Arguments.of(String.format("%d.%02d",j,i<0?100+i:i),d);
    			data[12*4*(50+i)+4*j-1]=Arguments.of(String.format("%d.%d",j,i<0?100+i:i),d);
        	}
		}        
		return Stream.of(data);
    }
        
    @ParameterizedTest
    @MethodSource("getAllDates")
    public void testFromStringString(String s,LocalDate input) {
    	assertEquals(input,cov.fromString(s));
    }
	

	@Test
	void testBaseDate() {
		cov.setBaseDate(LocalDate.of(1987,1,1));
		assertEquals(LocalDate.of(1987,1,1),cov.getBaseDate());
	}

}
