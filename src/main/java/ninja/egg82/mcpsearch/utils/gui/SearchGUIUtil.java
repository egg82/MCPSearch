package ninja.egg82.mcpsearch.utils.gui;

import ninja.egg82.mcpsearch.data.TreeClass;
import ninja.egg82.mcpsearch.data.TreeField;
import ninja.egg82.mcpsearch.data.TreeMethod;
import ninja.egg82.mcpsearch.utils.SearchUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public class SearchGUIUtil {
    public static Map<String, TreeClass> search(Map<String, TreeClass> haystack, String needle, boolean filterMethods, boolean filterFields, boolean exact) {
        needle = needle.toLowerCase();

        Map<String, TreeClass> retVal = new LinkedHashMap<>();

        searchInternal(retVal, haystack, needle, filterMethods, filterFields, exact);
        /*if (exact) {
            String[] split = needle.split("\\s+");
            if (split.length > 1) {
                for (String newNeedle : split) {
                    searchInternal(retVal, haystack, newNeedle, filterMethods, filterFields, exact);
                }
            }
        }*/

        return retVal;
    }

    private static void searchInternal(Map<String, TreeClass> retVal, Map<String, TreeClass> haystack, String needle, boolean filterMethods, boolean filterFields, boolean exact) {
        // Exact
        for (Map.Entry<String, TreeClass> kvp : haystack.entrySet()) {
            if (retVal.containsValue(kvp.getValue())) {
                continue;
            }
            if (kvp.getValue().getObfuscatedName().equalsIgnoreCase(needle) || kvp.getValue().getSRGName().equalsIgnoreCase(needle)) {
                TreeClass trimmed = trim(kvp.getValue(), needle, filterMethods, filterFields, exact, true);
                if (trimmed != null) {
                    retVal.put(kvp.getKey(), trimmed);
                }
            }
        }
        // Substring
        for (Map.Entry<String, TreeClass> kvp : haystack.entrySet()) {
            if (retVal.containsValue(kvp.getValue())) {
                continue;
            }
            if (kvp.getValue().getObfuscatedName().toLowerCase().contains(needle) || kvp.getValue().getSRGName().toLowerCase().contains(needle)) {
                TreeClass trimmed = trim(kvp.getValue(), needle, filterMethods, filterFields, exact, true);
                if (trimmed != null) {
                    retVal.put(kvp.getKey(), trimmed);
                }
            }
        }
        if (!exact) {
            // Levenshtein
            for (Map.Entry<String, TreeClass> kvp : haystack.entrySet()) {
                if (retVal.containsValue(kvp.getValue())) {
                    continue;
                }
                if (SearchUtil.getDistance(kvp.getValue().getObfuscatedName().toLowerCase(), needle) <= 2 || SearchUtil.getDistance(kvp.getValue().getSRGName().toLowerCase(), needle) <= 2) {
                    TreeClass trimmed = trim(kvp.getValue(), needle, filterMethods, filterFields, exact, true);
                    if (trimmed != null) {
                        retVal.put(kvp.getKey(), trimmed);
                    }
                }
            }
            // Substring Levenshtein
            for (Map.Entry<String, TreeClass> kvp : haystack.entrySet()) {
                if (retVal.containsValue(kvp.getValue())) {
                    continue;
                }
                if (SearchUtil.getDistanceSubstring(kvp.getValue().getObfuscatedName().toLowerCase(), needle) <= 1 || SearchUtil.getDistanceSubstring(kvp.getValue().getSRGName().toLowerCase(), needle) <= 1) {
                    TreeClass trimmed = trim(kvp.getValue(), needle, filterMethods, filterFields, exact, true);
                    if (trimmed != null) {
                        retVal.put(kvp.getKey(), trimmed);
                    }
                }
            }
        }

        // Inner classes/fields/methods
        for (Map.Entry<String, TreeClass> kvp : haystack.entrySet()) {
            if (retVal.containsValue(kvp.getValue())) {
                continue;
            }
            TreeClass clazz = trim(kvp.getValue(), needle, filterMethods, filterFields, exact, false);
            if (clazz == null) {
                continue;
            }

            if (!filterFields) {
                for (TreeField field : kvp.getValue().getFields()) {
                    if (!clazz.getFields().contains(field)) {
                        clazz.getFields().add(field);
                    }
                }
            }
            if (!filterMethods) {
                for (TreeMethod method : kvp.getValue().getMethods()) {
                    if (!clazz.getMethods().contains(method)) {
                        clazz.getMethods().add(method);
                    }
                }
            }

            retVal.put(kvp.getKey(), clazz);
        }
    }

    private static TreeClass trim(TreeClass clazz, String search, boolean filterMethods, boolean filterFields, boolean exact, boolean addExtras) {
        TreeClass retVal = new TreeClass(clazz.getObfuscatedName(), clazz.getSRGName());

        // Exact
        for (TreeField field : clazz.getFields()) {
            if (field.getObfuscatedName().equalsIgnoreCase(search) || field.getSRGName().equalsIgnoreCase(search) || (field.getMappedName() != null && field.getMappedName().equalsIgnoreCase(search)) || (field.getDescription() != null && field.getDescription().equalsIgnoreCase(search))) {
                retVal.getFields().add(field);
            }
        }
        // Substring
        for (TreeField field : clazz.getFields()) {
            if (retVal.getFields().contains(field)) {
                continue;
            }
            if (field.getObfuscatedName().toLowerCase().contains(search) || field.getSRGName().toLowerCase().contains(search) || (field.getMappedName() != null && field.getMappedName().toLowerCase().contains(search)) || (field.getDescription() != null && field.getDescription().toLowerCase().contains(search))) {
                retVal.getFields().add(field);
            }
        }
        if (!exact) {
            // Levenshtein
            for (TreeField field : clazz.getFields()) {
                if (retVal.getFields().contains(field)) {
                    continue;
                }
                if (SearchUtil.getDistance(field.getObfuscatedName().toLowerCase(), search) <= 2 || SearchUtil.getDistance(field.getSRGName().toLowerCase(), search) <= 2 || (field.getMappedName() != null && SearchUtil.getDistance(field.getMappedName().toLowerCase(), search) <= 2) || (field.getDescription() != null && SearchUtil.getDistance(field.getDescription().toLowerCase(), search) <= 2)) {
                    retVal.getFields().add(field);
                }
            }
            // Substring Levenshtein
            if (search.length() > 3) {
                for (TreeField field : clazz.getFields()) {
                    if (retVal.getFields().contains(field)) {
                        continue;
                    }
                    if (SearchUtil.getDistanceSubstring(field.getObfuscatedName().toLowerCase(), search) <= 1 || SearchUtil.getDistanceSubstring(field.getSRGName().toLowerCase(), search) <= 1 || (field.getMappedName() != null && SearchUtil.getDistanceSubstring(field.getMappedName().toLowerCase(), search) <= 1) || (field.getDescription() != null && SearchUtil.getDistanceSubstring(field.getDescription().toLowerCase(), search) <= 1)) {
                        retVal.getFields().add(field);
                    }
                }
            }
        }

        if (addExtras && !filterFields) {
            for (TreeField field : clazz.getFields()) {
                if (!retVal.getFields().contains(field)) {
                    retVal.getFields().add(field);
                }
            }
        }

        // Exact
        for (TreeMethod method : clazz.getMethods()) {
            if (method.getObfuscatedName().equalsIgnoreCase(search) || method.getSRGName().equalsIgnoreCase(search) || method.getObfuscatedString().equalsIgnoreCase(search) || method.getSRGString().equalsIgnoreCase(search) || method.getMappedString().equalsIgnoreCase(search) || (method.getMappedName() != null && method.getMappedName().equalsIgnoreCase(search)) || (method.getDescription() != null && method.getDescription().equalsIgnoreCase(search))) {
                retVal.getMethods().add(method);
            }
            for (String input : method.getSRGValues()) {
                if (input != null && input.equalsIgnoreCase(search)) {
                    retVal.getMethods().add(method);
                    break;
                }
            }
        }
        // Substring
        for (TreeMethod method : clazz.getMethods()) {
            if (retVal.getMethods().contains(method)) {
                continue;
            }
            if (method.getObfuscatedName().toLowerCase().contains(search) || method.getSRGName().toLowerCase().contains(search) || method.getObfuscatedString().toLowerCase().contains(search) || method.getSRGString().toLowerCase().contains(search) || method.getMappedString().toLowerCase().contains(search) || (method.getMappedName() != null && method.getMappedName().toLowerCase().contains(search)) || (method.getDescription() != null && method.getDescription().toLowerCase().contains(search))) {
                retVal.getMethods().add(method);
            }
            for (String input : method.getSRGValues()) {
                if (input != null && input.toLowerCase().contains(search)) {
                    retVal.getMethods().add(method);
                    break;
                }
            }
        }
        if (!exact) {
            // Levenshtein
            for (TreeMethod method : clazz.getMethods()) {
                if (retVal.getMethods().contains(method)) {
                    continue;
                }
                if (SearchUtil.getDistance(method.getObfuscatedName().toLowerCase(), search) <= 2 || SearchUtil.getDistance(method.getObfuscatedString().toLowerCase(), search) <= 2 || SearchUtil.getDistance(method.getSRGName().toLowerCase(), search) <= 2 || SearchUtil.getDistance(method.getSRGString().toLowerCase(), search) <= 2 || SearchUtil.getDistance(method.getMappedString().toLowerCase(), search) <= 2 || (method.getMappedName() != null && SearchUtil.getDistance(method.getMappedName().toLowerCase(), search) <= 2) || (method.getDescription() != null && SearchUtil.getDistance(method.getDescription().toLowerCase(), search) <= 2)) {
                    retVal.getMethods().add(method);
                }
                for (String input : method.getSRGValues()) {
                    if (input != null && SearchUtil.getDistance(input.toLowerCase(), search) <= 2) {
                        retVal.getMethods().add(method);
                        break;
                    }
                }
            }
            // Substring Levenshtein
            if (search.length() > 3) {
                for (TreeMethod method : clazz.getMethods()) {
                    if (retVal.getMethods().contains(method)) {
                        continue;
                    }
                    if (SearchUtil.getDistanceSubstring(method.getObfuscatedName().toLowerCase(), search) <= 1 || SearchUtil.getDistanceSubstring(method.getObfuscatedString().toLowerCase(), search) <= 1 || SearchUtil.getDistanceSubstring(method.getSRGName().toLowerCase(), search) <= 1 || SearchUtil.getDistanceSubstring(method.getSRGString().toLowerCase(), search) <= 1 || SearchUtil.getDistanceSubstring(method.getMappedString().toLowerCase(), search) <= 1 || (method.getMappedName() != null && SearchUtil.getDistanceSubstring(method.getMappedName().toLowerCase(), search) <= 1) || (method.getDescription() != null && SearchUtil.getDistanceSubstring(method.getDescription().toLowerCase(), search) <= 1)) {
                        retVal.getMethods().add(method);
                    }
                    for (String input : method.getSRGValues()) {
                        if (input != null && SearchUtil.getDistanceSubstring(input.toLowerCase(), search) <= 1) {
                            retVal.getMethods().add(method);
                            break;
                        }
                    }
                }
            }
        }

        if (addExtras && !filterMethods) {
            for (TreeMethod method : clazz.getMethods()) {
                if (!retVal.getMethods().contains(method)) {
                    retVal.getMethods().add(method);
                }
            }
        }

        for (TreeClass clazz2 : clazz.getClasses()) {
            TreeClass trimmed = trim(clazz2, search, filterMethods, filterFields, exact, addExtras);
            if (trimmed != null) {
                retVal.getClasses().add(trimmed);
            }
        }

        return (retVal.getFields().isEmpty() && retVal.getMethods().isEmpty() && retVal.getClasses().isEmpty()) ? null : retVal;
    }
}
