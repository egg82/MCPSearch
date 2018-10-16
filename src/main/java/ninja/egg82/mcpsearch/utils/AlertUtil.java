package ninja.egg82.mcpsearch.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AlertUtil {
    private AlertUtil() {}

    public static void show(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert error = new Alert(type);
            error.setTitle(title);
            error.setContentText(content);
            error.show();
        });
    }
}
