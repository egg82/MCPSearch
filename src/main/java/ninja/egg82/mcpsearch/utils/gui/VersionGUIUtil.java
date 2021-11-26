package ninja.egg82.mcpsearch.utils.gui;

import java.net.HttpURLConnection;
import java.net.URL;

import flexjson.JSONDeserializer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import ninja.egg82.mcpsearch.Controller;
import ninja.egg82.mcpsearch.model.MCPVersionModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ninja.egg82.mcpsearch.utils.AlertUtil;
import ninja.egg82.mcpsearch.utils.TimeUtil;
import ninja.egg82.mcpsearch.web.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionGUIUtil {
    private static Logger logger = LoggerFactory.getLogger(VersionGUIUtil.class);

    private VersionGUIUtil() {}

    public static void getVersions(Controller controller) {
        Map<String, MCPVersionModel> model;
        try {
            HttpURLConnection conn = WebRequest.builder(new URL("https://raw.githubusercontent.com/ModCoderPack/MCPMappingsArchive/master/versions.json"))
                    .timeout(new TimeUtil.Time(2500L, TimeUnit.MILLISECONDS))
                    .userAgent("egg82/MCPSearch")
                    .header("Accept", "application/json")
                    .build()
                    .getConnection();

            JSONDeserializer<Map<String, MCPVersionModel>> modelDeserializer = new JSONDeserializer<>();
            modelDeserializer.use("values", MCPVersionModel.class);
            model = modelDeserializer.deserialize(WebRequest.getString(conn));
        } catch (IOException ex) {
            logger.error("Could not get version.", ex);
            AlertUtil.show(Alert.AlertType.ERROR, "Version Fetch Error", ex.getMessage());
            return;
        }

        controller.versionsModel = model;

        List<String> versionList = new ArrayList<>();
        for (Map.Entry<String, MCPVersionModel> kvp : model.entrySet()) {
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
