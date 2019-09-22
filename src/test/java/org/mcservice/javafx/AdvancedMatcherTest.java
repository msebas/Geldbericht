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
package org.mcservice.javafx;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdvancedMatcherTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void constructorAndPassthrough() {
		AdvancedMatcher matcher = new AdvancedMatcher("(12)34(56)");
		
		matcher.reset("1234");
		
		assertFalse(matcher.matches());
		assertTrue(matcher.hitEnd());
		
		matcher.reset("1234a");
		
		assertFalse(matcher.matches());
		assertFalse(matcher.hitEnd());
		
		matcher.reset("123456");
		
		assertTrue(matcher.matches());
		assertEquals(2,matcher.groupCount());
		assertEquals("12",matcher.group(1));
		assertEquals("56",matcher.group(2));
		
		matcher.reset("123456a");
		
		assertFalse(matcher.matches());
		assertFalse(matcher.hitEnd());
	}
	
	@Test
	void testRequireEndNoMatch() {
		AdvancedMatcher matcher = new AdvancedMatcher("[0-9]{0,}\\.[a-z]{1,3}");
		
		matcher.reset("01.");
		
		assertFalse(matcher.matches());
		assertFalse(matcher.requireEnd());
	}
	
	@Test
	void testRequireEndDefault() {
		AdvancedMatcher matcher = new AdvancedMatcher("[0-9]{0,}\\.[a-z]{1,3}");
		
		matcher.reset("01.a");
		
		assertTrue(matcher.matches());
		assertFalse(matcher.requireEnd());
		
		matcher.reset("01.ab");
		
		assertTrue(matcher.matches());
		assertFalse(matcher.requireEnd());
		
		matcher.reset("01.abc");
		
		assertTrue(matcher.matches());
		assertTrue(matcher.requireEnd());
	}

	@Test
	void testRequireEndSetList() {
		AdvancedMatcher matcher = new AdvancedMatcher("[0-9]{0,}\\.[a-z]{0,3}");
		
		matcher.setCompletions(List.of("0", "."));
		
		matcher.reset("01");
		
		assertFalse(matcher.matches());
		assertFalse(matcher.requireEnd());

		matcher.reset("01.");
		
		assertTrue(matcher.matches());
		assertTrue(matcher.requireEnd());
		
		matcher.reset("01.ac");
		
		assertTrue(matcher.matches());
		assertTrue(matcher.requireEnd());
	}
	
	@Test
	void testCompleteSquenceTrivials() {
		AdvancedMatcher matcher = new AdvancedMatcher("[0-9]{0,2}\\.[a-z]{0,3}");
		
		matcher.reset("01.a");
		assertNull(matcher.completeSquence());
		matcher.reset("01b");
		assertNull(matcher.completeSquence());
		matcher.reset("01");
		matcher.setCompletions(List.of());
		assertNull(matcher.completeSquence());
	}
	
	@Test
	void testCompleteSquenceDefault1() {
		AdvancedMatcher matcher = new AdvancedMatcher("[0-9]{0,2}\\.[a-d]{0,3}eber[5-9]");
		
		matcher.reset("0");
		assertEquals(null,matcher.completeSquence());
		
		matcher.reset("01");
		assertEquals(".",matcher.completeSquence());
		
		matcher.reset("01.ae");
		assertEquals("ber",matcher.completeSquence());
		
		matcher.reset("01.abc");
		assertEquals("eber",matcher.completeSquence());
	}
	
	@Test
	void testCompleteSquenceDefault2() {
		AdvancedMatcher matcher = new AdvancedMatcher("ab[0-9][a-d]{0,3}eber");
		
		matcher.reset("");
		assertEquals("ab",matcher.completeSquence());
		
		matcher.reset("ab");
		assertEquals(null,matcher.completeSquence());
		
		matcher.reset("ab0");
		assertEquals(null,matcher.completeSquence());
		
		matcher.reset("ab1ae");
		assertEquals("ber",matcher.completeSquence());
		
		matcher.reset("ab2abc");
		assertEquals("eber",matcher.completeSquence());
	}
	
	@Test
	void testCompleteSquenceSetList1() {
		AdvancedMatcher matcher = new AdvancedMatcher("[0-9]{0,2}\\.[a-d]{0,3}eber[5-9]");
		
		matcher.setCompletions(List.of("e", "b", "r", ".","a","b","0","1"));
		
		matcher.reset("01");
		assertEquals(".",matcher.completeSquence());
		
		matcher.reset("01.ae");
		assertEquals("ber",matcher.completeSquence());
		
		matcher.reset("01.abc");
		assertEquals("eber",matcher.completeSquence());
	}
	
	@Test
	void testCompleteSquenceSetList2() {
		AdvancedMatcher matcher = new AdvancedMatcher("ab[0-9][a-d]{0,3}eber");
		
		matcher.setCompletions(List.of("e", "b", "r", ".","a","1"));
		
		matcher.reset("");
		assertEquals("ab1",matcher.completeSquence());
		
		matcher.reset("ab");
		assertEquals("1",matcher.completeSquence());
		
		matcher.reset("ab0");
		assertEquals(null,matcher.completeSquence());
		
		matcher.reset("ab1ae");
		assertEquals("ber",matcher.completeSquence());
		
		matcher.reset("ab2abc");
		assertEquals("eber",matcher.completeSquence());
	}

}
