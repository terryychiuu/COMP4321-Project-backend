package net.searchengine.searchengine.searching;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The {@code SearchEngineUtils} class is a utility class
 * that contains utility methods for handling document vectors
 * to adopt vector space model in the search engine
 */
public class SearchEngineUtils {

    /**
     * Calculate Cosine Similarity between two batch of term
     * @param vec1 a map between a keyword and weight
     * @param vec2 a map between a keyword and weight
     * @return a floating point value after calculating Cosine Similarity
     */
    public static double calculateCosineSimilarity(Map<String, Double> vec1, Map<String, Double> vec2) {
        Set<String> intersection = new HashSet<>(vec1.keySet());
        intersection.retainAll(vec2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String key : intersection) {
            double val1 = vec1.get(key);
            double val2 = vec2.get(key);

            dotProduct += val1 * val2;
            norm1 += val1 * val1;
            norm2 += val2 * val2;
        }

        for (String key : vec1.keySet()) {
            if (!intersection.contains(key)) {
                double val1 = vec1.get(key);
                norm1 += val1 * val1;
            }
        }

        for (String key : vec2.keySet()) {
            if (!intersection.contains(key)) {
                double val2 = vec2.get(key);
                norm2 += val2 * val2;
            }
        }

        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        } else {
            return dotProduct / (norm1 * norm2);
        }
    }

}
