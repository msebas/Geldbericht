package org.mcservice.javafx.control.date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
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

//@ExtendWith(MockitoExtension.class)

@Tag("GUI")
@Tag("Active")
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
    
	@Test
	@Disabled
    public void manualTest() {
    	//Wait 1 h, then continue

    	sleep(3600000);
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
    
    @ParameterizedTest
    @MethodSource("insertCharsNoIntermediateValues")
    public void insertCharsNoIntermediateTest(String inv,LocalDate result) {
    	dayMonthPicker.setYear(2017);
    	clickOn(dayMonthPicker);
    	write(inv);
    	
		assertFalse(dayMonthPicker.getStyleClass().contains("field-validation-error"));
		assertEquals(result,dayMonthPicker.getDate());
    }
    
    @Test
    public void checkCallback() {
    	String input = "12.10";
    	
    	@SuppressWarnings("unchecked")
		Consumer<String> callback=mock(Consumer.class);
    	
    	dayMonthPicker.setEndEditCallback(callback);
    	
    	clickOn(dayMonthPicker);
    	write(input);
    	
    	verify(callback,times(2)).accept(input);
    	
    }
    
}



