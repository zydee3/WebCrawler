package Scraper;

import Core.Constants.Constants;
import Core.Enums.DocumentType;
import Core.Helper.StringModifier;
import Database.DataHandler;
import Database.Helper.Bson;
import Database.Supplier.Mongo;
import Database.Supplier.Redis;
import Scraper.Helper.Parse;
import Scraper.Supplier.Jsoup;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author vince zydea
 */
public class Worker {
    private static final Logging log = new Logging(Worker.class.getSimpleName(), LogManager.getLogger(Worker.class));
    public static CopyOnWriteArrayList<String> activeProcess = new CopyOnWriteArrayList <>();

    static {
        try {
            if (Mongo.isQueueEmpty()) {
                long elapsed = Parse.GoogleParser.timeSinceLastGoogleRequest();
                if (elapsed > Constants.DURATION_BETWEEN_GOOGLE_REQUEST) {
                    try {
                        Thread.sleep( Constants.DURATION_BETWEEN_GOOGLE_REQUEST - elapsed);
                    } catch (Exception exception) {
                        log.exceptionThrown(exception);
                    }
                }
                Mongo.populateQueue();
            }

            log.initializedClassNotice();
        } catch (Exception exception){
            log.exceptionThrown(exception);
        }
    }

    public static void loadWorkers(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(Constants.MAX_CONCURRENT_THREADS);
        for(int i = 0; i < Constants.MAX_CONCURRENT_THREADS; i++){ // scheduleWithFixedDelay does not create a new crawler object every iteration..
            executor.scheduleWithFixedDelay(new Crawler(), 0, 1, TimeUnit.MILLISECONDS);
        }
    }

    public static class Crawler implements Runnable {
        @Override
        public void run() {

            Pair<String, Integer> next = Mongo.getNextInQueue();

            String url = next.getKey();

            if(url.isEmpty() || activeProcess.contains(url)){
                return;
            }

            activeProcess.add(url);

            int depth = next.getValue();

            log.printOperationCompleted(StringModifier.buildString("Starting queue item ", url, " at depth: ", String.valueOf(depth)));

            if(url.isEmpty() || depth < 0 || depth > Constants.JSOUP_MAX_LINKS_RETRIEVED){
                return;
            }

            Pair<Document, List<String>> contents = Jsoup.getDocument(url, depth, Constants.JSOUP_MAX_LINKS_RETRIEVED);
            Document document = contents.getKey();
            List<String> links = contents.getValue();

            if(document != null){
                org.bson.Document temp = Bson.generateDocument(DocumentType.MONGO_SITE_DOCUMENT, url, document);
                if(temp != null){
                    Mongo.insertDocument(DocumentType.MONGO_SITE_DOCUMENT, temp);
                }

                Redis.add(url);
            }

            if(links != null && !links.isEmpty()){
                depth++;
                for(String link : links){
                    if(!activeProcess.contains(link) && DataHandler.canInsertIntoQueue(link)) {
                        org.bson.Document temp = Bson.generateDocument(DocumentType.MONGO_QUEUE_DOCUMENT, link, depth);
                        if(temp != null) {
                            Mongo.insertDocument(DocumentType.MONGO_QUEUE_DOCUMENT, temp);
                        }
                    }
                }
            }

            activeProcess.remove(url);
            DataHandler.incSitesAdded();
            log.printOperationCompleted(StringModifier.buildString("Completed queue item ", url));
        }
    }

}
