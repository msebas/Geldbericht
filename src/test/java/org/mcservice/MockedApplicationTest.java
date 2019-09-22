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
package org.mcservice;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationAdapter;
import org.testfx.framework.junit5.ApplicationFixture;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Application.Parameters;
import javafx.application.Preloader.PreloaderNotification;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

@ExtendWith(MockedApplicationExtension.class)
public abstract class MockedApplicationTest extends FxRobot implements ApplicationFixture {

	public static void launch(Class<? extends Application> appClass, String... appArgs) throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(appClass, appArgs);
    }

    public final void setUpTestStage() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(() -> new ApplicationAdapter(this));
    }

    public final void tearDownTestStage() throws Exception {
        // release all keys
        release(new KeyCode[0]);
        // release all mouse buttons
        release(new MouseButton[0]);
        FxToolkit.cleanupStages();
        FxToolkit.cleanupApplication(new ApplicationAdapter(this));
    }

    @Override
    public void init() throws Exception {}

    @Override
    public void start(Stage stage) throws Exception {}

    @Override
    public void stop() throws Exception {}

    @Deprecated
    public final HostServices getHostServices() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final Parameters getParameters() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final void notifyPreloader(PreloaderNotification notification) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * This is a test case only hack to overwrite the system environment in unit tests.
     * 
     * !!!DO NEVER USE THIS IN PRODUCTION CODE!!!
     * 
     * Does create illegal reflective access operation errors.
     * This method changes the JVM internal map to the environment one needs for a unit test.
     * Credit goes to Edward Campbell and pushy (stackoverflow). 
     * See {@link https://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java}
     * 
     * @param newenv The environment map to overwrite the system side one
     * @throws Exception
     */    
    @SuppressWarnings("unchecked")
	public static void setEnv(Map<String, String> newenv) throws Exception {
		try {
			Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
			for(String envFieldName: new String []{"theEnvironment","theCaseInsensitiveEnvironment"}) {
				Field envField = processEnvironmentClass.getDeclaredField(envFieldName);
				envField.setAccessible(true);
				Map<String, String> env = (Map<String, String>) envField.get(null);
				env.clear();
				env.putAll(newenv);
				envField.setAccessible(false);
			}
		} catch (NoSuchFieldException e) {
			Class<?>[] classes = Collections.class.getDeclaredClasses();
			Map<String, String> env = System.getenv();
			for (Class<?> cl : classes) {
				if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
					Field field = cl.getDeclaredField("m");
					field.setAccessible(true);
					Object obj = field.get(env);
					Map<String, String> map = (Map<String, String>) obj;
					map.clear();
					map.putAll(newenv);
					field.setAccessible(false);
				}
			}
		}
	}
}
