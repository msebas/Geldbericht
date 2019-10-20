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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    public static Logger logger;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("LoginPane"));
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(App.class.getResource(fxml + ".fxml"));
        fxmlLoader.setControllerFactory(new ControllerFactory());
        return fxmlLoader.load();
    }
    
    @Override
    public void init() throws Exception {
      Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
        	logger.error("Exception in thread \"" + t.getName() + "\"", e);
        }
      });
      super.init();
    }

    public static void main(String[] args) {
    	if( null != System.getenv("GELDBERICHT_LOGFILE")) {
    		System.setProperty("GELDBERICHT_LOGFILE", System.getenv("GELDBERICHT_LOGFILE"));
    	}
    	if(null == System.getProperty("GELDBERICHT_LOGFILE")) {
    		System.setProperty("GELDBERICHT_LOGFILE", "geldbericht.log");
    	}
    	logger=Logger.getLogger(App.class);
        launch();
    }

}
