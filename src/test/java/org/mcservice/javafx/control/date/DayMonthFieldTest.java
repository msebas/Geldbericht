package org.mcservice.javafx.control.date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


@Tag("Active")
@Tag("GUI")
class DayMonthFieldTest extends ApplicationTest{
		
	DayMonthField dayMonthPicker = null ;
	
	@Override 
	public void start(Stage stage) throws Exception {
		Scene scene = new Scene(new Group());
		
		stage.setTitle("Reflection Table View Test");
        stage.setWidth(500);
        stage.setHeight(300);
        
        dayMonthPicker = new DayMonthField();
        		
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().add(dayMonthPicker);
        
        ((Group) scene.getRoot()).getChildren().addAll(vbox);
		
        stage.setScene(scene);
        stage.show();
    }
    

    @Tag("Active")
	@Test
	@Disabled
    public void manualTest() {
    	//Wait 1 h, then continue
    	while(true) {
    		sleep(1000);
    		try {
    			System.out.println(dayMonthPicker.getStyleClass().contains("field-validation-error"));
    			System.out.println(dayMonthPicker.getDate());
    		} catch (Exception e){
    			e.printStackTrace();
    		}
    	}
    	
    }
	
	@Test
    public void checkSetupTest() {
    	assertEquals("DD.MM",dayMonthPicker.getPromptText());
    	assertTrue(dayMonthPicker.getStyleClass().contains("field-validation-error"));
    }
	
	static Stream<Arguments> insertCharsValues() {
        return Stream.of(
        		Arguments.of("AEIQYZckuyz;:!",false,null),
        		Arguments.of("12.12",true,LocalDate.of(2017,12,12)),
        		Arguments.of("1.12",true,LocalDate.of(2017,12,1)),
        		Arguments.of("12.1",true,LocalDate.of(2017,1,12)),
        		Arguments.of("31.12",true,LocalDate.of(2017,12,31)),
        		Arguments.of("05.09",true,LocalDate.of(2017,9,5)),
        		Arguments.of("4.08",true,LocalDate.of(2017,8,4)),
        		Arguments.of("03.7",true,LocalDate.of(2017,7,3)),
        		Arguments.of("2.6",true,LocalDate.of(2017,6,2))
        		);
    }
        
    @ParameterizedTest
    @MethodSource("insertCharsValues")
    public void insertCharsTest(String inv,boolean valid,LocalDate result) {
    	dayMonthPicker.setYear(2017);
    	clickOn(dayMonthPicker);
    	Pattern p=Pattern.compile("[0-9]{1,2}.[0-9]{1,2}");
    	
    	for (int j = 0; j < inv.length(); j++) {
    		write(String.valueOf(inv.charAt(j)),2);
    		String actSub=inv.substring(0,j+1);
			if(!p.matcher(actSub).matches()) {
				assertTrue(dayMonthPicker.getStyleClass().contains("field-validation-error"),"Substring: ".concat(actSub));
				assertNull(dayMonthPicker.getDate(),"Substring: ".concat(actSub));
			}
	    	assertEquals(actSub.replaceAll("[^\\d]",""),dayMonthPicker.getText().replaceAll("[^\\d]",""),"Substring: ".concat(actSub));
    		
		}
    	
    	if(valid) {
    		assertFalse(dayMonthPicker.getStyleClass().contains("field-validation-error"));
    		assertEquals(result,dayMonthPicker.getDate());
    	}
    	
    }
    
    static Stream<Arguments> insertCharsNoIntermediateValues() {
        return Stream.of(
        		Arguments.of("ase12.10",LocalDate.of(2017,10,12)),
        		Arguments.of("12.13",LocalDate.of(2017,1,12)),
        		Arguments.of("113",LocalDate.of(2017,3,11)),
        		Arguments.of("1213",LocalDate.of(2017,1,12)),
        		Arguments.of("32.11",LocalDate.of(2017,11,3))
        		);
    }

    @Tag("Active")
    @ParameterizedTest
    @MethodSource("insertCharsNoIntermediateValues")
    public void insertCharsNoIntermediateTest(String inv,LocalDate result) {
    	dayMonthPicker.setYear(2017);
    	clickOn(dayMonthPicker);
    	write(inv);
    	
		assertFalse(dayMonthPicker.getStyleClass().contains("field-validation-error"));
		assertEquals(result,dayMonthPicker.get());
    }
    
    static Stream<Arguments> setDifferentDates() {
        return Stream.of(
        		Arguments.of("12.10",LocalDate.of(2017,10,12)),
        		Arguments.of("12.01",LocalDate.of(2017,1,12)),
        		Arguments.of("01.03",LocalDate.of(2017,3,1)),
        		Arguments.of("12.12",LocalDate.of(2017,12,12)),
        		Arguments.of("03.11",LocalDate.of(2017,11,3)),
        		Arguments.of("",null)
        		);
    }
    
    @ParameterizedTest
    @MethodSource("setDifferentDates")
    public void checkAccept(String expText,LocalDate input) {
    	
    	dayMonthPicker.accept(input);
    	assertEquals(expText,dayMonthPicker.getText());
    }
    
    @Test
    public void checkCallback() {
    	String input = "12.10";
    	
    	@SuppressWarnings("unchecked")
		Consumer<String> callback=mock(Consumer.class);
    	
    	dayMonthPicker.setEndEditCallback(callback);
    	
    	clickOn(dayMonthPicker);
    	write(input);
    	
    	verify(callback,atLeast(1)).accept(input);
    	
    }
    
    static Stream<Arguments> threeYears() {
        return Stream.of(
        		Arguments.of(1999),
        		Arguments.of(2000),
        		Arguments.of(2004)
        		);
    }
    
    @ParameterizedTest
    @MethodSource("threeYears")
    public void checkRegularExpression(int year) {
    	//This is done in one test, because otherwise the 
    	//tests runtime is too long
    	
    	//We take a new field, because otherwise it is rendered and
    	//the fast updates are not supported by the rendering framework.
    	dayMonthPicker = new DayMonthField();
    	dayMonthPicker.setYear(year);
    	
    	for (int i = 0; i < 99; i++) {
    		for (int j = 0; j < 99; j++) {
    			LocalDate expResult;
    			try {
    				expResult=LocalDate.of(year, j,i);
    			} catch (DateTimeException e){
    				//Legitimate exception
    				expResult=null;
    			}
				dayMonthPicker.setText(String.format("%d.%d",i,j));
    			assertEquals(expResult,dayMonthPicker.getDate(),String.format("Failed at %d.%d",i,j));
    			dayMonthPicker.setText(String.format("%02d.%02d",i,j));
    			assertEquals(expResult,dayMonthPicker.getDate(),String.format("Failed at %02d.%02d",i,j));		
    		}			
		}
    }
    
}



