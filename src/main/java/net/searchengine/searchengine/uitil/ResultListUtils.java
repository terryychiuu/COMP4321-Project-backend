package net.searchengine.searchengine.uitil;

import net.searchengine.searchengine.database.model.WebPage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The ResultListUtils class provide function for managing the search results
 */
public class ResultListUtils {

    public static void addWebPageToListInOrder(List<WebPage> personList, WebPage webPage) {
        Comparator<WebPage> bySimilarity = Comparator.comparing(WebPage::getSimilarity).reversed();
        int index = Collections.binarySearch(personList, webPage, bySimilarity);

        if (index < 0) {
            personList.add(-index - 1, webPage);
        } else {
            personList.add(index, webPage);
        }
    }
}
