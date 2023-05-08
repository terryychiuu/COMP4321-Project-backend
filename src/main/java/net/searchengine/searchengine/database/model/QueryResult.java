package net.searchengine.searchengine.database.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class QueryResult {
    private List<WebPage> webPages;
    private double retrievalTimeInSe;
    private int totalResults;

    @JsonCreator
    public QueryResult(@JsonProperty("webPages") List<WebPage> webPages,
                       @JsonProperty("retrievalTimeInSe") double retrievalTimeInSe,
                       @JsonProperty("totalResults") int totalResults) {
        this.webPages = webPages;
        this.retrievalTimeInSe = retrievalTimeInSe;
        this.totalResults = totalResults;
    }

    public List<WebPage> getWebPages() {
        return webPages;
    }

    public void setWebPages(List<WebPage> webPages) {
        this.webPages = webPages;
    }

    public double getRetrievalTimeInSe() {
        return retrievalTimeInSe;
    }

    public void setRetrievalTimeInSe(double retrievalTimeInSe) {
        this.retrievalTimeInSe = retrievalTimeInSe;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}
