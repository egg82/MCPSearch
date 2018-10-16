package ninja.egg82.mcpsearch.utils;

import ninja.egg82.concurrent.DynamicConcurrentDeque;
import ninja.egg82.concurrent.IConcurrentDeque;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONUtil {
    private static IConcurrentDeque<JSONParser> pool = new DynamicConcurrentDeque<JSONParser>(); // JSONParser is not stateless and thus requires a pool in multi-threaded environments

    static {
        pool.add(new JSONParser());
    }

    private JSONUtil() {}

    public static JSONObject parseObject(String input) throws ParseException, ClassCastException {
        JSONParser parser = getParser();
        JSONObject retVal = (JSONObject) parser.parse(input);
        pool.add(parser);
        return retVal;
    }

    public static JSONArray parseArray(String input) throws ParseException, ClassCastException {
        JSONParser parser = getParser();
        JSONArray retVal = (JSONArray) parser.parse(input);
        pool.add(parser);
        return retVal;
    }

    public static void write(JSONObject object, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(object.toJSONString());
        }
    }

    public static void write(JSONArray array, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(array.toJSONString());
        }
    }

    public static JSONObject readObject(File file) throws IOException, ParseException, ClassCastException {
        return (JSONObject) getParser().parse(new FileReader(file));
    }
    public static JSONArray readArray(File file) throws IOException, ParseException, ClassCastException {
        return (JSONArray) getParser().parse(new FileReader(file));
    }

    private static JSONParser getParser() {
        JSONParser parser = pool.pollFirst();
        if (parser == null) {
            parser = new JSONParser();
        }
        return parser;
    }
}
