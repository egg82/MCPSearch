package ninja.egg82.mcpsearch.utils;

import org.apache.commons.lang3.StringUtils;

public class SearchUtil {
    public static int getDistance(String haystack, String needle) {
        return StringUtils.getLevenshteinDistance(haystack, needle);
    }

    public static int getDistanceSubstring(String haystack, String needle) {
        if (haystack.length() <= needle.length()) {
            return Integer.MAX_VALUE;
        }

        int max = haystack.length() - needle.length();
        int distance = Integer.MAX_VALUE;
        for (int i = 0; i < max; i++) {
            distance = Math.min(distance, StringUtils.getLevenshteinDistance(haystack.substring(i, i + needle.length()), needle));
        }
        return distance;
    }
}
