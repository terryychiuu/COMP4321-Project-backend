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

//        System.out.println("___results");
//        results.forEach((k,v) -> {
//            System.out.println(k+": "+v);
//        });

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

        //
//        System.out.println("___queryTermVector");
//        queryTermVector.forEach((k,v) -> {
//            System.out.println(k+": "+v);
//        });
//        System.out.println();
        //=

        Map<Integer, Map<String, Double>> pageTermVector;
//        if (isTitle) pageTermVector = calculatePageTermVector(true);
//        else pageTermVector = calculatePageTermVector(false);
        if (isTitle) pageTermVector = pageTitleSimilarityForwardIndex.getPageTermVectorForwardIndex();
        else pageTermVector = pageWordSimilarityForwardIndex.getPageTermVectorForwardIndex();

        pageTermVector.forEach((page, termVector) -> {
            pageSimilarity.put(page, SearchEngineUtils.calculateCosineSimilarity(queryTermVector, (HashMap<String, Double>) termVector));
        });

        //
//        System.out.println("___pageSimilarity");
//        pageSimilarity.forEach((k,v) -> {
//            System.out.println(k+": "+v);
//        });
        //

        return pageSimilarity;
    }


//    public Map<Integer, Map<String, Double>> calculatePageTermVector(boolean isTitle) throws IOException {
//        Map<Integer, Map<String, Double>> pageTermVector = new HashMap<>(); // page -> (word -> termWeight)[]
//
//        InvertedIndex invertedIndex;
//        if (isTitle) invertedIndex = this.titleInvertedIndex;
//        else invertedIndex = this.wordInvertedIndex;
//
//        int numOfDocuments = this.urlPageProperties.getAllPageIds().size();
////        int docFreq = postingList1.keySet().size();
//
//        ForwardIndex forwardIndex;
//        if (isTitle) forwardIndex = this.pageTitleForwardIndex;
//        else forwardIndex = this.pageWordsForwardIndex;
//
//        Map<Integer, Integer> pageMaxTermFreq = new HashMap<>();
//        for (Map.Entry<Integer, Vector<Integer>> entry : forwardIndex.getIdListForwardIndex().entrySet()){
//            int m = mostFrequent(entry.getValue());
//            pageMaxTermFreq.put(entry.getKey(), m);
//        }
//
//
//        for (Map.Entry<Integer, HashMap<Integer, Posting>> entry : invertedIndex.getInvertedIndex().entrySet()) {
//            Integer wordId = entry.getKey();
//            HashMap<Integer, Posting> postingList = entry.getValue();
////            System.out.println(wordId);
//
//            String wordString = wordMappingIndex.getKey(wordId);
//
//            int docFreq = postingList.entrySet().size();
//
//            for (Map.Entry<Integer, Posting> postingListEntry : postingList.entrySet()) {
//                int pageId  = postingListEntry.getKey();
//                Posting posting = postingListEntry.getValue();
//
//                int maxTermFreq = pageMaxTermFreq.get(pageId);
//                int termFreq = posting.wordPositions.size();
//
//                double termWeight = calculateTermWeight(termFreq, numOfDocuments, docFreq, maxTermFreq);
//                System.out.println(">>>>  "+wordString);
//                System.out.println(">>>>  termFreq "+termFreq+" numOfDocuments "+numOfDocuments+" docFreq "+docFreq+" maxTermFreq "+maxTermFreq +">>> "+termWeight);
//
//                Map<String, Double> termVector = pageTermVector.get(pageId);
//                if (termVector == null) {
//                    termVector = new HashMap<>();
//                }
//                termVector.put(wordString, termWeight);
//
//                pageTermVector.put(pageId, termVector);
//            };
//
//        }
//        //
//        System.out.println("___pageTermVector");
//        pageTermVector.forEach((k,v) -> {
//            System.out.println(k+": ");
//            v.forEach((x,y)->{
//                System.out.println(x+": "+y);
//            });
//        });
//        //
//
//        return pageTermVector;
//    }
//
//    private int mostFrequent(Vector<Integer> arr) {
//        Map<Integer, Integer> frequencyMap = new HashMap<>();
//
//        for (int i = 0; i < arr.size(); i++) {
//            int num = arr.get(i);
//            if (frequencyMap.containsKey(num)) {
//                frequencyMap.put(num, frequencyMap.get(num) + 1);
//            } else {
//                frequencyMap.put(num, 1);
//            }
//        }
//
//        int maxFrequency = 0;
//        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
//            if (entry.getValue() > maxFrequency) {
//                maxFrequency = entry.getValue();
//            }
//        }
//
//        return maxFrequency;
//    }
//
//    private double calculateTermWeight(int termFreq, int numOfDocuments, int docFreq, int maxTermFreq) {
//        double inverseDocFreq = Math.log(1.0 * numOfDocuments / docFreq) / Math.log(2.);
//        return termFreq * inverseDocFreq / maxTermFreq;
//    }

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
