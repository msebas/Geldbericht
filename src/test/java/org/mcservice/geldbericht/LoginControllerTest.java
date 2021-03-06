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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mcservice.AfterFXInitBeforeEach;
import org.mcservice.MockedApplicationTest;
import org.mcservice.geldbericht.App;
import org.mcservice.geldbericht.data.User;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mockito.quality.Strictness;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

@Tag("Active")
@Tag("GUI")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
//@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class LoginControllerTest extends MockedApplicationTest{
	
	public class DbFactory extends ControllerFactory{

		boolean wait=false;
		int waitUiUpdateTime=10;
		int waitTime=25;
		
		public Object call(Class<?> clazz) {
			if(clazz==LoginController.class) {
				controller=new LoginController(factory);
				try {
				Field field=LoginController.class.getDeclaredField("dbWaitTime");
				field.setAccessible(true);
				field.set(controller, waitUiUpdateTime);
				field.setAccessible(false);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
				return controller;
			}
			return Mockito.mock(clazz);
		}
		
		public void createDB() {
			while(wait) {
				try {
					Thread.sleep((long) waitTime);
				} catch (InterruptedException e) {
					if(!wait)
						return;
				}
			}
		}
		
		public DbAbstractionLayer getDB() {
			return db;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	private @interface CreateUsers {
		   int value() default 3;
		}
	@Retention(RetentionPolicy.RUNTIME)
	private @interface BehaviourMock {
		boolean waitMockDBCreation() default false;
		boolean returnDatabase() default true;
	}
	
	protected TextField usernameInputField;
	protected PasswordField passwordInputField;
	protected ProgressBar progressBar;
	protected Label progressbarLabel;
	protected Button loginButton;
	protected DbAbstractionLayer db=null;
	
	protected DbFactory factory;
	protected LoginController controller;
	protected ByteArrayOutputStream stdOutMock;
	
		
	@BeforeEach 
	public void initMock(TestInfo testInfo) throws Exception {
		stdOutMock = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stdOutMock));
		
		factory=Mockito.spy(new DbFactory());
		boolean returnDb=true;
		
		BehaviourMock annotations[]=testInfo.getTestMethod().get().getAnnotationsByType(BehaviourMock.class);
		if(annotations.length>0) {
			factory.wait=annotations[0].waitMockDBCreation();
			returnDb=annotations[0].returnDatabase();
		}
		
		if(returnDb) {
			URL data=this.getClass().getClassLoader().getResource("connection.cfg.xml");
	    	Map<String,String> newenv=new TreeMap<String,String>(System.getenv());
	    	newenv.put("GELDBERICHT_CONFIGFILE",data.getPath());
	    	setEnv(newenv);
			db=new DbAbstractionLayer();
			db=Mockito.spy(db);
		}
		
		CreateUsers[] createUsers = testInfo.getTestMethod().get().getAnnotationsByType(CreateUsers.class);
		if(returnDb && createUsers.length>0) {
			//To make password hashing fast (and useless)
			User.passwordCpuThreads=1;
			User.passwordCpuUsageTime=10;
			User.passwordMemory=16;
			User.passwordForcedIterations=1;
			for(int i=0;i<createUsers[0].value();++i) {
				User user=new User();
				user.setUserName("UserName");
				user.setPassword(new char[] {'c', 'o', 'r', 'r', 'e', 'c', 't', ' ',Integer.toString(i).charAt(0)});
				db.persistUser(user);
			}
			Mockito.reset(db);
		}
	}
	
	@AfterEach
	public void checkNoPasswordInLogs() {
		//Some people might like to write passwords to logs for debug purpose
		//This should verify that all of those writes are removed before all test are valid
		for(int i=0;i<50;++i) {
			assertFalse(stdOutMock.toString().contains(String.format("correct %d",i)));
		}
	}
    
    @AfterFXInitBeforeEach
    public void setArgs() {
    	Node tmpNode=lookup("#usernameInputField").query();
		if (tmpNode instanceof TextField) {
			usernameInputField=(TextField) tmpNode;
		}
		tmpNode=lookup("#passwordInputField").query();
		if (tmpNode instanceof PasswordField) {
			passwordInputField=(PasswordField) tmpNode;
		}
		tmpNode=lookup("#progressBar").query();
		if (tmpNode instanceof ProgressBar) {
			progressBar=(ProgressBar) tmpNode;
		}
		tmpNode=lookup("#progressbarLabel").query();
		if (tmpNode instanceof Label) {
			progressbarLabel=(Label) tmpNode;
		}
		tmpNode=lookup("#loginButton").query();
		if (tmpNode instanceof Button) {
			loginButton=(Button) tmpNode;
		}
    }
    
    @Override 
	public void start(Stage stage) throws Exception {
    	FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("LoginPane.fxml"));
		fxmlLoader.setControllerFactory(factory);
		Scene scene = new Scene(fxmlLoader.load());
		stage.setScene(scene);
        stage.show();
    }
    
    @Test
    @BehaviourMock(returnDatabase = false,waitMockDBCreation = true)
    public void checkCreateDBerror() {
    	double oldProgress=0.0;
    	for(int i=0;i<50 && progressBar.getProgress()<0.7;++i) {
    		assertEquals("Datenbankverbindung aufbauen...",progressbarLabel.getText());
    		assertTrue(oldProgress<progressBar.getProgress());
    		oldProgress=progressBar.getProgress();
    		sleep(factory.waitUiUpdateTime*2);
    	}
    	factory.wait=false;
    	sleep(200);
    	assertEquals("Verbindungsaufbau fehlgeschlagen.",progressbarLabel.getText());
    }
    
    @Test
    @BehaviourMock(returnDatabase = true,waitMockDBCreation = true)
    public void checkCreateDBsuccess() {
		assertEquals("Datenbankverbindung aufbauen...",progressbarLabel.getText());
		factory.wait=false;
    	sleep(200);
    	assertTrue(0.749<progressBar.getProgress());
    	assertEquals("Verbunden.",progressbarLabel.getText());
    	sleep(120);
    }
    
    @Test
    @BehaviourMock(returnDatabase = false,waitMockDBCreation = false)
    public void checkLoginNoDatabase() {
		Platform.runLater(()-> {
			usernameInputField.setText("user");
			passwordInputField.setText("wrong");
		});
		sleep(100);
		clickOn(loginButton);
		assertEquals(1,javafx.stage.Window.getWindows().size());
		Mockito.verify(factory,Mockito.times(0)).call(PrimaryController.class);
		assertEquals("Verbindungsaufbau fehlgeschlagen.",progressbarLabel.getText());
    }
    
    @Test
    @BehaviourMock(returnDatabase = false,waitMockDBCreation = true)
    public void checkLoginNoDatabaseWaiting() {
		Platform.runLater(()-> {
			usernameInputField.setText("user");
			passwordInputField.requestFocus();
			passwordInputField.selectRange(0, 0);
			passwordInputField.setText("wrong");
		});
		factory.waitTime=350;
		factory.wait=false;
		sleep(100);
		type(KeyCode.ENTER);
		sleep(100);
		assertEquals(1,javafx.stage.Window.getWindows().size());
		Mockito.verify(factory,Mockito.times(0)).call(PrimaryController.class);
		assertEquals("Verbindungsaufbau fehlgeschlagen.",progressbarLabel.getText());
    }
    
    @Test
    @CreateUsers(1)
    @BehaviourMock(returnDatabase = true,waitMockDBCreation = true)
    public void checkLoginDbDelayedFail() {
		Platform.runLater(()-> {
			usernameInputField.setText("UserName");
			passwordInputField.requestFocus();
			passwordInputField.selectRange(0, 0);
			passwordInputField.setText("wrong");
		});
		factory.waitTime=350;
		factory.wait=false;
		sleep(100);
		type(KeyCode.ENTER);
		sleep(100);
		assertEquals(1,javafx.stage.Window.getWindows().size());
		Mockito.verify(factory,Mockito.times(0)).call(PrimaryController.class);
		assertEquals("Passwort oder Benutzname falsch.",progressbarLabel.getText());
    }
    
    @Test
    @CreateUsers(1)
    @BehaviourMock(returnDatabase = true,waitMockDBCreation = true)
    public void checkLoginDbDelayedOK1() {
		Platform.runLater(()-> {
			usernameInputField.setText("UserName");
			passwordInputField.requestFocus();
			passwordInputField.selectRange(0, 0);
			passwordInputField.setText("correct 0");
		});
		factory.waitTime=350;
		factory.wait=false;
		sleep(100);
		type(KeyCode.ENTER);
		//To fail the test fast if it fails
		assertFalse(progressbarLabel.getText().equals("Passwort oder Benutzname falsch."));
		sleep(100);
		Mockito.verify(factory).call(PrimaryController.class);
		assertFalse(loginButton.getScene().getWindow().isShowing());
    }
    
    @Test
    @CreateUsers(2)
    @BehaviourMock(returnDatabase = true,waitMockDBCreation = true)
    public void checkLoginDbDelayedOK2() {
		Platform.runLater(()-> {
			usernameInputField.setText("UserName");
			passwordInputField.requestFocus();
			passwordInputField.selectRange(0, 0);
			passwordInputField.setText("correct 1");
		});
		factory.waitTime=350;
		factory.wait=false;
		sleep(100);
		type(KeyCode.ENTER);
		//To fail the test fast if it fails
		assertFalse(progressbarLabel.getText().equals("Passwort oder Benutzname falsch."));
		sleep(100);
		Mockito.verify(factory).call(PrimaryController.class);
		assertFalse(loginButton.getScene().getWindow().isShowing());
    }

    @Test
    @CreateUsers(1)
    @BehaviourMock(returnDatabase = true,waitMockDBCreation = false)
    public void checkLoginOK1() {
    	//One test has to manually insert anything to check if the input behavior works correctly
		clickOn(usernameInputField).write("UserName");
		type(KeyCode.TAB).write("correct 0");		
		type(KeyCode.ENTER);
		//To fail the test fast if it fails
		assertFalse(progressbarLabel.getText().equals("Passwort oder Benutzname falsch."));
		sleep(100);
		assertFalse(loginButton.getScene().getWindow().isShowing());
    }
    
    @Test
    @CreateUsers(2)
    @BehaviourMock(returnDatabase = true,waitMockDBCreation = false)
    public void checkLoginOK2() {
		Platform.runLater(()-> {
			usernameInputField.setText("UserName");
			passwordInputField.requestFocus();
			passwordInputField.selectRange(0, 0);
			passwordInputField.setText("correct 1");
		});
		sleep(100);
		type(KeyCode.ENTER);
		//To fail the test fast if it fails
		assertFalse(progressbarLabel.getText().equals("Passwort oder Benutzname falsch."));
		sleep(100);
		Mockito.verify(factory).call(PrimaryController.class);
		assertFalse(loginButton.getScene().getWindow().isShowing());
    }
    
    
}
