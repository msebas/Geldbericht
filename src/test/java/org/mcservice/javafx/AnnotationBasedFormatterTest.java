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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeast;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.atLeastOnce;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mcservice.javafx.AnnotationBasedFormatter.AnnotationBasedFilter;
import org.mcservice.javafx.control.table.TestTypes;

import javafx.scene.control.TextFormatter.Change;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;

@Tag("Table")
@Tag("GUI")
class AnnotationBasedFormatterTest{
	
	AnnotationBasedFormatter<TestTypes.Test3S2I,String> stringFormatter;
	AnnotationBasedFormatter<TestTypes.Test3S2I,Integer> integerFormatter;
	AnnotationBasedFormatter<TestTypes.Test1S1M,Money> moneyFormatter;
	
	@BeforeEach
	public void setUp() {
	}
        
	@Test
    public void checkConstructorStringDefault() throws Exception {
    	stringFormatter = new AnnotationBasedFormatter<TestTypes.Test3S2I,String>(
    			TestTypes.Test3S2I.class.getDeclaredField("firstString"),
    			TestTypes.Test3S2I.class,"empty");
		
    	assertEquals("empty",stringFormatter.getValue());
    	assertTrue(stringFormatter.getValueConverter() instanceof TrimStringConverter);
    	assertTrue(stringFormatter.getFilter() instanceof AnnotationBasedFilter);
    }
    
	@Test
    public void checkConstructorStringDifferentConverter() throws Exception {
    	stringFormatter = new AnnotationBasedFormatter<TestTypes.Test3S2I,String>(
    			TestTypes.Test3S2I.class.getDeclaredField("thirdString"),
    			TestTypes.Test3S2I.class,"different");
		
    	assertEquals("different",stringFormatter.getValue());    
    	assertTrue(stringFormatter.getValueConverter() instanceof DefaultStringConverter);
    }

	@Test
    public void checkConstructorIntConverter() throws Exception {
    	integerFormatter = new AnnotationBasedFormatter<TestTypes.Test3S2I,Integer>(
    			TestTypes.Test3S2I.class.getDeclaredField("firstInt"),
    			TestTypes.Test3S2I.class,3);
		
    	assertEquals(3,integerFormatter.getValue());    
    	assertTrue(integerFormatter.getValueConverter() instanceof IntegerStringConverter);
    }
	
	@Test
    public void checkConstructorIntegerConverter() throws Exception {
    	integerFormatter = new AnnotationBasedFormatter<TestTypes.Test3S2I,Integer>(
    			TestTypes.Test3S2I.class.getDeclaredField("secondInt"),
    			TestTypes.Test3S2I.class,null);
		
    	assertEquals(null,integerFormatter.getValue());    
    	assertTrue(integerFormatter.getValueConverter() instanceof IntegerStringConverter);
    }
	
	@Test
    public void checkConstructorThrowExceptionNoConverter(){
    	assertThrows(RuntimeException.class,() -> new AnnotationBasedFormatter<TestTypes.Test2O,Object>(
    			TestTypes.Test2O.class.getDeclaredField("firstObject"),
    			TestTypes.Test2O.class,null));
    }
	
	@Test
    public void checkConstructorThrowExceptionCastConverter(){
    	assertThrows(ClassCastException.class,() -> new AnnotationBasedFormatter<TestTypes.Test2O,Object>(
    			TestTypes.Test2O.class.getDeclaredField("secondObject"),
    			TestTypes.Test2O.class,null));
    }
	
	@Test
    public void checkVerificator() throws Exception {
    	integerFormatter = new AnnotationBasedFormatter<TestTypes.Test3S2I,Integer>(
    			TestTypes.Test3S2I.class.getDeclaredField("firstInt"),
    			TestTypes.Test3S2I.class,3);
		
    	assertFalse(integerFormatter.getVerificator().call(2));
    	assertTrue(integerFormatter.getVerificator().call(22));
    	assertFalse(integerFormatter.getVerificator().call(1000));
    	
    }
	
	@Test
    public void checkNullMatcher() throws Exception {
		AnnotationBasedFormatter<TestTypes.Test1S,String> stringFormatter;
		stringFormatter = new AnnotationBasedFormatter<TestTypes.Test1S,String>(
    			TestTypes.Test1S.class.getDeclaredField("firstString"),
    			TestTypes.Test1S.class,"different");
		
		Change changeMock = mock(Change.class);
		
		assertEquals(changeMock,stringFormatter.getFilter().apply(changeMock));
		verifyZeroInteractions(changeMock);    	
    }
	
	@Test
    public void checkMinMaxMatcherOK() throws Exception {
		AnnotationBasedFormatter<TestTypes.Test3S,String> stringFormatter;
		stringFormatter = new AnnotationBasedFormatter<TestTypes.Test3S,String>(
    			TestTypes.Test3S.class.getDeclaredField("firstString"),
    			TestTypes.Test3S.class,"different");
		
		Change changeMock = mock(Change.class);
		when(changeMock.getControlNewText()).thenReturn("Name");
		
		assertEquals(changeMock,stringFormatter.getFilter().apply(changeMock));
		verify(changeMock,atLeastOnce()).getControlNewText();    	
    }
	
	@Test
    public void checkMinMaxMatcherHitEnd() throws Exception {
		AnnotationBasedFormatter<TestTypes.Test3S,String> stringFormatter;
		stringFormatter = new AnnotationBasedFormatter<TestTypes.Test3S,String>(
    			TestTypes.Test3S.class.getDeclaredField("firstString"),
    			TestTypes.Test3S.class,"different");
		
		Change changeMock = mock(Change.class);
		when(changeMock.getControlNewText()).thenReturn("");
		
		assertEquals(changeMock,stringFormatter.getFilter().apply(changeMock));
		verify(changeMock,atLeastOnce()).getControlNewText();    	
    }

	@Test
    public void checkMinMaxMatcherNotOK() throws Exception {
		AnnotationBasedFormatter<TestTypes.Test3S,String> stringFormatter;
		stringFormatter = new AnnotationBasedFormatter<TestTypes.Test3S,String>(
    			TestTypes.Test3S.class.getDeclaredField("firstString"),
    			TestTypes.Test3S.class,"different");
		
		Change changeMock = mock(Change.class);
		
		when(changeMock.getControlNewText()).thenReturn("1".repeat(257));
		
		assertEquals(null,stringFormatter.getFilter().apply(changeMock));
		verify(changeMock,times(1)).getControlNewText();
    }
	

	@Test
    public void checkPatternMatcherNoEditCallback() throws Exception {
		AnnotationBasedFormatter<TestTypes.Test3S,String> stringFormatter;
		stringFormatter = new AnnotationBasedFormatter<TestTypes.Test3S,String>(
    			TestTypes.Test3S.class.getDeclaredField("secondString"),
    			TestTypes.Test3S.class,"");
		
		Change changeMock = mock(Change.class);
		
		when(changeMock.getControlNewText()).thenReturn("Theo1");
		
		assertEquals(changeMock,stringFormatter.getFilter().apply(changeMock));
		verify(changeMock,atLeastOnce()).getControlNewText();
    }
	
	@Test
    public void checkPatternMatcherEditCallback() throws Exception {
		AnnotationBasedFormatter<TestTypes.Test3S,String> stringFormatter;
		stringFormatter = new AnnotationBasedFormatter<TestTypes.Test3S,String>(
    			TestTypes.Test3S.class.getDeclaredField("secondString"),
    			TestTypes.Test3S.class,"");
		
		@SuppressWarnings("unchecked")
		Consumer<String> callbackMock = mock(Consumer.class);
		stringFormatter.setCallback(callbackMock);
		
		Change changeMock = mock(Change.class);
		when(changeMock.isAdded()).thenReturn(true);
		
		when(changeMock.getControlNewText()).thenReturn("Theo1");
		
		assertEquals(changeMock,stringFormatter.getFilter().apply(changeMock));
		verify(changeMock,atLeast(1)).getControlNewText();
		verify(callbackMock,times(1)).accept("Theo1");
    }
	
    static Stream<Arguments> integerVariants() {
        return Stream.of(
        		Arguments.of("T",false),
        		Arguments.of("t",false),
        		Arguments.of("a",false),
        		Arguments.of("x",false),
        		Arguments.of("0",false),
        		Arguments.of(",",false),
        		Arguments.of("%",false),
        		Arguments.of("€",false),
        		Arguments.of("3,7",false),
        		Arguments.of("37a",false),
        		Arguments.of("1",true),
        		Arguments.of("233",true),
        		Arguments.of("30",true)
        		);
    }
        
    @ParameterizedTest
    @MethodSource("integerVariants")
    public void checkIntegerMatcherFails(String tstChars, boolean valid) throws Exception {
    	integerFormatter = new AnnotationBasedFormatter<TestTypes.Test3S2I,Integer>(
    			TestTypes.Test3S2I.class.getDeclaredField("secondInt"),
    			TestTypes.Test3S2I.class,null);
		
    	Change changeMock = mock(Change.class);
		
		when(changeMock.getControlNewText()).thenReturn(tstChars);
		assertEquals(valid ? changeMock : null,integerFormatter.getFilter().apply(changeMock));
		verify(changeMock,atLeastOnce()).getControlNewText();
    }
    
    static Stream<Arguments> moneyVariants() {
        return Stream.of(
        		Arguments.of("1E2",false),
        		Arguments.of("G",false),
        		//Arguments.of(" ",false),
        		Arguments.of("1.23.1",false),
        		Arguments.of("1.23,21 EUR",false),
        		Arguments.of("1,234",false),
        		//Arguments.of(",",false),
        		Arguments.of(".",false),
        		Arguments.of("€",false),
        		Arguments.of("1.123,34,34",false),
        		Arguments.of("1.123345,34",true),
        		Arguments.of("37a",false),
        		Arguments.of("0",true),
        		Arguments.of("1",true),
        		Arguments.of("0,1",true),
        		Arguments.of("1,0",true),
        		Arguments.of("0,00",true),
        		Arguments.of("1,09",true),
        		Arguments.of("23,0",true),
        		Arguments.of("233,0",true),
        		Arguments.of("1.000",true),
        		Arguments.of("1.066,0",true),
        		Arguments.of("1.065,00",true),
        		Arguments.of("19.000.066,0",true),
        		Arguments.of("123.054.706.000,67",true),
        		Arguments.of("1 EUR",true),
        		Arguments.of("1 €",true),
        		Arguments.of("1 USD",true),
        		Arguments.of("1 $",true),
        		Arguments.of("1,23 EUR",true),
        		Arguments.of("1,24 €",true),
        		Arguments.of(" 1234",true),
        		Arguments.of("1,25 USD",true)       		
        		);
    }
        
    @ParameterizedTest
    @MethodSource("moneyVariants")
    public void checkMoneyMatcherFails(String tstChars, boolean valid) throws Exception {
    	moneyFormatter = new AnnotationBasedFormatter<TestTypes.Test1S1M,Money>(
    			TestTypes.Test1S1M.class.getDeclaredField("firstMoney"),
    			TestTypes.Test1S1M.class,null);
		
    	Change changeMock = mock(Change.class);
		
		when(changeMock.getControlNewText()).thenReturn(tstChars);
		assertEquals(valid ? changeMock : null,moneyFormatter.getFilter().apply(changeMock));
		verify(changeMock,atLeastOnce()).getControlNewText();
    }
    
}
