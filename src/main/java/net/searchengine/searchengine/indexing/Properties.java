package net.searchengine.searchengine.indexing;

import java.io.Serializable;
import java.util.Date;

public class Properties implements Serializable {
    private String title;
    private String URL;
    private String lastModificationDate;
    private int size;

    public Properties(String title, String URL, String lastModificationDate, int size) {
        this.title = title;
        this.URL = URL;
        this.lastModificationDate = lastModificationDate;
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public String getURL() {
        return URL;
    }

    public String getLastModificationDate() {
        return lastModificationDate;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Properties{");
        sb.append("title='").append(title).append('\'');
        sb.append(", URL='").append(URL).append('\'');
        sb.append(", lastModificationDate=").append(lastModificationDate);
        sb.append(", size=").append(size);
        sb.append('}');
        return sb.toString();
    }
}
