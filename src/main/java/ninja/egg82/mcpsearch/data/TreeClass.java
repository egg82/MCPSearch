package ninja.egg82.mcpsearch.data;

import ninja.egg82.mcpsearch.utils.SearchUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TreeClass  {
    private String obfuscatedName;
    private String srgName;

    private List<TreeMethod> methods = new ArrayList<>();
    private List<TreeField> fields = new ArrayList<>();
    private List<TreeClass> classes = new ArrayList<>();

    public TreeClass(JSONObject json) {
        obfuscatedName = (String) json.get("obfName");
        srgName = (String) json.get("srgName");

        for (Object o : (JSONArray) json.get("methods")) {
            methods.add(new TreeMethod((JSONObject) o));
        }
        for (Object o : (JSONArray) json.get("fields")) {
            fields.add(new TreeField((JSONObject) o));
        }
        for (Object o : (JSONArray) json.get("classes")) {
            classes.add(new TreeClass((JSONObject) o));
        }
    }

    public TreeClass(String obfuscatedName, String srgName) {
        this.obfuscatedName = obfuscatedName;
        this.srgName = srgName;
    }

    public String getObfuscatedName() { return obfuscatedName; }

    public String getSRGName() { return srgName; }

    public List<TreeMethod> getMethods() { return methods; }

    public List<TreeField> getFields() { return fields; }

    public List<TreeClass> getClasses() { return classes; }

    public JSONObject serialize() {
        JSONObject out = new JSONObject();

        out.put("obfName", obfuscatedName);
        out.put("srgName", srgName);

        JSONArray methods = new JSONArray();
        for (TreeMethod method : this.methods) {
            methods.add(method.serialize());
        }
        out.put("methods", methods);

        JSONArray fields = new JSONArray();
        for (TreeField field : this.fields) {
            fields.add(field.serialize());
        }
        out.put("fields", fields);

        JSONArray classes = new JSONArray();
        for (TreeClass clazz : this.classes) {
            classes.add(clazz.serialize());
        }
        out.put("classes", classes);

        return out;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TreeClass)) {
            return false;
        }

        TreeClass o = (TreeClass) obj;

        return new EqualsBuilder().append(obfuscatedName, o.obfuscatedName).append(srgName, o.srgName).append(methods.toArray(), o.methods.toArray()).append(fields.toArray(), o.fields.toArray()).append(classes.toArray(), o.classes.toArray()).build();
    }

    public int hashCode() {
        return new HashCodeBuilder(41203, 30223).append(obfuscatedName).append(srgName).append(methods.toArray()).append(fields.toArray()).append(classes.toArray()).build();
    }
}

