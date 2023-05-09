package net.searchengine.searchengine.searching;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import net.searchengine.searchengine.indexing.*;
import net.searchengine.searchengine.uitil.MapUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static net.searchengine.searchengine.uitil.Constants.DATABASE_PATH;
import static net.searchengine.searchengine.uitil.Constants.PHRASE_2_RESULT_PATH;

public class Phrase2Test {
    public static void main(String[] args) throws IOException {
        System.out.println("Result file is in " + PHRASE_2_RESULT_PATH);
        PrintStream out = new PrintStream(new FileOutputStream(PHRASE_2_RESULT_PATH));
        System.setOut(out);

        try {
            RecordManager recman = RecordManagerFactory.createRecordManager(DATABASE_PATH);

            MappingIndex urlMappingIndex = new MappingIndex(recman, "urlMappingIndex");
            MappingIndex wordMappingIndex = new MappingIndex(recman, "wordMappingIndex");

            ForwardIndex pageWordsForwardIndex = new ForwardIndex(recman, "pageWordsForwardIndex");
            ForwardIndex pageTitleForwardIndex = new ForwardIndex(recman, "pageTitleForwardIndex");

            PageProperties urlPageProperties = new PageProperties(recman, "urlPageProperties");

            ForwardIndex parentChildPageForwardIndex = new ForwardIndex(recman, "parentChildPageForwardIndex");
            ForwardIndex childParentPageForwardIndex = new ForwardIndex(recman, "childParentPageForwardIndex");

            InvertedIndex wordInvertedIndex = new InvertedIndex(recman, "wordInvertedIndex");
            InvertedIndex titleInvertedIndex = new InvertedIndex(recman, "titleInvertedIndex");

            Vector<Integer> pageIds = urlPageProperties.getAllPageIds();
            for (int pageId : pageIds) {
                Properties props = urlPageProperties.getProperties(pageId);
                if(props == null) continue;
                System.out.println(props.getTitle());
                System.out.println(urlMappingIndex.getKey(pageId));
                System.out.println(props.getLastModificationDate() + " " + props.getSize());

                List<Integer> titleTerms = new ArrayList<>(pageTitleForwardIndex.getValues(pageId));
                List<Integer> wordTerms = new ArrayList<>(pageWordsForwardIndex.getValues(pageId));
                Map<Integer, Integer> titleFreqMap = MapUtils.getFrequencyMap(titleTerms);
                Map<Integer, Integer> wordFreqMap = MapUtils.getFrequencyMap(wordTerms);

                StringBuilder titleStringBuilder = new StringBuilder();
                for (int wordId : titleFreqMap.keySet()) {
                    titleStringBuilder.append(wordMappingIndex.getKey(wordId));
                    titleStringBuilder.append('=');
                    titleStringBuilder.append(titleFreqMap.get(wordId));
                    titleStringBuilder.append("; ");
                }
                System.out.println(titleStringBuilder);

                StringBuilder stringBuilder = new StringBuilder();
                for (int wordId : wordFreqMap.keySet()) {
                    stringBuilder.append(wordMappingIndex.getKey(wordId));
                    stringBuilder.append('=');
                    stringBuilder.append(wordFreqMap.get(wordId));
                    stringBuilder.append("; ");
                }
                System.out.println(stringBuilder);

                Vector<Integer> childPages = parentChildPageForwardIndex.getValues(pageId);
                if (childPages == null) childPages = new Vector<>();
                System.out.println("Child Link:");
                int i = 0;
                for (int childPage : childPages) {
                    if (!(i < 10) ) break;
                    System.out.println(urlMappingIndex.getKey(childPage));
                    i++;
                }

                Vector<Integer> parentPages = childParentPageForwardIndex.getValues(pageId);
                if (parentPages == null) parentPages = new Vector<>();
                System.out.println("Parent Link:");
                i = 0;
                for (int parentPage : parentPages) {
                    if (!(i < 10) ) break;
                    System.out.println(urlMappingIndex.getKey(parentPage));
                    i++;
                }
                System.out.println("----------------------------------------");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
