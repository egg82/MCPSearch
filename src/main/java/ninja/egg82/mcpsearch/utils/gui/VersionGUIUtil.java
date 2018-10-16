package ninja.egg82.mcpsearch.utils.gui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import ninja.egg82.analytics.exceptions.IExceptionHandler;
import ninja.egg82.mcpsearch.Controller;
import ninja.egg82.mcpsearch.utils.AlertUtil;
import ninja.egg82.mcpsearch.utils.WebUtil;
import ninja.egg82.patterns.ServiceLocator;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VersionGUIUtil {
    private VersionGUIUtil() {}

    public static void getVersions(Controller controller) {
        JSONObject versions;
        try {
            versions = WebUtil.getJsonObject("http://export.mcpbot.bspk.rs/versions.json", "egg82/MCPSearch");
        } catch (ParseException ex) {
            ServiceLocator.getService(IExceptionHandler.class).sendException(ex);
            AlertUtil.show(Alert.AlertType.ERROR, "JSON Parse Error", ex.getMessage());
            return;
        } catch (IOException ex) {
            ServiceLocator.getService(IExceptionHandler.class).sendException(ex);
            AlertUtil.show(Alert.AlertType.ERROR, "Version Fetch Error", ex.getMessage());
            return;
        }

        controller.versions = versions;

        List<String> versionList = new ArrayList<>();
        for (Object i : versions.entrySet()) {
            Map.Entry<String, JSONObject> kvp = (Map.Entry<String, JSONObject>) i;
            versionList.add(kvp.getKey());
        }
        versionList.sort((v1, v2) -> {
            int[] v1I = parseVersion(v1);
            int[] v2I = parseVersion(v2);

            for (int i = 0; i < Math.min(v1I.length, v2I.length); i++) {
                if (v1I[i] < v2I[i]) {
                    return 1;
                } else if (v1I[i] > v2I[i]) {
                    return -1;
                }
            }

            if (v1I.length < v2I.length) {
                return 1;
            } else if (v2I.length < v1I.length) {
                return -1;
            }

            return 0;
        });

        Platform.runLater(() -> controller.versionCombo.getItems().addAll(versionList));
    }

    public static int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int[] retVal = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            retVal[i] = Integer.parseInt(parts[i]);
        }
        return retVal;
    }
}
