package ninja.egg82.mcpsearch.data;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.simple.JSONObject;

public class TreeField {
    private String obfuscatedName;
    private String srgName;
    private String mappedName;
    private String description;

    public TreeField(JSONObject json) {
        obfuscatedName = (String) json.get("obfName");
        srgName = (String) json.get("srgName");
        mappedName = (String) json.get("mappedName");
        description = (String) json.get("description");
    }

    public TreeField(String obfuscatedName, String srgName, String mappedName, String description) {
        this.obfuscatedName = obfuscatedName;
        this.srgName = srgName;
        this.mappedName = mappedName;
        this.description = description;
    }

    public String getObfuscatedName() { return (obfuscatedName.indexOf('/') > -1) ? obfuscatedName.substring(obfuscatedName.lastIndexOf('/') + 1) : obfuscatedName; }

    public String getSRGName() { return srgName; }

    public String getMappedName() { return mappedName; }

    public String getDescription() { return description; }

    public JSONObject serialize() {
        JSONObject out = new JSONObject();

        out.put("obfName", obfuscatedName);
        out.put("srgName", srgName);
        out.put("mappedName", mappedName);
        out.put("description", description);

        return out;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TreeField)) {
            return false;
        }

        TreeField o = (TreeField) obj;

        return new EqualsBuilder().append(obfuscatedName, o.obfuscatedName).append(srgName, o.srgName).append(mappedName, o.mappedName).append(description, o.description).build();
    }

    public int hashCode() {
        return new HashCodeBuilder(17551, 30931).append(obfuscatedName).append(srgName).append(mappedName).append(description).build();
    }
}
