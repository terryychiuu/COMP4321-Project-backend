package net.searchengine.searchengine.controller;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import net.searchengine.searchengine.database.model.QueryResult;
import net.searchengine.searchengine.database.model.WebPage;
import net.searchengine.searchengine.indexing.*;
import net.searchengine.searchengine.database.model.Query;
import net.searchengine.searchengine.indexing.Properties;
import net.searchengine.searchengine.searching.SearchEngine;
import net.searchengine.searchengine.uitil.MapUtils;
import net.searchengine.searchengine.uitil.ResultListUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.searchengine.searchengine.uitil.Constants.DATABASE_PATH;
import static net.searchengine.searchengine.uitil.Constants.FRONTEND_PATH;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = FRONTEND_PATH)
public class WebPagesController {
    @GetMapping("/documents")
    public ResponseEntity<Vector<Integer>> allWebPages() {
        try {
            RecordManager recman = RecordManagerFactory.createRecordManager(DATABASE_PATH);

            MappingIndex urlMappingIndex = new MappingIndex(recman, "urlMappingIndex");
            MappingIndex wordMappingIndex = new MappingIndex(recman, "wordMappingIndex");

            ForwardIndex pageWordsForwardIndex = new ForwardIndex(recman, "pageWordsForwardIndex");

            PageProperties urlPageProperties = new PageProperties(recman, "urlPageProperties");
            ForwardIndex parentChildPageForwardIndex = new ForwardIndex(recman, "parentChildPageForwardIndex");

            Vector<Integer> pageIds = urlPageProperties.getAllPageIds();
            return new ResponseEntity<Vector<Integer>>(pageIds, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<Vector<Integer>>(new Vector<>(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<QueryResult> getSearchQuery(@RequestParam("q") String query, @RequestParam(name = "offset", required = false) Integer offset) throws IOException {
        long startTime = System.nanoTime();

        System.out.println("Q " + query);

        Indexer indexer = new Indexer();
        PageProperties pageProperties = indexer.getUrlPageProperties();

        ForwardIndex pageTitleForwardIndex = indexer.getPageTitleForwardIndex();
        ForwardIndex pageWordsForwardIndex = indexer.getPageWordsForwardIndex();
        MappingIndex wordMappingIndex = indexer.getWordMappingIndex();

        ForwardIndex parentChildPageForwardIndex = indexer.getParentChildPageForwardIndex();
        ForwardIndex childParentPageForwardIndex = indexer.getChildParentPageForwardIndex();
        MappingIndex urlMappingIndex = indexer.getUrlMappingIndex();

        SearchEngine searchEngine = new SearchEngine(query, indexer);

        Map<Integer, Double> pageSimilarity = searchEngine.calculatePageSimilarity(0.6);

        List<WebPage> webPages = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : pageSimilarity.entrySet()) {
            Integer pageId = entry.getKey();
            Double similarity = entry.getValue();
            if (similarity == 0) continue;

            Properties p = pageProperties.getProperties(pageId);

            List<Integer> titleTerms = new ArrayList<>(pageTitleForwardIndex.getValues(pageId));
            List<Integer> wordTerms = new ArrayList<>(pageWordsForwardIndex.getValues(pageId));

            Map<Integer, Integer> termIdFreqMap = MapUtils.getTopFrequencies(titleTerms, wordTerms);

            Map<String, Integer> termFreqMap = new HashMap<>();
            int i = 0;
            for (Map.Entry<Integer, Integer> entry1 : termIdFreqMap.entrySet()) {
//                if (i > 10) break;
                int k = entry1.getKey();
                int v = entry1.getValue();
                termFreqMap.put(wordMappingIndex.getKey(k), v);
                i++;
            };


            Vector<Integer> childPages = parentChildPageForwardIndex.getValues(pageId);
            Vector<Integer> parentPages = childParentPageForwardIndex.getValues(pageId);
            if (childPages == null) childPages = new Vector<>();
            if (parentPages == null) parentPages = new Vector<>();

            List<String> childUrls = new ArrayList<>();
            List<String> parentUrls = new ArrayList<>();

            for (int childPage : childPages) {
                childUrls.add(urlMappingIndex.getKey(childPage));
            }
            for (int parentPage : parentPages) {
                parentUrls.add(urlMappingIndex.getKey(parentPage));
            }

            String title = p.getTitle();
            String url = p.getURL();
            String lastModificationDate = p.getLastModificationDate();
            int pageSize = p.getSize();
            List<String> childLinks = childUrls;
            List<String> parentLinks = parentUrls;
            WebPage webPage = new WebPage(title, url, lastModificationDate, pageSize, termFreqMap, childLinks, parentLinks, similarity);
            ResultListUtils.addWebPageToListInOrder(webPages, webPage);
        }

        int totalResults = webPages.size();
        if (offset == null) offset = 1;
        int startIndex =(offset-1)*50;
        int endIndex =  Math.min(startIndex + 50, totalResults);
        List<WebPage> offsetWebPages = webPages.subList(startIndex, endIndex);


        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        QueryResult queryResult = new QueryResult(offsetWebPages, duration / 1000000000.0, totalResults);

        indexer.finalize();

        return new ResponseEntity<>(queryResult, HttpStatus.OK);
    }
}
