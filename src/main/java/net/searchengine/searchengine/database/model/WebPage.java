package net.searchengine.searchengine.database.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class WebPage {
    private String title = "";
    private String url = "";
    private String lastModificationDate = "";
    private int pageSize;
    private Map<String, Integer> termFreqMap;
    private List<String> childLinks;
    private List<String> parentLinks;
    private double similarity;

    @JsonCreator
    public WebPage(@JsonProperty("title") String title,
                   @JsonProperty("url") String url,
                   @JsonProperty("lastModificationDate") String lastModificationDate,
                   @JsonProperty("pageSize") int pageSize,
                   @JsonProperty("termFreqMap") Map<String, Integer> termFreqMap,
                   @JsonProperty("childLinks") List<String> childLinks,
                   @JsonProperty("parentLinks") List<String> parentLinks,
                   @JsonProperty("similarity") double similarity) {
        this.title = title;
        this.url = url;
        this.lastModificationDate = lastModificationDate;
        this.pageSize = pageSize;
        this.termFreqMap = termFreqMap;
        this.childLinks = childLinks;
        this.parentLinks = parentLinks;
        this.similarity = similarity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(String lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Map<String, Integer> getTermFreqMap() {
        return termFreqMap;
    }

    public void setTermFreqMap(Map<String, Integer> termFreqMap) {
        this.termFreqMap = termFreqMap;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<String> getChildLinks() {
        return childLinks;
    }

    public void setChildLinks(List<String> childLinks) {
        this.childLinks = childLinks;
    }

    public List<String> getParentLinks() {
        return parentLinks;
    }

    public void setParentLinks(List<String> parentLinks) {
        this.parentLinks = parentLinks;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }
}
