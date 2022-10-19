package Scraper.Helper;

import Core.Helper.Predicate;
import Database.Supplier.Redis;
import org.apache.logging.log4j.LogManager;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author vince zydea
 */
public class Parse {
    private static final Logging log = new Logging(Parse.class.getSimpleName(), LogManager.getLogger(Parse.class));

    public static class GoogleParser {
        private static long lastGoogleRequest = -1;

        public static String generateSearchQuery(String question){
            if(question.isEmpty()){
                return "";
            }

            // "example search result " -> "example+search+result+"
            question.replace(" ", "+");

            // "example+search+result+" -> "example+search+result"
            while(question.charAt(question.length() - 1) == '+'){
                question = question.substring(0, question.length() - 1);
            }

            return question;
        }

        public static List<String> getResults(String query){
            CopyOnWriteArrayList<String> results = new CopyOnWriteArrayList<>();
            try {

                Document document = Scraper.Supplier.Jsoup.getDocument("http://www.google.com/search?q=" + generateSearchQuery(query), 0, 0).getKey();
                lastGoogleRequest = System.currentTimeMillis();

                if(document != null) {
                    document.select("a[href]").parallelStream().forEach(link -> {
                        // get all links elements from html
                        String currentLink = link.attr("abs:href");

                        // this link leads to websites and generally isn't ads or google's similar search recommendations
                        if (currentLink.contains("http://www.google.com/url?q=")) {

                            // extracting target website's url embedded inside google's request url
                            currentLink = currentLink.replace("http://www.google.com/url?q=", "");
                            currentLink = currentLink.substring(0, currentLink.indexOf('&'));

                            // only get websites from non-blocked sites
                            if (!Predicate.isBlockedURL(currentLink)) {
                                results.add(currentLink);
                            }
                        }
                    });
                }

            } catch (Exception exception){
                log.exceptionThrown(exception);
            }
            return results;
        }

        public static long timeSinceLastGoogleRequest(){
            return System.currentTimeMillis() - lastGoogleRequest;
        }
    }

    public static class TopSites {
        private static final String topSite = "https://www.mondovo.com/keywords/most-asked-questions-on-google";
        private static final String elementTag = "td";

        public static List<String> getMostSearchedQuestions(){
            Document document = Scraper.Supplier.Jsoup.getDocument(topSite, 0, 0).getKey();
            CopyOnWriteArrayList<String> results = new CopyOnWriteArrayList<>();

            if(document != null){
                Elements elements = document.select(elementTag);
                if(!elements.isEmpty()){
                    elements.parallelStream().forEach(element -> {
                        String leadingWord = element.text().split(" ")[0];
                        if (!leadingWord.isEmpty() && leadingWord.toLowerCase().equals("how") || leadingWord.toLowerCase().equals("what")) {
                            results.add(element.text());
                        }
                    });
                }

            }

            return results;
        }
    }

    public static List<String> getLinks(Elements elements, int linksCount){
        if(!elements.isEmpty()){
            try {
                CopyOnWriteArrayList<String> finalLinks = new CopyOnWriteArrayList<>();
                AtomicInteger count = new AtomicInteger(0);
                elements.parallelStream().forEach(element -> {
                    String link = element.attr("abs:href");
                    if (count.incrementAndGet() <= linksCount && !finalLinks.contains(link) && !Redis.exists(link)) {
                        finalLinks.add(link);
                    }
                });

                return finalLinks;
            } catch (Exception exception){
                log.exceptionThrown(exception);
            }
        }

        return null;
    }
}
