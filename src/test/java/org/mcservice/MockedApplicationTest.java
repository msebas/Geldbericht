package org.mcservice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
}
