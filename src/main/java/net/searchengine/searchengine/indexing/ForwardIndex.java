package net.searchengine.searchengine.indexing;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.*;

public class ForwardIndex {
    private RecordManager recman;
    private HTree hashtable;

    private String tableName;

    public ForwardIndex(RecordManager recman, String objectname) throws IOException {
        this.recman = recman;
        long idRecid = recman.getNamedObject(objectname);
//        long keyRecid = recman.getNamedObject(objectname+"KeyId");

        if (idRecid != 0) {
            hashtable = HTree.load(recman, idRecid);
        }
        else {
            hashtable = HTree.createInstance(recman);
            recman.setNamedObject(objectname, hashtable.getRecid() );
        }
        tableName = objectname;
    }

    // pageID -> wordPosList
    public void addEntry(int pageId, int wordID) throws IOException {
        Vector<Integer> wordIds = (Vector<Integer>) hashtable.get(pageId);

        if (wordIds == null) {
            wordIds = new Vector<>();

        }
        wordIds.add(wordID);
        hashtable.put(pageId, wordIds);
    }

    public void uniqueAddEntry(int pageId, int id) throws IOException {
        Vector<Integer> wordIds = (Vector<Integer>) hashtable.get(pageId);

        if (wordIds == null) {
            wordIds = new Vector<>();
            wordIds.add(id);
        }
        else {
            if (!wordIds.contains(id))
                wordIds.add(id);
        }
        hashtable.put(pageId, wordIds);
    }

    public Vector<Integer> getValues(int pageId) throws IOException {
        return (Vector<Integer>) hashtable.get(pageId);
    }

    public Map<Integer, Integer> getPageWordFrequency(int pageId, InvertedIndex invertedIndex) throws IOException {
        Vector<Integer> wordIds = (Vector<Integer>) hashtable.get(pageId);
        if (wordIds == null) return null;

        Set<Integer> wordIdsSet = new HashSet<>(wordIds);

        Map<Integer, Integer> termFreqMap = new HashMap<>();
        for (Integer wordId : wordIdsSet) {
            HashMap postingList = invertedIndex.getPostingList(wordId);
            int freq =  ((Posting) postingList.get(pageId)).wordPositions.size();
            termFreqMap.put(wordId, freq);
        }

        return termFreqMap;
    }

    // another type of forward table
    public void addPageTermVector(int pageId, Map<String, Double> termVector) throws IOException {
//        System.out.println("______ "+pageId);
//        termVector.forEach((x,y)->{
//            System.out.println(x+": "+y);
//        });
        hashtable.put(pageId, termVector);
    }

    public HashMap<Integer, Vector<Integer>> getIdListForwardIndex() throws IOException {
        HashMap<Integer, Vector<Integer>> forwardIndex = new HashMap<>();
        FastIterator iter = hashtable.keys();
        Integer pageId;
        while( (pageId = (Integer)iter.next())!=null) {
            Vector<Integer> list = (Vector<Integer>) hashtable.get(pageId);
            forwardIndex.put(pageId, list);
        }
        return forwardIndex;
    }

    public Map<Integer, Map<String, Double>> getPageTermVectorForwardIndex() throws IOException {
        Map<Integer, Map<String, Double>> pageTermVector = new HashMap<>();
        FastIterator iter = hashtable.keys();
        Integer pageId;
        while( (pageId = (Integer)iter.next())!=null) {
            Map<String, Double> termVector = (Map<String, Double>) hashtable.get(pageId);
            pageTermVector.put(pageId, termVector);
        }

//        System.out.println("HIHIHI getPageTermVectorForwardIndex " +hashtable.keys());
//        pageTermVector.forEach((k,v) -> {
//            System.out.println(k+": ");
//            v.forEach((x,y)->{
//                System.out.println(x+": "+y);
//            });
//        });
        return pageTermVector;
    }



    public void delEntry(String word) throws IOException {
        hashtable.remove(word);
    }

    public void clear(int pageId) throws IOException {
        hashtable.remove(pageId);
    }

    public void finalize() throws IOException
    {
        recman.commit();
//        recman.close();
    }

    public void printAll(int pageId) throws IOException {
        Vector<Integer> values = (Vector<Integer>) hashtable.get(pageId);
        System.out.println("^^^ " + tableName + " " + pageId + " " + values);

//        FastIterator iter2 = keyIdHashtable.keys();
//        String key;
//        while( (key = (String) iter2.next())!=null) {
//            System.out.println("^^^ Mapping "+key + " : " + keyIdHashtable.get(key));
//        }
    }
}
