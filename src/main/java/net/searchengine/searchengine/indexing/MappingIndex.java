package net.searchengine.searchengine.indexing;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * URL <-> PageID
 * Word <-> WordID
 */
public class MappingIndex {
    private RecordManager recman;
    private HTree idKeyHashtable;
    private HTree keyIdHashtable;

    private String tableName;

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(1);

    public MappingIndex(RecordManager recman, String objectname) throws IOException {
        this.recman = recman;
        long idRecid = recman.getNamedObject(objectname+"IdKey");
        long keyRecid = recman.getNamedObject(objectname+"KeyId");

        if (idRecid != 0) {
            idKeyHashtable = HTree.load(recman, idRecid);
            keyIdHashtable = HTree.load(recman, keyRecid);
        }
        else {
            idKeyHashtable = HTree.createInstance(recman);
            keyIdHashtable = HTree.createInstance(recman);
            recman.setNamedObject( objectname+"IdKey", idKeyHashtable.getRecid() );
            recman.setNamedObject( objectname+"KeyId", keyIdHashtable.getRecid() );
        }
        tableName = objectname;
    }

    public int addEntry(String key) throws IOException {
        if (keyIdHashtable.get(key) != null) return -1;

        int id = ATOMIC_INTEGER.getAndIncrement();
        idKeyHashtable.put(id, key);
        keyIdHashtable.put(key, id);
        return id;
    }

//    public void delEntry(String word) throws IOException {
//        hashtable.remove(word);
//    }

    public int getId(String key) throws IOException {
        Integer id = (Integer) keyIdHashtable.get(key);
        return id != null ? id : -1;
    }

    public String getKey(int id) throws IOException {
        return (String) idKeyHashtable.get(id);
    }

//    public void finalize() throws IOException
//    {
//        recman.commit();
////        recman.close();
//    }

    public void printAll() throws IOException {
        FastIterator iter = idKeyHashtable.keys();
        Integer id;
        while( (id = (Integer) iter.next())!=null) {
            System.out.println("^^^ " + tableName + " " + id + " : " + idKeyHashtable.get(id));
        }
//        FastIterator iter2 = keyIdHashtable.keys();
//        String key;
//        while( (key = (String) iter2.next())!=null) {
//            System.out.println("^^^ Mapping "+key + " : " + keyIdHashtable.get(key));
//        }
    }
}
