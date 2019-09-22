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

import java.util.List;

import org.mcservice.geldbericht.data.User;
import org.mcservice.geldbericht.database.DbAbstractionLayer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class LoginController {

	private int dbWaitTime = 100;
	
	protected ControllerFactory factory;
	protected Thread dbCreationThread;
	protected Thread uiUpdateThread;
	protected DbAbstractionLayer db = null;
	@FXML
	protected TextField usernameInputField;
	@FXML
	protected PasswordField passwordInputField;
	@FXML
	protected ProgressBar progressBar;
	@FXML
	protected Label progressbarLabel;
	@FXML
	protected Button loginButton;

	public LoginController(ControllerFactory factory) {
		this.factory=factory;
		dbCreationThread = new Thread(() -> {
			factory.createDB();
			this.db = factory.getDB();
		});
		dbCreationThread.setDaemon(true);
		dbCreationThread.start();
	}
	
	@FXML
	protected void initialize() {
		progressbarLabel.setText(String.format("Datenbankverbindung aufbauen..."));
		
		final Task<Void> task = new Task<Void>() {
			
            @Override
            protected Void call() throws Exception {
            	int i = 1000, j = 1;
                while(true) {
                    updateProgress(3*j,4*i);
                    if(dbCreationThread.isAlive()) {
                    	Thread.sleep(dbWaitTime);
                    	if(j>i*0.6) {
                    		j+=(i-j)/30;
                    	} else {
                    		j+=i/100;
                    	}
                		continue;
                    }
                    try {
                    	dbCreationThread.join();
                    } catch (InterruptedException e) {
                    	continue;
                    }
                    updateProgress(3,4);
                    break;
                }
                Thread.sleep(100);
                Platform.runLater( () -> {
                	if(!progressbarLabel.getText().equals("Datenbankverbindung aufbauen...")) {
                		//We are already behind trying this stuff...
                		return;
                	}
	                if(db!=null) {
	                	progressbarLabel.setText("Verbunden.");
	                } else {
	                	progressbarLabel.setText("Verbindungsaufbau fehlgeschlagen.");
	                }
                });
    			dbCreationThread=null;
                return null;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        
        uiUpdateThread = new Thread(task, "task-thread");
        uiUpdateThread.setDaemon(true);
        uiUpdateThread.start();
        
        passwordInputField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
        	try {
				login();
				event.consume();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
        });
	}

	@FXML
	protected void login() throws Exception {
		if(!waitForDbConnection())
			return;
		

		String userName = usernameInputField.getText();
		List<User> users=db.getUserByName(userName);
		

		try {
			// Verify password
			boolean loginSuccessfull=false;
			for(User user:users) {
				progressbarLabel.setText(String.format("Prüfe Benutzer %d",user.getUid()));
				char[] password;
				if(passwordInputField.getText()!=null) {
					password = passwordInputField.getText().toCharArray();
				} else {
					password = new char[] {};
				}
				if (user.verifyPassword(password)) {
					loginSuccessfull=true;
					break;
				}
			}
			if(loginSuccessfull) {
				progressBar.setProgress(1.0);
				//Start Main application
				startMainApp();
				//Close window
				((Stage) loginButton.getScene().getWindow()).close();				
			} else {
				progressbarLabel.setText(String.format("Passwort oder Benutzname falsch."));
			}			
		} finally {
			// Wipe passwords
			passwordInputField.setText(null);
			// This might clear the password from memory, but this could not be forced.
			System.gc();
			// FIXME There might be still a representation of the password string in the
			// string pool. Implement a secure password input field using only char[] and 
			// make a pull request
		}

	}

	protected boolean waitForDbConnection() throws InterruptedException {
		if(null!=uiUpdateThread) {
			//Wait for db connection creation and ui update
			uiUpdateThread.join();
			uiUpdateThread=null;
			progressBar.progressProperty().unbind();
			Thread.sleep(100);
		}
		return null!=db;
	}
	
	@FXML
	private void startMainApp() throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));
		fxmlLoader.setControllerFactory(this.factory);
		Scene scene = new Scene(fxmlLoader.load());
        
		// New window (Stage)
		Stage newWindow = new Stage();
		newWindow.setTitle("Geldbericht");
		newWindow.setScene(scene);
		newWindow.show();
	}
}
