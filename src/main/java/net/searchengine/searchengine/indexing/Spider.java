package net.searchengine.searchengine.indexing;

import net.searchengine.searchengine.nlp.NLPUtils;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Spider {
    private final String rootUrl;
    private String databasePath;

    private final HashSet<String> doneUrls;     // the set of urls that have been visited before
    private Queue<String> tasks;

    private final int maxPages; // max page
//    private final int maxDepth;

    Spider (String rootUrl, int maxPages, int maxDepth) {
        this.rootUrl = rootUrl;
        this.doneUrls = new HashSet<>();

        this.tasks = new LinkedList();
        this.tasks.add(rootUrl);

        this.maxPages = maxPages;
//        this.maxDepth = maxDepth;
    }

//    public static HashSet<String> visit (String s) {
//        System.out.println("Visiting " + s);
//        HashSet<String> strings = new HashSet<>();
//        try {
//            Parser parser = new Parser(s);
//            NodeList list = parser.parse(new LinkStringFilter(""));
//            for (int i=0; i<list.size(); i++) {
//                String st = ((LinkTag)(list.elementAt(i))).extractLink();
//                strings.add(st);
//            }
//            return strings;
//        } catch (ParserException e) {
////            e.printStackTrace();
//            return new HashSet<>();
//        }
//    }
//
//    public static HashSet<String> visit (String url, int depth) {
//        HashSet<String> strings = new HashSet<>();
//        if (depth == 0) {
//            strings.add(url);
//            return strings;
//        }
//        else {
//            HashSet<String> strings1 = visit(url);
//            for (String string:strings1) strings.addAll(visit(string,depth-1));
//        }
//        strings.add(url);
//        return strings;
//    }

    public void visit() {

        if (tasks.isEmpty() || !(doneUrls.size() < maxPages)) return;

        String url = tasks.poll();
        System.out.println("fetching: " + url);

        System.out.println( " Tasks : " + tasks.size());

        try {
            Crawler crawler = new Crawler(url);
            Indexer indexer = new Indexer(url);

            String lastModificationDate = crawler.getLastModificationDate();

            System.out.println("Last Modified: " + lastModificationDate);

            Vector<String> links = crawler.extractLinks();

            for (int i = 0; i < links.size(); i++) {
                tasks.add(links.elementAt(i));
            }

            if (indexer.isPageOutdated(lastModificationDate)) {
                Vector<String> words = crawler.extractWords().stream()
                        .filter(NLPUtils::stopwordFilter)
                        .map(NLPUtils::stemFilter)
//                        .filter(NLPUtils::isAlphaNumeric)
                        .collect(Collectors.toCollection(Vector::new));



                List title = crawler.extractTitle();    // return [String, Vector<String>]
                if (title != null) {
                    String strTitle = (String) title.get(0);
                    Vector<String> tokenizedTitle = ((Vector<String>) title.get(1)).stream()
//                            .filter(NLPUtils::isAlphaNumeric)
                            .filter(NLPUtils::stopwordFilter)
                            .map(NLPUtils::stemFilter)
                            .collect(Collectors.toCollection(Vector::new));
                    System.out.println("Title: " + strTitle);
                    System.out.println("Title: " + tokenizedTitle);


                    int pageSize = crawler.getPageSize();
                    indexer.insertPageProperty(strTitle, lastModificationDate, pageSize);
                    System.out.println("pageSize: "+pageSize);

                    indexer.insertTokenizedTitle(tokenizedTitle);
                    indexer.insertWords(words);

                    for (String link : links) {
                        indexer.insertParentChildPageForwardIndex(link);
                        indexer.insertChildParentPageForwardIndex(link);
                    }

//                    indexer.printAllPageProperties();

//                    System.out.println("Words \t" + words);
//                    System.out.println("Links \t" + links);

//                    indexer.printAllWordsMappingIndex();
//                    indexer.printAllWordsInvertedIndex();
//                    indexer.printParentPageForwardIndex();
                }


            } else
                System.out.println("Database already store the updated one for: "+ url);

            if(!doneUrls.contains(url)) {
                doneUrls.add(url);
                System.out.println(doneUrls.size());
                System.out.println("=========");
            }

            indexer.finalize();

            if (tasks.peek() != null) {
                visit();
            }

        } catch (ParserException e) {
//            e.printStackTrace();
            System.out.println("Cannot access: " + url + "\nError: " + e + "\n");
            tasks.remove();
            if (tasks.peek() != null) {
                visit();
            }
        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("IO exxxxxxx");
//
            tasks.remove();
            if (tasks.peek() != null) {
                visit();
            }
        }

    }

}
