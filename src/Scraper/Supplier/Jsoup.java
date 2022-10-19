package Scraper.Supplier;


import Core.Constants.Constants;
import Scraper.Helper.Parse;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

/**
 * @author vince zydea
 */
public class Jsoup {
    private static final Logging log = new Logging(Jsoup.class.getSimpleName(), LogManager.getLogger(Jsoup.class));
    private static final int acceptableStatusCode = 200;
    private static final int maxTimeoutPerTrickle = 5000;

    static {
        log.initializedClassNotice();
    }

    /*
        Function: Returns org.bson.Document generated from data retrieved from url.website using org.jsoup.Jsoup
        Params:
            url: url of website to retrieve data from
            depth: number of linked sites parsed before the current one
     */
    public static Pair<Document, List<String>> getDocument(String url, int depth, int linksCount){
        Document document = null;
        List<String> links = null;

        try {
            Connection connection = org.jsoup.Jsoup.connect(url).userAgent(UserAgent.getRandomUserAgent()).timeout(maxTimeoutPerTrickle);
            Connection.Response response = connection.execute();

            if(response.statusCode() == acceptableStatusCode){
                document = connection.get();

                depth++;
                if(document != null && linksCount > 0 && depth <= Constants.JSOUP_DOCUMENT_FETCH_MAX_DEPTH){
                    links = Parse.getLinks(document.select("a[href]"), linksCount);
                }
            }
        } catch (IOException exception) {
            log.exceptionThrown(exception);
        }

        return new Pair<>(document, links);
    }
}
