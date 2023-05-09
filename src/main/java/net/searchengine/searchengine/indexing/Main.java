package net.searchengine.searchengine.indexing;

import java.io.IOException;
import java.util.Map;

public class Main {

    public static void main (String[] args) throws IOException {
        String link0 = "http://www.cse.ust.hk";
        String link1 = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
        String link3 = "https://www.cse.ust.hk/~kwtleung/COMP4321/Movie.htm";

        Spider spider = new Spider(link1, 300, 1);
        spider.visit();

        Indexer indexer = new Indexer();
        indexer.insertPageSimilarityForwardIndex();

        indexer.finalize();

    }
}
