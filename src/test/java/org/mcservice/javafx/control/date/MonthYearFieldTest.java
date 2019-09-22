package org.mcservice.javafx.control.date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeast;
import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

//@ExtendWith(MockitoExtension.class)

@Tag("GUI")
//@Tag("Active")
class MonthYearFieldTest extends ApplicationTest{
		
	MonthYearField monthYearPicker = null ;
	
	@Override 
	public void start(Stage stage) throws Exception {
		Scene scene = new Scene(new Group());
		
		stage.setTitle("Reflection Table View Test");
        stage.setWidth(500);
        stage.setHeight(300);
        
        monthYearPicker = new MonthYearField();
        		
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().add(monthYearPicker);
        
        ((Group) scene.getRoot()).getChildren().addAll(vbox);
		
        stage.setScene(scene);
        stage.show();
    }
    	
	@Test
    public void checkSetupTest() {
    	assertEquals("MM.YY (1950-2049)",monthYearPicker.getPromptText());
    	assertTrue(monthYearPicker.getStyleClass().contains("field-validation-error"));
    }
	
	static Stream<Arguments> insertCharsValues() {
        return Stream.of(
        		Arguments.of("AEIQYZckuyz;:!",false,null),
        		Arguments.of("12.12",true,LocalDate.of(2012,12,1)),
        		Arguments.of("1.12",true,LocalDate.of(2012,1,1)),
        		Arguments.of("12.1",true,LocalDate.of(2001,12,1)),
        		Arguments.of("3.13",true,LocalDate.of(2013,3,1)),
        		Arguments.of("05.49",true,LocalDate.of(2049,5,1)),
        		Arguments.of("4.48",true,LocalDate.of(2048,4,1)),
        		Arguments.of("03.07",true,LocalDate.of(2007,3,1)),
        		Arguments.of("4.50",true,LocalDate.of(1950,4,1)),
        		Arguments.of("03.99",true,LocalDate.of(1999,3,1)),
        		Arguments.of("2.6",true,LocalDate.of(2006,2,1))
        		);
    }
        
    @ParameterizedTest
    @MethodSource("insertCharsValues")
    public void insertCharsTest(String inv,boolean valid,LocalDate result) {
    	clickOn(monthYearPicker);
    	Pattern p=Pattern.compile("[0-9]{1,2}.[0-9]{1,2}");
    	
    	for (int j = 0; j < inv.length(); j++) {
    		write(String.valueOf(inv.charAt(j)),2);
    		String actSub=inv.substring(0,j+1);
			if(!p.matcher(actSub).matches()) {
				assertTrue(monthYearPicker.getStyleClass().contains("field-validation-error"),"Substring: ".concat(actSub));
				assertNull(monthYearPicker.getDate(),"Substring: ".concat(actSub));
			}
	    	assertEquals(actSub.replaceAll("[^\\d]",""),monthYearPicker.getText().replaceAll("[^\\d]",""),"Substring: ".concat(actSub));
    		
		}
    	
    	if(valid) {
    		assertFalse(monthYearPicker.getStyleClass().contains("field-validation-error"));
    		assertEquals(result,monthYearPicker.get());
    	}
    	
    }
    
    static Stream<Arguments> insertCharsNoIntermediateValues() {
        return Stream.of(
        		Arguments.of("ase12.10",LocalDate.of(2010,12,13)),
        		Arguments.of("12.77",LocalDate.of(1977,12,13)),
        		Arguments.of("113",LocalDate.of(2003,11,13)),
        		Arguments.of("1267",LocalDate.of(1967,12,13)),
        		Arguments.of("32.11",LocalDate.of(2021,3,13))
        		);
    }
    
    @ParameterizedTest
    @MethodSource("insertCharsNoIntermediateValues")
    public void insertCharsNoIntermediateTest(String inv,LocalDate result) {
    	monthYearPicker.setDay(13);
    	clickOn(monthYearPicker);
    	write(inv);
    	
		assertFalse(monthYearPicker.getStyleClass().contains("field-validation-error"));
		assertEquals(result,monthYearPicker.getDate());
    }
    
    @Test
    public void checkCallback() {
    	String input = "12.10";
    	
    	@SuppressWarnings("unchecked")
		Consumer<String> callback=mock(Consumer.class);
    	
    	monthYearPicker.setEndEditCallback(callback);
    	
    	clickOn(monthYearPicker);
    	write(input);
    	
    	verify(callback,atLeast(1)).accept(input);
    	
    }
    
    @Tag("Active")
    @Test
    public void setDateNull() {
    	Platform.runLater( () -> monthYearPicker.accept(null));
    	sleep(100);
    	assertNotNull(monthYearPicker.getBaseDate());
    }
    
    @Tag("Active")
    @Test
    public void setDate() {
    	Platform.runLater( () -> monthYearPicker.accept(LocalDate.of(1920, 4, 1)));
    	sleep(100);
    	assertEquals("04.20",monthYearPicker.getText());
    	assertEquals(LocalDate.of(1920, 4, 1),monthYearPicker.getBaseDate());
    }
    
}



