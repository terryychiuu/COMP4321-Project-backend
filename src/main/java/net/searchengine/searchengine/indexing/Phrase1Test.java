package net.searchengine.searchengine.indexing;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Vector;

import static net.searchengine.searchengine.uitil.Constants.DATABASE_PATH;
import static net.searchengine.searchengine.uitil.Constants.RESULT_PATH;

public class Phrase1Test {
    public static void main(String[] args) throws IOException {
        System.out.println("Result file is in " + RESULT_PATH);
        PrintStream out = new PrintStream(new FileOutputStream(RESULT_PATH));
        System.setOut(out);

        try {
            RecordManager recman = RecordManagerFactory.createRecordManager(DATABASE_PATH);

            MappingIndex urlMappingIndex = new MappingIndex(recman, "urlMappingIndex");
            MappingIndex wordMappingIndex = new MappingIndex(recman, "wordMappingIndex");

            ForwardIndex pageWordsForwardIndex = new ForwardIndex(recman, "pageWordsForwardIndex");
            ForwardIndex pageTitleForwardIndex = new ForwardIndex(recman, "pageTitleForwardIndex");

            PageProperties urlPageProperties = new PageProperties(recman, "urlPageProperties");
            ForwardIndex parentChildPageForwardIndex = new ForwardIndex(recman, "parentChildPageForwardIndex");

            InvertedIndex wordInvertedIndex = new InvertedIndex(recman, "wordInvertedIndex");

            Vector<Integer> pageIds = urlPageProperties.getAllPageIds();
            for (int pageId : pageIds) {
                Properties props = urlPageProperties.getProperties(pageId);
                if(props == null) continue;
                System.out.println(props.getTitle());
                System.out.println(urlMappingIndex.getKey(pageId));
                System.out.println(props.getLastModificationDate() + " " + props.getSize());

                Map<Integer, Integer> wordFreqMap = pageWordsForwardIndex.getPageWordFrequency(pageId, wordInvertedIndex);
                StringBuilder wordsStringBuilder = new StringBuilder();
                for (int wordId : wordFreqMap.keySet()) {
                    wordsStringBuilder.append(wordMappingIndex.getKey(wordId));
                    wordsStringBuilder.append('=');
                    wordsStringBuilder.append(wordFreqMap.get(wordId));
                    wordsStringBuilder.append("; ");
                }
                System.out.println(wordsStringBuilder);

                Vector<Integer> childPages = parentChildPageForwardIndex.getValues(pageId);
                int i = 0;
                for (int childPage : childPages) {
                    if (!(i < 10) ) break;
                    System.out.println(urlMappingIndex.getKey(childPage));
                    i++;
                }
                System.out.println("----------------------------------------");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
