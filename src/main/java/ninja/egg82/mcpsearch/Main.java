package ninja.egg82.mcpsearch;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ninja.egg82.mcpsearch.utils.gui.VersionGUIUtil;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static ExecutorService initialLoader = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Init-%d").build());

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

        initialLoader.submit(() -> {
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
        controller.stop();

        if (!initialLoader.isShutdown()) {
            initialLoader.shutdown();
            try {
                if (!initialLoader.awaitTermination(8L, TimeUnit.SECONDS)) {
                    initialLoader.shutdownNow();
                }
            } catch (InterruptedException ignored) {
                initialLoader.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
