package org.mcservice.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mcservice.javafx.AnnotationBasedFormatter.AnnotationBasedFilter;
import org.mcservice.javafx.table.TestTypes;

import javafx.scene.control.TextFormatter.Change;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;

@Tag("Table")
@Tag("GUI")
class AnnotationBasedFormatterTest{
	
	AnnotationBasedFormatter<TestTypes.Test3S2I,String> stringFormatter;
	AnnotationBasedFormatter<TestTypes.Test3S2I,Integer> integerFormatter;
	
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
		verify(changeMock,times(1)).getControlNewText();    	
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
		verify(changeMock,times(1)).getControlNewText();    	
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
		verify(changeMock,times(1)).getControlNewText();
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
		
		when(changeMock.getControlNewText()).thenReturn("Theo1");
		
		assertEquals(changeMock,stringFormatter.getFilter().apply(changeMock));
		verify(changeMock,times(2)).getControlNewText();
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
		verify(changeMock,times(1)).getControlNewText();
    }
    
}
