package net.searchengine.searchengine.searching;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SearchEngineUtilsTest {
    @Test
    void calculateCosineSimilarity() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("dancing", 1.);
        v1.put("mushroom", 3.);
        Map<String, Double> v2 = new HashMap<>();
        v2.put("ust", 2.);
        v2.put("mushroom", 4.);
        double similarity = SearchEngineUtils.calculateCosineSimilarity(v1, v2);
        assertEquals(12. / Math.sqrt(10) / Math.sqrt(20), similarity, 1e-6);
    }

    @Test
    void calculateCosineSimilarityWithZeroRelationship() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("dancing", 1.);
        v1.put("mushroom", 3.);
        Map<String, Double> v2 = new HashMap<>();
        v2.put("ust", 2.);
        v2.put("student", 4.);
        double similarity = SearchEngineUtils.calculateCosineSimilarity(v1, v2);
        assertEquals(0.0, similarity, 1e-6);
    }

}