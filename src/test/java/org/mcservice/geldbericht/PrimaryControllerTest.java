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
package org.mcservice.geldbericht;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mcservice.AfterFXInitBeforeEach;
import org.mcservice.MockedApplicationTest;
import org.mcservice.geldbericht.App;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Callback;

@Tag("Active")
@Tag("GUI")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
//@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class PrimaryControllerTest extends MockedApplicationTest{

	@Mock
	DbAbstractionLayer db;
	Node menuManager;
	
	Scene scene;
	PrimaryController controller=null;
	ArrayList<Object> controllers=new ArrayList<Object>();
	
	
	class MockControllerFactory implements Callback<Class<?>, Object>{
		public Object call(Class<?> clazz) {
			if (PrimaryController.class.equals(clazz)) {
				controller=new PrimaryController(db);
				return controller;
			}
			Object result=Mockito.mock(clazz);
			controllers.add(result);
			return result;
		}		
	}
		
	@Override 
	public void start(Stage stage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));
		fxmlLoader.setControllerFactory(new MockControllerFactory());
		
		scene = new Scene(fxmlLoader.load());
		controller=fxmlLoader.getController();
		stage.setScene(scene);
        stage.show();
    }
    
	@AfterFXInitBeforeEach
    public void setArgs() {
		menuManager = (Node) (lookup("#menuManager").query());
	}
	
	@Test
	@Disabled
    public void manual() {
    	sleep(360);
    }
	
	@Test
    public void setupCheck() {
    	List<Class<?>> data=new ArrayList<>();
    	for (Object obj : controllers) {
			data.add(obj.getClass());
		}
    	assertTrue(controllers.get(0) instanceof TransactionInputPaneController);
    	assertTrue(controllers.get(1) instanceof PdfGeneratorPaneController);
    }
	
	@Test
    public void startAndCloseAccountManager() {
    	clickOn(menuManager);
    	clickOn((Node) lookup("#menuAccountManager").query());
    	
    	assertEquals(2,javafx.stage.Window.getWindows().size());
    	assertEquals("Kontenmanager",((Stage) javafx.stage.Window.getWindows().get(1)).getTitle());
    	
    	Button persistButton=(Button) lookup("#persistButton").query();
    	persistButton.setDisable(false);
    	clickOn(persistButton);
    	verify((TransactionInputPaneController) controllers.get(0)).updateData();
    	verify((PdfGeneratorPaneController) controllers.get(1)).updateData();
    }
	
	@Test
    public void startCompanyManager() {
    	clickOn(menuManager);
    	clickOn((Node) lookup("#menuCompanyManager").query());
    	
    	assertEquals(2,javafx.stage.Window.getWindows().size());
    	assertEquals("Betriebmanager",((Stage) javafx.stage.Window.getWindows().get(1)).getTitle());
    	
    	Button persistButton=(Button) lookup("#persistButton").query();
    	persistButton.setDisable(false);
    	clickOn(persistButton);
    	verify((TransactionInputPaneController) controllers.get(0)).updateData();
    	verify((PdfGeneratorPaneController) controllers.get(1)).updateData();
    }
	
	@Test
    public void startVatTypeManager() {
    	clickOn(menuManager);
    	clickOn((Node) lookup("#menuVatTypeManager").query());
    	
    	assertEquals(2,javafx.stage.Window.getWindows().size());
    	assertEquals("Steuermanager",((Stage) javafx.stage.Window.getWindows().get(1)).getTitle());
    	
    	Button persistButton=(Button) lookup("#persistButton").query();
    	persistButton.setDisable(false);
    	clickOn(persistButton);
    	verify((TransactionInputPaneController) controllers.get(0)).updateData();
    	verify((PdfGeneratorPaneController) controllers.get(1)).updateData();
    }
}
