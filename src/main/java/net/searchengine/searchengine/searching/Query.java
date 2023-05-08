package net.searchengine.searchengine.searching;

import net.searchengine.searchengine.indexing.InvertedIndex;
import net.searchengine.searchengine.indexing.MappingIndex;
import net.searchengine.searchengine.indexing.Posting;
import net.searchengine.searchengine.nlp.NLPUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Query {

    private List<String> tokenizedStopStemQuery;

    private HashMap<String, Double> queryVector;


    public Query(String query, InvertedIndex titleInvertedIndex, InvertedIndex wordInvertedIndex) throws IOException {
        List<String> tokenizedQuery = tokenizeQuery(query);
        this.tokenizedStopStemQuery = stopStemQuery(tokenizedQuery);

        System.out.println("tokenizedStopStemQuery "+tokenizedStopStemQuery);

        this.queryVector = new HashMap<>();

        for (String q : tokenizedStopStemQuery) {

            Double qTermVector = queryVector.get(q);
            if (qTermVector == null) {
                queryVector.put(q, 1.);
            } else {
                queryVector.put(q, qTermVector+1.);
                continue;
            }

        }
    }

    public static List<String> tokenizeQuery(String query) {
        List<String> tokenizedQuery = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        boolean shouldEscape = false;
        for (char c : query.toCharArray()) {
            if (c == '"') {
                shouldEscape = !shouldEscape;
            } else if (c == ' ' && !shouldEscape) {
                if (buffer.length() > 0)
                    tokenizedQuery.add(buffer.toString());
                buffer = new StringBuilder();
            } else {
                buffer.append(c);
            }
        }
        if (buffer.length() > 0)
            tokenizedQuery.add(buffer.toString());
        return tokenizedQuery;
    }

    public static List<String> stopStemQuery(List<String> query) {
        return query.stream()
//                .map(QueryUtils::extractSpecialTokens)
                .filter(NLPUtils::stopwordFilter)
                .map(NLPUtils::stemFilter)
                .collect(Collectors.toList());
    }

    public List<String> getTokenizedStopStemQuery() {
        return tokenizedStopStemQuery;
    }

    public HashMap<String, Double> getQueryVector() {
        return queryVector;
    }

    public Set<String> getQueryKeySet() {
        return queryVector.keySet();
    }

//    public HashMap<Integer, Integer> getQueryForTitleLookUpTable() {
//        return queryForTitleLookUpTable;
//    }
//
//    public HashMap<Integer, Integer> getQueryForWordLookUpTable() {
//        return queryForWordLookUpTable;
//    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Query{");
        sb.append("tokenizedStopStemQuery=").append(tokenizedStopStemQuery);
        sb.append('}');
        return sb.toString();
    }
}
