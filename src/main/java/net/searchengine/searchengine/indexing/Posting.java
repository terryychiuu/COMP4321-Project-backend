package net.searchengine.searchengine.indexing;

import java.io.Serializable;
import java.util.Vector;

//  wordID -> Posting: {pageID, [word position]}
public class Posting implements Serializable {
    public int pageID;
    public Vector<Integer> wordPositions;

    public Posting(int pageID) {
        this.pageID = pageID;
        wordPositions = new Vector<Integer>();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Posting{");
        sb.append("pageID=").append(pageID);
        sb.append(", wordPositions=").append(wordPositions);
        sb.append('}');
        return sb.toString();
    }
}
