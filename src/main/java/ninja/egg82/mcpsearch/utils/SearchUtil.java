package ninja.egg82.mcpsearch.utils;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class SearchUtil {
    private static LevenshteinDistance distanceFunction = LevenshteinDistance.getDefaultInstance();

    public static int getDistance(String haystack, String needle) {
        return distanceFunction.apply(haystack, needle);
    }

    public static int getDistanceSubstring(String haystack, String needle) {
        if (haystack.length() <= needle.length()) {
            return Integer.MAX_VALUE;
        }

        int max = haystack.length() - needle.length();
        int distance = Integer.MAX_VALUE;
        for (int i = 0; i < max; i++) {
            distance = Math.min(distance, distanceFunction.apply(haystack.substring(i, i + needle.length()), needle));
        }
        return distance;
    }
}
