package Database;

import Core.Constants.Constants;
import Core.Enums.DebugType;
import Core.Helper.Predicate;
import Core.Helper.Time;
import Database.Supplier.BloomFilter;
import Database.Supplier.Mongo;
import Database.Supplier.Redis;
import com.mongodb.client.MongoCursor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author vince zydea
 */
public class DataHandler {
    private static final Logger logger = LogManager.getLogger(DataHandler.class.getSimpleName();
    private static final BloomFilter filter = new BloomFilter(Integer.MAX_VALUE, 1024);
    private static final long initialMemory = Runtime.getRuntime().totalMemory();
    private static long totalSitesAdded = Mongo.getSiteCollection().countDocuments();
    private static int totalSitesAddedThisSession = 0;
    private static AtomicInteger sitesAdded = new AtomicInteger(0);
    private static final long timeStarted = System.currentTimeMillis();
    private static int lastCompletionCount = 0;
    private static int largestCompletionCount = 0;
    private static int lowestCompletionCount = 0;

    public static boolean canInsertIntoQueue(String url){
        if(filter.contains(url) && Redis.exists(url) && Predicate.isBlockedURL(url)){ // FUCK
            return false;
        }

        return true;
    }

    public static void addToFilters(String url){
        System.out.println("Adding to filter: " + url);
        filter.add(url);
        Redis.add(url);
    }

    public static void loadBloomAndRedis(){
        Redis.clear();

        MongoCursor cursor = Mongo.getSiteCollection().find().noCursorTimeout(true).iterator();
        while(cursor.hasNext()){
            Document document = (Document)cursor.next();
            if(document != null){
                addToFilters(document.get(Constants.MONGO_ELEMENT_URL_FIELD_NAME).toString());
            }
        }

        cursor = Mongo.getQueueCollection().find().iterator();
        while(cursor.hasNext()){
            Document document = (Document)cursor.next();
            if(document != null){
                addToFilters(document.get(Constants.MONGO_ELEMENT_URL_FIELD_NAME).toString());
            }
        }

        log.printOperationCompleted("Successfully filled bloom filter and redis");
    }

    public static class TimedResultsDisplay extends TimerTask {
        @Override
        public void run() {
            try {
                int currentCompletionCount = sitesAdded.get();
                int interval = Constants.RESULT_DISPLAY_INTERVAL / 1000;
                long timeElapsed = (System.currentTimeMillis() - timeStarted);

                totalSitesAddedThisSession += currentCompletionCount;
                totalSitesAdded += currentCompletionCount;

                if (currentCompletionCount > largestCompletionCount) {
                    largestCompletionCount = currentCompletionCount;
                } else if (currentCompletionCount < lowestCompletionCount || lowestCompletionCount == 0){
                    lowestCompletionCount = currentCompletionCount;
                }

                double memoryUsed = (initialMemory - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

                if (Constants.DEBUG_TYPE.getVal() >= DebugType.LOW.getVal()) {
                    log.getLogger().info("\nTime Elapsed: {}s\nCurrent Threads: {}\nMemory Used: {}mb\nLast Completion Count (per {}s): {}\nLargest Completion Count (per {}s): {}\nSmallest Completion Count (per {}s): {}\nTotal Completed: {}\nTotal Completed This Session: {}\n\n", Time.milliToHHMMSS(timeElapsed), Constants.MAX_CONCURRENT_THREADS, memoryUsed, interval, currentCompletionCount, interval, largestCompletionCount, interval, lowestCompletionCount, totalSitesAdded, totalSitesAddedThisSession);
                }

                lastCompletionCount = currentCompletionCount;
                sitesAdded.set(0);
            } catch (Exception exception){
                log.exceptionThrown(exception);
            }
        }
    }


    public static void incSitesAdded(){
        sitesAdded.incrementAndGet();
    }


}
