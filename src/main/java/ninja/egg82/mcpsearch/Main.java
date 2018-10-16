package ninja.egg82.mcpsearch;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ninja.egg82.analytics.exceptions.GameAnalyticsExceptionHandler;
import ninja.egg82.analytics.exceptions.IExceptionHandler;
import ninja.egg82.mcpsearch.utils.gui.VersionGUIUtil;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.utils.ThreadUtil;

import java.io.IOException;
import java.util.UUID;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();

        controller = loader.getController();
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        controller.setup();
        primaryStage.show();

        ServiceLocator.provideService(new GameAnalyticsExceptionHandler("801d7ccccb2bed9ebd0ca7621e5c355d", "e37be12a9125a9bbf915c119f7d6fae0cd5506ee", "1.0.0", UUID.randomUUID().toString(), "MCPSearch"));

        ThreadUtil.submit(() -> {
            VersionGUIUtil.getVersions(controller);

            Platform.runLater(() -> {
                controller.versionCombo.setDisable(false);
                controller.reloadButton.setDisable(false);
                controller.versionSpinner.setVisible(false);
                controller.versionLabel.setText("");
            });
        });
    }

    @Override
    public void stop() {
        ServiceLocator.getService(IExceptionHandler.class).close();
        ThreadUtil.shutdown(5000L);
    }
}
