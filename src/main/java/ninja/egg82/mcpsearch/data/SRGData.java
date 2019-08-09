package ninja.egg82.mcpsearch.data;

import javafx.scene.control.Alert;
import ninja.egg82.mcpsearch.utils.AlertUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SRGData {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public Map<String, String> classes = new HashMap<>(); // a, net/minecraft/util/text/TextFormatting
    public Map<String, String> methods = new HashMap<>(); // a/a, func_175744_a
    public Map<String, String[]> params = new HashMap<>(); // func_184104_a, [net/minecraft/command/ICommandSender, java/lang/String, net/minecraft/util/math/BlockPos, boolean]
    public Map<String, String> fields = new HashMap<>(); // a/A, field_96303_A

    private Pattern duplicate = Pattern.compile("^.*\\$\\d+.*$");
    private Pattern paramsSplit = Pattern.compile("^\\((.*?);?\\)(.*?);?$");

    public SRGData(JSONObject json) {
        JSONObject classes = (JSONObject) json.get("classes");
        for (Object i : classes.entrySet()) {
            Map.Entry<String, String> kvp = (Map.Entry<String, String>) i;
            this.classes.put(kvp.getKey(), kvp.getValue());
        }

        JSONObject methods = (JSONObject) json.get("methods");
        for (Object i : methods.entrySet()) {
            Map.Entry<String, String> kvp = (Map.Entry<String, String>) i;
            this.methods.put(kvp.getKey(), kvp.getValue());
        }

        JSONObject params = (JSONObject) json.get("params");
        for (Object i : params.entrySet()) {
            Map.Entry<String, JSONArray> kvp = (Map.Entry<String, JSONArray>) i;
            this.params.put(kvp.getKey(), (String[]) kvp.getValue().toArray(new String[0]));
        }

        JSONObject fields = (JSONObject) json.get("fields");
        for (Object i : fields.entrySet()) {
            Map.Entry<String, String> kvp = (Map.Entry<String, String>) i;
            this.fields.put(kvp.getKey(), kvp.getValue());
        }
    }
    public SRGData(File joined, int srgVersion) {
        if (srgVersion == 1) {
            parseSrgV1(joined);
        } else if (srgVersion == 2) {
            parseSrgV2(joined);
        }
    }

    public JSONObject serialize() {
        JSONObject out = new JSONObject();

        JSONObject classes = new JSONObject();
        for (Map.Entry<String, String> kvp : this.classes.entrySet()) {
            classes.put(kvp.getKey(), kvp.getValue());
        }

        JSONObject methods = new JSONObject();
        for (Map.Entry<String, String> kvp : this.methods.entrySet()) {
            methods.put(kvp.getKey(), kvp.getValue());
        }

        JSONObject params = new JSONObject();
        for (Map.Entry<String, String[]> kvp : this.params.entrySet()) {
            JSONArray array = new JSONArray();
            array.addAll(Arrays.asList(kvp.getValue()));
            params.put(kvp.getKey(), array);
        }

        JSONObject fields = new JSONObject();
        for (Map.Entry<String, String> kvp : this.fields.entrySet()) {
            fields.put(kvp.getKey(), kvp.getValue());
        }

        out.put("classes", classes);
        out.put("methods", methods);
        out.put("params", params);
        out.put("fields", fields);

        return out;
    }

    private void parseSrgV1(File joined) {
        try (FileReader reader = new FileReader(joined); BufferedReader in = new BufferedReader(reader)) {
            String line;
            while ((line = in.readLine()) != null) {
                if (duplicate.matcher(line).matches()) {
                    continue;
                }

                String[] split = line.split("\\s+");

                if (line.startsWith("CL:")) { // CL: a net/minecraft/util/text/TextFormatting
                    //System.out.println(split[1] + ": " + split[2]);
                    classes.put(split[1], split[2]);
                } else if (line.startsWith("MD:")) { // MD: a/a (I)La; net/minecraft/util/text/TextFormatting/func_175744_a (I)Lnet/minecraft/util/text/TextFormatting;
                    String end = split[1].substring(split[1].lastIndexOf('/') + 1);
                    if (end.equals("toString") || end.equals("equals") || end.equals("hashCode") || end.equals("values") || end.equals("valueOf")) {
                        continue;
                    }

                    //System.out.println(split[1] + ": " + split[3]);
                    methods.put(split[1], split[3].substring(split[3].lastIndexOf('/') + 1));

                    Matcher splitMatcher = paramsSplit.matcher(split[4]);
                    if (!splitMatcher.find()) {
                        AlertUtil.show(Alert.AlertType.ERROR, "Pattern Error", "Line was not of expected pattern.");
                        return;
                    }

                    String input = splitMatcher.group(1);
                    String[] pSplit = convert(split(input));
                    String output = convert(splitMatcher.group(2));

                    //System.out.println("Input: " + Arrays.toString(pSplit));
                    //System.out.println("Output: " + output);

                    params.put(split[3].substring(split[3].lastIndexOf('/') + 1), add(pSplit, output));
                } else if (line.startsWith("FD:")) { // FD: a/A net/minecraft/util/text/TextFormatting/field_96303_A
                    //System.out.println(split[1] + ": " + split[2]);
                    if (split[2].substring(split[2].lastIndexOf('/') + 1).charAt(0) == '$') {
                        continue;
                    }
                    fields.put(split[1], split[2].substring(split[2].lastIndexOf('/') + 1));
                }
            }
        } catch (IOException ex) {
            logger.error("Could not parse SRG.", ex);
            AlertUtil.show(Alert.AlertType.ERROR, "SRG Parse Error", ex.getMessage());
            return;
        }

        convertMethodClasses();
    }

    private void parseSrgV2(File joined) {
        try (FileReader reader = new FileReader(joined); BufferedReader in = new BufferedReader(reader)) {
            String currentClassObf = null;
            String line;
            while ((line = in.readLine()) != null) {
                if (duplicate.matcher(line).matches()) {
                    continue;
                }

                String[] split = line.trim().split("\\s+");

                if (!line.startsWith("\t")) { // a net/minecraft/util/text/TextFormatting
                    currentClassObf = split[0];
                    //System.out.println(split[0] + ": " + split[1]);
                    classes.put(split[0], split[1]);
                } else if (split[1].charAt(0) == '(') { // a (C)La; func_211165_a
                    if (split[0].equals("toString") || split[0].equals("equals") || split[0].equals("hashCode") || split[0].equals("values") || split[0].equals("valueOf")) {
                        continue;
                    }

                    //System.out.println(split[0] + ": " + split[2]);
                    methods.put(currentClassObf + "/" + split[0], split[2]);

                    Matcher splitMatcher = paramsSplit.matcher(split[1]);
                    if (!splitMatcher.find()) {
                        AlertUtil.show(Alert.AlertType.ERROR, "Pattern Error", "Line was not of expected pattern.");
                        return;
                    }

                    String input = splitMatcher.group(1);
                    String[] pSplit = convert(split(input));
                    String output = convert(splitMatcher.group(2));

                    //System.out.println("Input: " + Arrays.toString(pSplit));
                    //System.out.println("Output: " + output);

                    params.put(split[2], add(pSplit, output));
                } else { // a BLACK
                    //System.out.println(split[0] + ": " + split[1]);
                    if (split[1].charAt(0) == '$') {
                        continue;
                    }
                    fields.put(currentClassObf + "/" + split[0], split[1]);
                }
            }
        } catch (IOException ex) {
            logger.error("Could not parse SRG.", ex);
            AlertUtil.show(Alert.AlertType.ERROR, "SRG Parse Error", ex.getMessage());
            return;
        }

        convertMethodClasses();
    }

    private void convertMethodClasses() {
        for (Map.Entry<String, String[]> kvp : params.entrySet()) {
            String[] params = kvp.getValue();
            for (int i = 0; i < params.length; i++) {
                String deobf = classes.get(params[i].indexOf('[') > -1 ? params[i].substring(0, params[i].indexOf('[')) : params[i]);
                if (deobf != null) {
                    params[i] = deobf + (params[i].indexOf('[') > -1 ? params[i].substring(params[i].indexOf('[')) : "");
                }
            }
        }
    }

    private String[] convert(String[] input) {
        String[] retVal = new String[input.length];
        for (int i = 0; i < input.length; i++) {
            retVal[i] = convert(input[i]);
        }
        return retVal;
    }

    private String convert(String input) {
        if (input.equals("V")) {
            return "void";
        }

        String arrayVal = "";
        while (input.charAt(0) == '[') {
            arrayVal += "[]";
            input = input.substring(1);
        }

        if (input.equals("Z")) {
            return "boolean" + arrayVal;
        } else if (input.equals("F")) {
            return "float" + arrayVal;
        } else if (input.equals("D")) {
            return "double" + arrayVal;
        } else if (input.equals("I")) {
            return "int" + arrayVal;
        } else if (input.equals("J")) {
            return "long" + arrayVal;
        } else if (input.equals("B")) {
            return "byte" + arrayVal;
        } else if (input.equals("C")) {
            return "char" + arrayVal;
        } else if (input.equals("S")) {
            return "short" + arrayVal;
        } else if (input.startsWith("L")) {
            return input.substring(1) + arrayVal;
        }

        return input + arrayVal;
    }

    private String[] split(String input) {
        List<String> retVal = new ArrayList<>();
        String[] baseline = input.split(";");

        for (String b : baseline) {
            if (b.isEmpty()) {
                continue;
            }

            retVal.addAll(explode(b));
        }

        return retVal.toArray(new String[0]);
    }

    private List<String> explode(String input) {
        List<String> retVal = new ArrayList<>();
        while (!input.isEmpty()) {
            int at = 0;
            char prim = input.charAt(at);
            while (prim == '[') {
                at++;
                prim = input.charAt(at);
            }

            int length;

            if (prim == 'L') {
                length = input.length();
            } else {
                length = at + 1;
            }

            retVal.add(input.substring(0, length));
            input = input.substring(length);
        }

        return retVal;
    }

    private String[] add(String[] input, String element) {
        String[] retVal = new String[input.length + 1];
        System.arraycopy(input, 0, retVal, 0, input.length);
        retVal[retVal.length - 1] = element;
        return retVal;
    }
}
