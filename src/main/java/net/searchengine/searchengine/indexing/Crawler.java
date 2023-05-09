package net.searchengine.searchengine.indexing;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;


public class Crawler
{
    private String url;
    Crawler(String _url)
    {
        url = _url;
    }

    public Connection.Response getResponse(URL url) throws HttpStatusException, IOException {
        // the default body size is 2Mb, to attain unlimited page, use the following.
        // Connection conn = Jsoup.connect(this.url).maxBodySize(0).followRedirects(false);
        Connection conn = Jsoup.connect(url.toString()).followRedirects(false);

        /* establish the connection and retrieve the response */
        Connection.Response res = conn.execute();

        return res;
    }

    public String getUrl() {
        return url;
    }

    public int getPageSize() throws IOException {

        Connection.Response resp = Jsoup.connect(url).execute();
        String length = resp.header("content-length");
        if (length == null) return resp.bodyAsBytes().length;
        return Integer.parseInt(length);
    }

    public String getLastModificationDate() throws IOException{

        // Wed, 29 Mar 2023 09:41:52 GMT
        URL crawlURL = new URL(url);
        Connection.Response returns = getResponse(crawlURL);
        Connection.Response res = returns.bufferUp();
        Document doc = res.parse();

        String lastModified = res.header("Last-Modified");
        if (lastModified == null || lastModified.equals("")) {
            lastModified = res.header("Date");
        }

        return lastModified;
    }

    public Parser getParser() throws ParserException{
        return (new Parser(url));
    }

    public List extractTitle() throws ParserException{
        List title = null;
        Parser parser = getParser();
        if (parser == null) return null;

        // list[0]: get title as string
        title = new ArrayList<>();

        NodeFilter filter = new NodeClassFilter(TitleTag.class);
        NodeList nodelist =  parser.parse(filter);
        String str ="";

        for(int i = 0; i < nodelist.size(); i++){
            Node node = nodelist.elementAt(i);
            if(node instanceof TitleTag) {
                TitleTag titletag = (TitleTag) node;
                str = titletag.getTitle();
            }
        }
        title.add(str);

        // list[1]: get tokenized title
        Vector<String> tokenizedStr = new Vector<>();
        StringTokenizer st = new StringTokenizer(str);
        while (st.hasMoreTokens())
            tokenizedStr.add(st.nextToken());

        title.add(tokenizedStr);
        return title;
    }

    public Vector<String> extractWords() throws ParserException, IOException {

        Parser parser = getParser();
        if (parser == null) return null;

        Document doc = Jsoup.connect(url).get();
        Element bodyElement = doc.select("body").first();
        String bodyContent = bodyElement.text();

        Vector<String> tokenizedStr = new Vector<>();
        StringTokenizer st = new StringTokenizer(bodyContent);
        while (st.hasMoreTokens())
            tokenizedStr.add(st.nextToken());

        return tokenizedStr;
    }

    public Vector<String> extractLinks() throws ParserException {
        // extract links in url and return them
        Vector<String> links = new Vector<>();

        LinkBean lb = new LinkBean();
        lb.setURL(url);
        URL[] URL_array = lb.getLinks();

        for(int i=0; i<URL_array.length; i++) {
            String link = URL_array[i].toString();
            links.add(link);
        }
        return links;
    }


}



