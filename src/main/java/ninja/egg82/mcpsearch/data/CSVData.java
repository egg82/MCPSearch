package ninja.egg82.mcpsearch.data;

import javafx.scene.control.Alert;
import ninja.egg82.analytics.exceptions.IExceptionHandler;
import ninja.egg82.mcpsearch.utils.AlertUtil;
import ninja.egg82.patterns.ServiceLocator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CSVData {
    public Map<String, String[]> methods = new HashMap<>(); // func_100011_g, [getIsPotionDurationMax, "Get the value of the isPotionDurationMax field."]
    public Map<String, String> params = new HashMap<>(); // p_100012_1_, maxDuration
    public Map<String, String[]> fields = new HashMap<>(); // field_100013_f, [isPotionDurationMax, "True if potion effect duration is at maximum, false otherwise."]

    public CSVData(JSONObject json) {
        JSONObject methods = (JSONObject) json.get("methods");
        for (Object i : methods.entrySet()) {
            Map.Entry<String, JSONArray> kvp = (Map.Entry<String, JSONArray>) i;
            this.methods.put(kvp.getKey(), (String[]) kvp.getValue().toArray(new String[0]));
        }

        JSONObject params = (JSONObject) json.get("params");
        for (Object i : params.entrySet()) {
            Map.Entry<String, String> kvp = (Map.Entry<String, String>) i;
            this.params.put(kvp.getKey(), kvp.getValue());
        }

        JSONObject fields = (JSONObject) json.get("fields");
        for (Object i : fields.entrySet()) {
            Map.Entry<String, JSONArray> kvp = (Map.Entry<String, JSONArray>) i;
            this.fields.put(kvp.getKey(), (String[]) kvp.getValue().toArray(new String[0]));
        }
    }
    public CSVData(File fields, File methods, File params) {
        parseFields(fields);
        parseMethods(methods);
        parseParams(params);
    }

    public JSONObject serialize() {
        JSONObject out = new JSONObject();

        JSONObject methods = new JSONObject();
        for (Map.Entry<String, String[]> kvp : this.methods.entrySet()) {
            JSONArray array = new JSONArray();
            array.addAll(Arrays.asList(kvp.getValue()));
            methods.put(kvp.getKey(), array);
        }

        JSONObject params = new JSONObject();
        for (Map.Entry<String, String> kvp : this.params.entrySet()) {
            params.put(kvp.getKey(), kvp.getValue());
        }

        JSONObject fields = new JSONObject();
        for (Map.Entry<String, String[]> kvp : this.fields.entrySet()) {
            JSONArray array = new JSONArray();
            array.addAll(Arrays.asList(kvp.getValue()));
            fields.put(kvp.getKey(), array);
        }

        out.put("methods", methods);
        out.put("params", params);
        out.put("fields", fields);

        return out;
    }

    private void parseFields(File file) {
        try (FileReader reader = new FileReader(file); BufferedReader in = new BufferedReader(reader)) {
            String line;
            String first = null;
            while ((line = in.readLine()) != null) {
                if (first == null) {
                    first = line;
                    continue;
                }

                String[] split = line.split(",");
                fields.put(split[0], new String[] { split[1], split.length >= 4 ? split[3] : null });
            }
        } catch (IOException ex) {
            ServiceLocator.getService(IExceptionHandler.class).sendException(ex);
            AlertUtil.show(Alert.AlertType.ERROR, "CSV Parse Error", ex.getMessage());
            return;
        }
    }
    private void parseMethods(File file) {
        try (FileReader reader = new FileReader(file); BufferedReader in = new BufferedReader(reader)) {
            String line;
            String first = null;
            while ((line = in.readLine()) != null) {
                if (first == null) {
                    first = line;
                    continue;
                }

                String[] split = line.split(",");
                methods.put(split[0], new String[] { split[1], split.length >= 4 ? split[3] : null });
            }
        } catch (IOException ex) {
            ServiceLocator.getService(IExceptionHandler.class).sendException(ex);
            AlertUtil.show(Alert.AlertType.ERROR, "CSV Parse Error", ex.getMessage());
            return;
        }
    }
    private void parseParams(File file) {
        try (FileReader reader = new FileReader(file); BufferedReader in = new BufferedReader(reader)) {
            String line;
            String first = null;
            while ((line = in.readLine()) != null) {
                if (first == null) {
                    first = line;
                    continue;
                }

                String[] split = line.split(",");
                params.put(split[0], split[1]);
            }
        } catch (IOException ex) {
            ServiceLocator.getService(IExceptionHandler.class).sendException(ex);
            AlertUtil.show(Alert.AlertType.ERROR, "CSV Parse Error", ex.getMessage());
            return;
        }
    }
}
