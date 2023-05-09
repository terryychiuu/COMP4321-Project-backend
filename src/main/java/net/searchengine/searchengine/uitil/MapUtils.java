package net.searchengine.searchengine.uitil;

import java.util.*;

public class MapUtils {

    public static Map<Integer, Integer> getTopFrequencies(List<Integer> list1, List<Integer> list2) {
        // Combine the two lists into a single list
        List<Integer> allIds = new ArrayList<>(list1);
        allIds.addAll(list2);

        // Calculate the frequency of each ID using a Map
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int id : allIds) {
            int count = frequencyMap.getOrDefault(id, 0);
            frequencyMap.put(id, count + 1);
        }

        // Sort the Map by value (frequency) in descending order
        List<Map.Entry<Integer, Integer>> entryList = new ArrayList<>(frequencyMap.entrySet());
        entryList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Create a new Map with only the top 3 frequencies
        Map<Integer, Integer> topFrequencies = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : entryList) {
            topFrequencies.put(entry.getKey(), entry.getValue());
            count++;
            if (!(count < 10)) {
                break;
            }
        }

        return topFrequencies;
    }
    // Unused function
//    public static Map<Integer, Integer> combineMapsByAggregate(Map<Integer, Integer> map1, Map<Integer, Integer> map2) {
//        Map<Integer, Integer> combinedMap = new HashMap<>(map1);
//
//        for (Map.Entry<Integer, Integer> entry : map2.entrySet()) {
//            Integer key = entry.getKey();
//            Integer value = entry.getValue();
//
//            if (combinedMap.containsKey(key)) {
//                combinedMap.put(key, combinedMap.get(key) + value);
//            } else {
//                combinedMap.put(key, value);
//            }
//        }
//
//        return combinedMap;
//    }

    public static Map<Integer, Integer> getFrequencyMap(List<Integer> wordIdList) {
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        for (Integer wordId : wordIdList) {
            if (frequencyMap.containsKey(wordId)) {
                frequencyMap.put(wordId, frequencyMap.get(wordId) + 1);
            } else {
                frequencyMap.put(wordId, 1);
            }
        }

        return frequencyMap;
    }

    // Unused function
//    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
//        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
//        list.sort(Map.Entry.comparingByValue());
//
//        Map<K, V> result = new LinkedHashMap<>();
//        for (Map.Entry<K, V> entry : list) {
//            result.put(entry.getKey(), entry.getValue());
//        }
//
//        return result;
//    }
//
//    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescending(Map<K, V> map) {
//        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
//        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
//
//        Map<K, V> result = new LinkedHashMap<>();
//        for (Map.Entry<K, V> entry : list) {
//            result.put(entry.getKey(), entry.getValue());
//        }
//
//        return result;
//    }
}
