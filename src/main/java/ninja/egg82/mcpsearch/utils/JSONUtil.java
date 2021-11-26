package ninja.egg82.mcpsearch.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JSONUtil {
    private JSONUtil() { }

    public static void write(JSONAware json, File file) throws IOException {
        try (FileWriter fileOut = new FileWriter(file); BufferedWriter out = new BufferedWriter(fileOut)) {
            out.write(json.toJSONString());
        }
    }

    public static JSONObject readObject(File file) throws IOException, ParseException, ClassCastException {
        return (JSONObject) new JSONParser().parse(FileUtil.readFileString(file));
    }

    public static JSONArray readArray(File file) throws IOException, ParseException, ClassCastException {
        return (JSONArray) new JSONParser().parse(FileUtil.readFileString(file));
    }
}
