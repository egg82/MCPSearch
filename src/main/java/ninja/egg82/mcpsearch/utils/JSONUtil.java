package ninja.egg82.mcpsearch.utils;

import java.io.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class JSONUtil {
    private JSONUtil() {}

    public static void write(JSONAware json, File file) throws IOException {
        try (
                FileWriter fileOut = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fileOut)
        ) {
            out.write(json.toJSONString());
        }
    }

    public static JSONObject readObject(File file) throws IOException, ParseException, ClassCastException {
        return ninja.egg82.json.JSONUtil.parseObject(getStringFromFile(file));
    }

    public static JSONArray readArray(File file) throws IOException, ParseException, ClassCastException {
        return ninja.egg82.json.JSONUtil.parseArray(getStringFromFile(file));
    }

    private static String getStringFromFile(File file) throws IOException {
        try (
                FileReader fileIn = new FileReader(file);
                BufferedReader in = new BufferedReader(fileIn)
        ) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line);
                builder.append(System.lineSeparator());
            }
            return builder.toString().trim();
        }
    }
}
