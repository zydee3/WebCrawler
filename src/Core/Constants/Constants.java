package Core.Constants;

import Core.Enums.DebugType;

/**
 * @author vince zydea
 */
public class Constants {
    // Global Constants
    public static final boolean USE_SHUTDOWN_HOOK = true;
    public static final int MAX_CONCURRENT_THREADS = 24;
    public static final DebugType DEBUG_TYPE = DebugType.HIGH;
    public static final int RESULT_DISPLAY_INTERVAL = 60 * 1000; // 60 seconds
    public static final boolean USE_RESULT_DISPLAY = true;

    // Mongo Constants
    public static final String MONGO_SCHEMA_NAME = "spider";
    public static final String MONGO_SITE_COLLECTION_NAME = "sites";
    public static final String MONGO_QUEUE_COLLECTION_NAME = "queue";
    public static final String MONGO_QUESTION_COLLECTION_NAME = "questions";
    public static final String MONGO_ELEMENT_URL_FIELD_NAME = "url";
    public static final String MONGO_ELEMENT_TIMESTAMP_FIELD_NAME = "timestamp";
    public static final String MONGO_ELEMENT_SITES_HEADER_FIELD_NAME = "title";
    public static final String MONGO_ELEMENT_SITES_BODY_FIELD_NAME = "body";
    public static final String MONGO_ELEMENT_SITES_DEPTH_FIELD_NAME = "depth";
    public static final String MONGO_ELEMENT_QUESTIONS_QUESTION_FIELD_NAME = "question";

    // Services Constants
    public static final long DURATION_BETWEEN_GOOGLE_REQUEST = 300000;


    // Jsoup Constants
    public static final int JSOUP_DOCUMENT_FETCH_MAX_DEPTH = 3;
    public static final int JSOUP_MAX_LINKS_RETRIEVED = 30;

    // Question Cache Resource
    public static final String QUESTIONS_TOP_VIEWED_SITES_RANKING = "https://www.mondovo.com/keywords/most-asked-questions-on-google";
    public static final String[] QUESTIONS_BACKUP_STARTING_SITES = {
            "https://www.healthline.com/",
            "https://www.foxnews.com/",
            "https://www.wowhead.com/",
            "https://www.alexa.com/topsites",
            "https://www.similarweb.com/top-websites/united-states",
            "http://divinfosys.com/",
            "https://www.reddit.com/",
            "http://pinterest.com",
            "https://www.cnn.com/",
            "https://www.quora.com/"
    };

    public static final String[] blackListedSites = {
            "www.dictionary.com",
            "www.thesaurus.com",
            "en.wikipedia.org",
            "www.youtube.com"
    };
}
