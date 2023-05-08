package net.searchengine.searchengine.indexing;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InvertedIndex {
	private RecordManager recman;
	private HTree hashtable;

	private String tableName;

	public InvertedIndex(RecordManager recordmanager, String objectname) throws IOException {
		recman = recordmanager;
		long recid = recman.getNamedObject(objectname);

		if (recid != 0)
			hashtable = HTree.load(recman, recid);
		else {
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject(objectname, hashtable.getRecid());
		}
		tableName = objectname;
	}

	public void addEntry(int wordId, int pageId, int wordPosition) throws IOException {

		HashMap<Integer, Posting> postingList = (HashMap<Integer, Posting>) hashtable.get(wordId);
		if (postingList == null) {
			postingList = new HashMap<>();
			Posting p = new Posting(pageId);
			p.wordPositions.add(wordPosition);
			postingList.put(pageId, p);

		}
		else {
			Posting posting = postingList.get(pageId);
			if (posting == null) {
				Posting p = new Posting(pageId);
				p.wordPositions.add(wordPosition);
				postingList.put(pageId, p);
			}
			else {
				posting.wordPositions.add(wordPosition);
			}
		}

		hashtable.put(wordId, postingList);

	}

	public HashMap<Integer, Posting> getPostingList(int wordId) throws IOException {
		return (HashMap<Integer, Posting>) hashtable.get(wordId);
	}

	public HashMap<Integer, HashMap<Integer, Posting>> getInvertedIndex() throws IOException {
		HashMap<Integer, HashMap<Integer, Posting>> invertedIndex = new HashMap<>();
		FastIterator iter = hashtable.keys();
		Integer pageId;
		while( (pageId = (Integer)iter.next())!=null) {
			HashMap<Integer, Posting> PostingList = (HashMap<Integer, Posting>) hashtable.get(pageId);
			invertedIndex.put(pageId, PostingList);
		}
		return invertedIndex;
	}

	public int getMaxTermFreq() throws IOException {
		int maxTermFreq = 0;
		FastIterator iter = hashtable.keys();
		Integer key;
		while ((key = (Integer)iter.next())!=null) {
			HashMap<Integer, Posting> postingList = (HashMap<Integer, Posting>) hashtable.get(key);
			for (Map.Entry<Integer, Posting> postingEntry : postingList.entrySet()) {
				Posting p = postingEntry.getValue();
				int termFreq = p != null ? p.wordPositions.size() : 0;
				maxTermFreq = termFreq > maxTermFreq ? termFreq : maxTermFreq;
			}
		}
		return maxTermFreq;
	}

	public void delEntry(int wordId) throws IOException {
		hashtable.remove(wordId);
	}

//	public void finalize() throws IOException
//	{
//		recman.commit();
////        recman.close();
//	}

	public void printAll() throws IOException {
		FastIterator iter = hashtable.keys();
		Integer key;
		while ((key = (Integer)iter.next())!=null) {
			System.out.println("^^^ " + tableName + " " + key);
			HashMap<Integer, Posting> postingList = (HashMap<Integer, Posting>) hashtable.get(key);
			for (Map.Entry<Integer, Posting> postingEntry : postingList.entrySet()) {
				System.out.println("^^^ ^^^ " + postingEntry.getKey() + " " + postingEntry.getValue());
			}
		}
	}
}
