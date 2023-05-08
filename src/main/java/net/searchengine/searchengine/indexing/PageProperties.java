package net.searchengine.searchengine.indexing;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

public class PageProperties {
    private RecordManager recman;
    private HTree hashtable;

    private String tableName;

    public PageProperties(RecordManager recordmanager, String objectname) throws IOException {
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

    public int addEntry(int pageId, String title, String URL, String lastModificationDate, int size) throws IOException {
        if(!isContains(pageId)) {
            Properties p = new Properties(title, URL, lastModificationDate, size);
            hashtable.put(pageId, p);
            return pageId;
        }
        return -1;
    }

    public boolean isContains(int pageId) throws IOException {
        return (hashtable.get(pageId) != null);
    }

    public Properties getProperties(int pageId) throws IOException {
        return (Properties) hashtable.get(pageId);
    }

    public Vector<Integer> getAllPageIds() throws IOException {
        Vector<Integer> pageIds = new Vector<>();
        FastIterator iter = hashtable.keys();
        Integer pageId;
        while( (pageId = (Integer)iter.next())!=null) {
            pageIds.add(pageId);
        }
        return pageIds;
    }

//    public void finalize() throws IOException
//    {
//        recman.commit();
////        recman.close();
//    }

    public void printAll() throws IOException {
        FastIterator iter = hashtable.keys();
        Integer pageId;
        while( (pageId = (Integer)iter.next())!=null) {
            System.out.printf("^^^ " + tableName + " %s : %s\n" , pageId, hashtable.get(pageId));
        }
    }

}
