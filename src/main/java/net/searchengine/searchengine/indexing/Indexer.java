package net.searchengine.searchengine.indexing;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static net.searchengine.searchengine.uitil.Constants.DATABASE_PATH;

public class Indexer {
    private RecordManager recman;
    private String url;
    private Integer pageId;

    private MappingIndex urlMappingIndex;           // url <-> pageId
    private MappingIndex wordMappingIndex;          // word <-> wordId

    private ForwardIndex pageWordsForwardIndex;         // pageId -> wordIds[]
    private ForwardIndex pageTitleForwardIndex;         // pageId -> wordIds[]
    private PageProperties urlPageProperties;        // pageId -> Properties{}
    private InvertedIndex wordInvertedIndex;         // wordId -> PostingList
    private InvertedIndex titleInvertedIndex;        // wordId -> PostingList

    private ForwardIndex parentChildPageForwardIndex;       // pageId -> pageIds[]
    private ForwardIndex childParentPageForwardIndex;       // pageId -> pageIds[]

    private ForwardIndex pageTitleSimilarityForwardIndex;   // pageId -> similarity
    private ForwardIndex pageWordSimilarityForwardIndex;    // pageId -> similarity

//    private static

    public Indexer() throws IOException {
        recman = RecordManagerFactory.createRecordManager(DATABASE_PATH);

        urlMappingIndex = new MappingIndex(recman, "urlMappingIndex");

        wordMappingIndex = new MappingIndex(recman, "wordMappingIndex");
        pageWordsForwardIndex = new ForwardIndex(recman, "pageWordsForwardIndex");
        pageTitleForwardIndex = new ForwardIndex(recman, "pageTitleForwardIndex");
        urlPageProperties = new PageProperties(recman, "urlPageProperties");
        wordInvertedIndex = new InvertedIndex(recman, "wordInvertedIndex");
        titleInvertedIndex = new InvertedIndex(recman, "titleInvertedIndex");

        parentChildPageForwardIndex = new ForwardIndex(recman, "parentChildPageForwardIndex");
        childParentPageForwardIndex = new ForwardIndex(recman, "childParentPageForwardIndex");

        pageTitleSimilarityForwardIndex = new ForwardIndex(recman, "pageTitleSimilarityForwardIndex");
        pageWordSimilarityForwardIndex = new ForwardIndex(recman, "pageWordSimilarityForwardIndex");
    }

    public Indexer(String url) throws IOException {
        this();
        this.url = url;

        urlMappingIndex.addEntry(url);
        this.pageId = urlMappingIndex.getId(url);

    }

    public void insertWords(Vector<String> words) throws IOException {
        pageWordsForwardIndex.clear(pageId);

        if (words.isEmpty()) return;

        Vector<String> biGram = ToNGrams(words, 2);
        Vector<String> triGram = ToNGrams(words, 3);

        insertWordsHelper(words);
        insertWordsHelper(biGram);
        insertWordsHelper(triGram);
    }

    private void insertWordsHelper(Vector<String> words) throws IOException {
        for(int i=0; i<words.size(); i++) {
            // Mapping 21 : You
            String w = words.get(i);
            if(w == null || w.length() <= 0 || w.equals("")) continue;
            wordMappingIndex.addEntry(w);
            int wordId = wordMappingIndex.getId(w);
            if (wordId != -1) {
                // Forward 37 [18, 16, 41, 17, 18, 16, 41, 46, 15, 48, 15, 50, 51, 52]
                pageWordsForwardIndex.addEntry(pageId, wordId);
                wordInvertedIndex.addEntry(wordId, pageId, i);
            }
        }
    }

    public void insertTokenizedTitle(Vector<String> tokenizedTitle) throws IOException {
        pageTitleForwardIndex.clear(pageId);

        if (tokenizedTitle.isEmpty()) return;

        Vector<String> biGram = ToNGrams(tokenizedTitle, 2);
        Vector<String> triGram = ToNGrams(tokenizedTitle, 3);

        insertTokenizedTitleHelper(tokenizedTitle);
        insertTokenizedTitleHelper(biGram);
        insertTokenizedTitleHelper(triGram);
    }

    private void insertTokenizedTitleHelper(Vector<String> tokenizedTitle) throws IOException {
        for(int i=0; i<tokenizedTitle.size(); i++) {
            String t = tokenizedTitle.get(i);
            if(t == null || t.length() <= 0 || t.equals("")) continue;
            wordMappingIndex.addEntry(t);
            int wordId = wordMappingIndex.getId(t);
            if (wordId != -1) {
                pageTitleForwardIndex.addEntry(pageId, wordId);
                titleInvertedIndex.addEntry(wordId, pageId, i);
            }
        }
    }

    public static Vector<String> ToNGrams(Vector<String> words, int n) {
        Vector<String> nGrams = new Vector<>();
        if (words.size () == 0)
            return nGrams;
        // Generate bigram sequence
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size() - n + 1; i++) {
            for (int j = 0; j < n - 1; j++) {
                sb.append(words.get(i + j));
                sb.append(" ");
            }
            sb.append(words.get(i + n - 1));

            nGrams.add(sb.toString());
            sb.setLength(0);
        }
        return nGrams;
    }

    public void insertPageProperty(String title, String lastModificationDate, int size) throws IOException {
        urlPageProperties.addEntry(pageId, title, url, lastModificationDate, size);
    }

    public void insertParentChildPageForwardIndex(String childPageUrl) throws IOException {
        int childPageId = urlMappingIndex.getId(childPageUrl);
        if (childPageId == -1) {
            urlMappingIndex.addEntry(childPageUrl);
            childPageId = urlMappingIndex.getId(childPageUrl);
        }
        parentChildPageForwardIndex.uniqueAddEntry(pageId, childPageId);
    }

    public void insertChildParentPageForwardIndex(String childPageUrl) throws IOException {
        int childPageId = urlMappingIndex.getId(childPageUrl);
        if (childPageId == -1) {
            urlMappingIndex.addEntry(childPageUrl);
            childPageId = urlMappingIndex.getId(childPageUrl);
        }
        childParentPageForwardIndex.uniqueAddEntry(childPageId, pageId);
    }

    public boolean isPageOutdated(String lastModificationDate) throws IOException {
        Properties properties = urlPageProperties.getProperties(pageId);
        if(properties == null) return true;

        if (lastModificationDate.equals(properties.getLastModificationDate()))
            return false;
        else return true;
    }

    public void insertPageSimilarityForwardIndex() throws IOException {
        Map<Integer, Map<String, Double>> pageTitleTermVector = calculatePageTermVector(true);
        Map<Integer, Map<String, Double>> pageWordTermVector = calculatePageTermVector(false);

        for (Map.Entry<Integer, Map<String, Double>> entry : pageTitleTermVector.entrySet()) {
            pageTitleSimilarityForwardIndex.addPageTermVector(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, Map<String, Double>> entry : pageWordTermVector.entrySet()) {
            pageWordSimilarityForwardIndex.addPageTermVector(entry.getKey(), entry.getValue());
        }
    }

    public Map<Integer, Map<String, Double>> calculatePageTermVector(boolean isTitle) throws IOException {
        Map<Integer, Map<String, Double>> pageTermVector = new HashMap<>(); // page -> (word -> termWeight)[]

        InvertedIndex invertedIndex;
        if (isTitle) invertedIndex = this.titleInvertedIndex;
        else invertedIndex = this.wordInvertedIndex;

        ForwardIndex forwardIndex;
        if (isTitle) forwardIndex = this.pageTitleForwardIndex;
        else forwardIndex = this.pageWordsForwardIndex;



        int numOfDocuments = this.urlPageProperties.getAllPageIds().size();

        Map<Integer, Integer> pageMaxTermFreq = new HashMap<>();
        for (Map.Entry<Integer, Vector<Integer>> entry : forwardIndex.getIdListForwardIndex().entrySet()){
            int m = mostFrequent(entry.getValue());
            pageMaxTermFreq.put(entry.getKey(), m);
        }

        for (Map.Entry<Integer, HashMap<Integer, Posting>> entry : invertedIndex.getInvertedIndex().entrySet()) {
            Integer wordId = entry.getKey();
            HashMap<Integer, Posting> postingList = entry.getValue();
//            System.out.println(wordId);

            String wordString = wordMappingIndex.getKey(wordId);

            int docFreq = postingList.entrySet().size();

            for (Map.Entry<Integer, Posting> postingListEntry : postingList.entrySet()) {
                int pageId  = postingListEntry.getKey();
                Posting posting = postingListEntry.getValue();

                int maxTermFreq = pageMaxTermFreq.get(pageId);
                int termFreq = posting.wordPositions.size();

                double termWeight = calculateTermWeight(termFreq, numOfDocuments, docFreq, maxTermFreq);
//                System.out.println(">>>>  "+wordString);
//                System.out.println(">>>>  termFreq "+termFreq+" numOfDocuments "+numOfDocuments+" docFreq "+docFreq+" maxTermFreq "+maxTermFreq +">>> "+termWeight);

                Map<String, Double> termVector = pageTermVector.get(pageId);
                if (termVector == null) {
                    termVector = new HashMap<>();
                }
                termVector.put(wordString, termWeight);

                pageTermVector.put(pageId, termVector);
            };
        }

        //
//        System.out.println("___pageTermVector");
//        pageTermVector.forEach((k,v) -> {
//            System.out.println(k+": ");
//            v.forEach((x,y)->{
//                System.out.println(x+": "+y);
//            });
//        });
        //

        return pageTermVector;
    }

    private int mostFrequent(Vector<Integer> arr) {
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        for (int i = 0; i < arr.size(); i++) {
            int num = arr.get(i);
            if (frequencyMap.containsKey(num)) {
                frequencyMap.put(num, frequencyMap.get(num) + 1);
            } else {
                frequencyMap.put(num, 1);
            }
        }

        int maxFrequency = 0;
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxFrequency) {
                maxFrequency = entry.getValue();
            }
        }

        return maxFrequency;
    }

    private double calculateTermWeight(int termFreq, int numOfDocuments, int docFreq, int maxTermFreq) {
        double inverseDocFreq = Math.log(1.0 * numOfDocuments / docFreq) / Math.log(2.);
        return termFreq * inverseDocFreq / maxTermFreq;
    }


    // getter
    public MappingIndex getUrlMappingIndex() {
        return urlMappingIndex;
    }

    public MappingIndex getWordMappingIndex() {
        return wordMappingIndex;
    }

    public ForwardIndex getPageWordsForwardIndex() {
        return pageWordsForwardIndex;
    }

    public ForwardIndex getPageTitleForwardIndex() {
        return pageTitleForwardIndex;
    }

    public PageProperties getUrlPageProperties() {
        return urlPageProperties;
    }

    public InvertedIndex getWordInvertedIndex() {
        return wordInvertedIndex;
    }

    public InvertedIndex getTitleInvertedIndex() {
        return titleInvertedIndex;
    }

    public ForwardIndex getParentChildPageForwardIndex() {
        return parentChildPageForwardIndex;
    }

    public ForwardIndex getChildParentPageForwardIndex() {
        return childParentPageForwardIndex;
    }

    public ForwardIndex getPageTitleSimilarityForwardIndex() {
        return pageTitleSimilarityForwardIndex;
    }

    public ForwardIndex getPageWordSimilarityForwardIndex() {
        return pageWordSimilarityForwardIndex;
    }

    // print
    public void printAllUrlsMappingIndex() throws IOException {
        urlMappingIndex.printAll();
    }

    public void printAllWordsMappingIndex() throws IOException {
        wordMappingIndex.printAll();
        pageWordsForwardIndex.printAll(pageId);
    }

    public void printAllWordsInvertedIndex() throws IOException {
        wordInvertedIndex.printAll();
    }

    public void printAllPageProperties() throws IOException {
        urlPageProperties.printAll();
    }

    public void printParentPageForwardIndex() throws IOException {
        parentChildPageForwardIndex.printAll(pageId);
    }

    public void finalize() throws IOException {
        recman.commit();
        recman.close();
    }
}
