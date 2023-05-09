package net.searchengine.searchengine.searching;

import net.searchengine.searchengine.indexing.*;

import java.io.IOException;
import java.util.*;

public class SearchEngine {
    MappingIndex wordMappingIndex;
    InvertedIndex titleInvertedIndex;
    InvertedIndex wordInvertedIndex;
    PageProperties urlPageProperties;
    ForwardIndex pageTitleForwardIndex;
    ForwardIndex pageWordsForwardIndex;

    ForwardIndex pageTitleSimilarityForwardIndex;
    ForwardIndex pageWordSimilarityForwardIndex;

    Query query;

    HashMap<Integer, Double> termWeight;

    public SearchEngine(String query, Indexer indexer) throws IOException {
        this.wordMappingIndex = indexer.getWordMappingIndex();
        this.titleInvertedIndex = indexer.getTitleInvertedIndex();
        this.wordInvertedIndex = indexer.getWordInvertedIndex();
        this.urlPageProperties = indexer.getUrlPageProperties();
        this.pageTitleForwardIndex = indexer.getPageTitleForwardIndex();
        this.pageWordsForwardIndex = indexer.getPageWordsForwardIndex();

        this.pageTitleSimilarityForwardIndex = indexer.getPageTitleSimilarityForwardIndex();
        this.pageWordSimilarityForwardIndex = indexer.getPageWordSimilarityForwardIndex();

        this.query = new Query(query, this.titleInvertedIndex, this.wordInvertedIndex);
    }

    /**
     * This method calculate the page similarity
     * @param TITLE_RATIO is the weight for favoring title matches
     * @return a map of results, where key is pageId, and value is the weighted similarity score
     */
    public Map<Integer, Double> calculatePageSimilarity(double TITLE_RATIO) throws IOException {
        Map<Integer, Double> results = new HashMap<>();

        Map<Integer, Double> pageTitleSimilarity = getPageTermSimilarity(true);
        Map<Integer, Double> pageWordSimilarity = getPageTermSimilarity(false);

        for (Integer key : pageTitleSimilarity.keySet()) {
            double titleSimilarity = pageTitleSimilarity.get(key);

            if (pageWordSimilarity.containsKey(key)) {
                double wordSimilarity = pageWordSimilarity.get(key);
                double value = ((TITLE_RATIO * titleSimilarity) + ((1-TITLE_RATIO) * wordSimilarity));
                results.put(key, value);
                pageWordSimilarity.remove(key);
            } else {
                results.put(key, TITLE_RATIO * titleSimilarity);
            }
        }
        for (Map.Entry<Integer, Double> entry : pageWordSimilarity.entrySet()) {
            results.put(entry.getKey(), (1-TITLE_RATIO) * entry.getValue());
        }


        return results;


    }

    /**
     * This method the helper function for calculating the page similarity
     * @param isTitle specify if is from page's title corpus or page's content
     * @return a map of results, where key is pageId, and value is the similarity score
     */
    private Map<Integer, Double> getPageTermSimilarity(boolean isTitle) throws IOException {

        Map<Integer, Double> pageSimilarity = new HashMap<>();
        HashMap<String, Double> queryTermVector = this.query.getQueryVector();


        Map<Integer, Map<String, Double>> pageTermVector;
        if (isTitle) pageTermVector = pageTitleSimilarityForwardIndex.getPageTermVectorForwardIndex();
        else pageTermVector = pageWordSimilarityForwardIndex.getPageTermVectorForwardIndex();

        pageTermVector.forEach((page, termVector) -> {
            pageSimilarity.put(page, SearchEngineUtils.calculateCosineSimilarity(queryTermVector, (HashMap<String, Double>) termVector));
        });

        return pageSimilarity;
    }

    public static void main(String[] args) throws IOException {
        Indexer indexer = new Indexer();
        long startTime = System.nanoTime();
        SearchEngine searchEngine = new SearchEngine("news \"CSE department\" \"Neil Diamond\"", indexer);

//        System.out.println(searchEngine.query);

        searchEngine.calculatePageSimilarity(0.6);
        System.out.println( indexer.getUrlMappingIndex().getKey(742));



        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        System.out.println("Execution time: " + duration / 1000000 + " ms");
    }

}
