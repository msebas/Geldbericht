package org.mcservice.geldbericht;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mcservice.AfterFXInitBeforeEach;
import org.mcservice.MockedApplicationTest;
import org.mcservice.geldbericht.App;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;


import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Tag("Active")
@Tag("GUI")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
//@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class LoginControllerTest extends MockedApplicationTest{

	@Retention(RetentionPolicy.RUNTIME)
	private @interface CreateCompanies {
		   int value() default 3;
		}
	@Retention(RetentionPolicy.RUNTIME)
	private @interface CreateCompany {
		String name() default "Company & Co GmbH";
		String number() default "12345";
		String appointment() default "1234567890";
		int accountNumber() default 2;
		boolean insertError() default false;
		boolean skip() default false;
	}
	
	protected TextField usernameInputField;
	protected PasswordField passwordInputField;
	protected ProgressBar progressBar;
	protected Label progressbarLabel;
	protected Button loginButton;
	
		
	@BeforeEach 
	public void initMock(TestInfo testInfo) {
		//Create the database
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
    	URL data=this.getClass().getClassLoader().getResource("connection.cfg.xml");
    	Map<String,String> newenv=new TreeMap<String,String>(System.getenv());
    	newenv.put("GELDBERICHT_CONFIGFILE",data.getPath());
    	setEnv(newenv);
    	
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("LoginPane.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		stage.setScene(scene);
        stage.show();
    }
    
    @Test
    public void manual() {
    	sleep(36000);
    }
    
}
