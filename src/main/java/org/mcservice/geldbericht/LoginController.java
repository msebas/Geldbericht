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

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

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

	public LoginController() {
		dbCreationThread = new Thread(() -> {
			this.db = new DbAbstractionLayer();
		});
		dbCreationThread.run();
	}
	
	@FXML
	protected void initialize() {
		progressbarLabel.setText(String.format("Datenbankverbindung aufbauen..."));
		
		final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
            	int i = 100, j = 1;
                while(true) {
                    updateProgress(3*j,4*i);
                    try {
                    	dbCreationThread.join(100);
                    } catch (InterruptedException e) {
                    	j=(i-j)*9/10;
                    	continue;
                    }
                    updateProgress(3,4);
                    break;
                }
                progressbarLabel.setText(String.format("Verbunden."));
    			dbCreationThread=null;
                return null;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        
        uiUpdateThread = new Thread(task, "task-thread");
        uiUpdateThread.setDaemon(true);
        uiUpdateThread.start();
	}

	@FXML
	protected void login() throws Exception {
		waitForDbConnection();		

		String userName = usernameInputField.getText();
		List<User> users=db.getUserByName(userName);

		char[] password = passwordInputField.getText().toCharArray();

		try {
			// Verify password
			boolean loginSuccessfull=false;
			for(User user:users) {
				progressbarLabel.setText(String.format("Prüfe Benutzer %d",user.getUid()));
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

	protected void waitForDbConnection() throws InterruptedException {
		if(null!=dbCreationThread) {
			//Wait for db connection creation
			dbCreationThread.join();
			progressbarLabel.setText(String.format("Verbunden."));
			progressBar.setProgress(0.5);
			dbCreationThread=null;
		}
	}
	
	protected void waitForUiUpdateThread() throws InterruptedException {
		if(null!=uiUpdateThread) {
			uiUpdateThread.join();
			uiUpdateThread=null;
		}
	}
	
	@FXML
	private void startMainApp() throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));
		fxmlLoader.setControllerFactory(new ControllerFactory(db));
		Scene scene = new Scene(fxmlLoader.load());
        
		// New window (Stage)
		Stage newWindow = new Stage();
		newWindow.setTitle("Geldbericht");
		newWindow.setScene(scene);
		newWindow.show();
	}
}
