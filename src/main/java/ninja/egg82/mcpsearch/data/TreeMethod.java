package ninja.egg82.mcpsearch.data;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TreeMethod {
    private String obfuscatedName;
    private String srgName;
    private String mappedName;
    private String[] srgInput;
    private String[] srgValues;
    private String[]  mappedInput;
    private String output;
    private String description;

    public TreeMethod(JSONObject json) {
        obfuscatedName = (String) json.get("obfName");
        srgName = (String) json.get("srgName");
        mappedName = (String) json.get("mappedName");
        srgInput = (String[]) ((JSONArray) json.get("srgInput")).toArray(new String[0]);
        srgValues = (String[]) ((JSONArray) json.get("srgValues")).toArray(new String[0]);
        mappedInput = (String[]) ((JSONArray) json.get("mappedInput")).toArray(new String[0]);
        output = (String) json.get("output");
        description = (String) json.get("description");
    }

    public TreeMethod(String obfuscatedName, String srgName, String mappedName, String[] srgInput, String[] srgValues, String[] mappedInput, String output, String description) {
        this.obfuscatedName = obfuscatedName;
        this.srgName = srgName;
        this.mappedName = mappedName;
        this.srgInput = srgInput;
        this.srgValues = srgValues;
        this.mappedInput = mappedInput;
        this.output = output;
        this.description = description;
    }

    public String getObfuscatedString() { return (output.indexOf('/') > -1 ? output.substring(output.lastIndexOf('/') + 1) : output) + " " + (obfuscatedName.indexOf('/') > -1 ? obfuscatedName.substring(obfuscatedName.lastIndexOf('/') + 1) : obfuscatedName) + "(" + getList(srgInput) + ")"; }

    public String getObfuscatedName() { return (obfuscatedName.indexOf('/') > -1) ? obfuscatedName.substring(obfuscatedName.lastIndexOf('/') + 1) : obfuscatedName; }

    public String getSRGName() { return srgName; }

    public String getMappedName() { return mappedName; }

    public String getSRGString() { return (output.indexOf('/') > -1 ? output.substring(output.lastIndexOf('/') + 1) : output) + " " + srgName + "(" + getList(srgInput) + ")"; }

    public String getMappedString() { return (output.indexOf('/') > -1 ? output.substring(output.lastIndexOf('/') + 1) : output) + " " + (mappedName != null ? mappedName : srgName) + "(" + getList(srgInput, mappedInput) + ")"; }

    public String[] getSRGInput() { return srgInput; }

    public String[] getSRGValues() { return srgValues; }

    public String[] getMappedInput() { return mappedInput; }

    public String getOutput() { return output; }

    public String getDescription() { return description; }

    public JSONObject serialize() {
        JSONObject out = new JSONObject();

        out.put("obfName", obfuscatedName);
        out.put("srgName", srgName);
        out.put("mappedName", mappedName);

        JSONArray srgInput = new JSONArray();
        for (String s : this.srgInput) {
            srgInput.add(s);
        }
        out.put("srgInput", srgInput);

        JSONArray srgValues = new JSONArray();
        for (String s : this.srgValues) {
            srgValues.add(s);
        }
        out.put("srgValues", srgValues);

        JSONArray mappedInput = new JSONArray();
        if (this.mappedInput != null) {
            for (String s : this.mappedInput) {
                mappedInput.add(s);
            }
        }
        out.put("mappedInput", mappedInput);

        out.put("output", output);
        out.put("description", description);

        return out;
    }

    private String getList(String[] input) {
        if (input == null || input.length == 0) {
            return "";
        }

        StringBuilder retVal = new StringBuilder();
        for (String i : input) {
            retVal.append((i.indexOf('/') > -1 ? i.substring(i.lastIndexOf('/') + 1) : i) + ", ");
        }
        retVal.delete(retVal.length() - 2, retVal.length());
        return retVal.toString();
    }

    private String getList(String[] input, String[] mapped) {
        if (input == null || input.length == 0) {
            return "";
        }

        StringBuilder retVal = new StringBuilder();
        for (int i = 0 ; i < input.length; i++) {
            retVal.append((input[i].indexOf('/') > -1 ? input[i].substring(input[i].lastIndexOf('/') + 1) : input[i]) + (i < mapped.length && mapped[i] != null ? " " + mapped[i] : " " + (srgValues[i] != null ? srgValues[i] : "?")) + ", ");
        }
        retVal.delete(retVal.length() - 2, retVal.length());
        return retVal.toString();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TreeMethod)) {
            return false;
        }

        TreeMethod o = (TreeMethod) obj;

        return new EqualsBuilder().append(obfuscatedName, o.obfuscatedName).append(srgName, o.srgName).append(mappedName, o.mappedName).append(srgInput, o.srgInput).append(srgValues, o.srgValues).append(mappedInput, o.mappedInput).append(output, o.output).append(description, o.description).build();
    }

    public int hashCode() {
        return new HashCodeBuilder(6247, 14713).append(obfuscatedName).append(srgName).append(mappedName).append(srgInput).append(srgValues).append(mappedInput).append(output).append(description).build();
    }
}
