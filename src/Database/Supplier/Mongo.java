package Database.Supplier;

import Core.Constants.Constants;
import Core.Enums.DocumentType;
import Database.Helper.Bson;
import Scraper.Helper.Parse;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import javafx.util.Pair;
import java.util.List;

/**
 * @author vince zydea
 */
public class Mongo {
    private final static Logging log = new Logging(Mongo.class.getSimpleName(), LogManager.getLogger(Mongo.class));
    private final static MongoClient client = new MongoClient();
    private final static MongoDatabase database = client.getDatabase(Constants.MONGO_SCHEMA_NAME);
    private final static MongoCollection siteCollection = database.getCollection(Constants.MONGO_SITE_COLLECTION_NAME);
    private final static MongoCollection queueCollection = database.getCollection(Constants.MONGO_QUEUE_COLLECTION_NAME);
    private final static MongoCollection questionCollection = database.getCollection(Constants.MONGO_QUESTION_COLLECTION_NAME);


    static {
        log.initializedClassNotice();
    }

    public static MongoCollection getSiteCollection(){
        return siteCollection;
    }

    public static MongoCollection getQueueCollection(){
        return queueCollection;
    }

    public static boolean isQueueEmpty(){
        return queueCollection.countDocuments() < 1;
    }

    public static void close(){
        client.close();
    }

    public static void insertDocument(DocumentType type, Document document){
        try {
            switch (type) {
                case MONGO_SITE_DOCUMENT:
                    siteCollection.insertOne(document);
                    break;
                case MONGO_QUEUE_DOCUMENT:
                    queueCollection.insertOne(document);
                    break;
                case MONGO_QUESTION_DOCUMENT:
                    questionCollection.insertOne(document);
                    break;
                default:
                    log.unhandledOperation("Unknown DocumentType: " + type + " at insertDocument");
            }
        } catch (DuplicateKeyException exception){
            log.exceptionThrown(exception);
        }
    }

    public static void deleteDocument(DocumentType type, Document document){
        try {
            switch (type) {
                case MONGO_SITE_DOCUMENT:
                    siteCollection.deleteOne(document);
                    break;
                case MONGO_QUEUE_DOCUMENT:
                    queueCollection.deleteOne(document);
                    break;
                case MONGO_QUESTION_DOCUMENT:
                    questionCollection.deleteOne(document);
                    break;
                default:
                    log.unhandledOperation("Unknown DocumentType: " + type + " at deleteDocument");
            }
        } catch (Exception e){

        }
    }

    public static Pair<String, Integer> getNextInQueue(){
        if(queueCollection.countDocuments() < 1){
            populateQueue();
        }

        String next = "";
        int depth = 0;

        Document document = (Document)queueCollection.find().first();
        if(document != null){
            next = document.get(Constants.MONGO_ELEMENT_URL_FIELD_NAME).toString();
            depth = (int)document.get(Constants.MONGO_ELEMENT_SITES_DEPTH_FIELD_NAME);
        }
        deleteDocument(DocumentType.MONGO_QUEUE_DOCUMENT, document);
        return new Pair(next, depth);
    }

    public static void populateQueue(){
        if(queueCollection.countDocuments() > 0){
            return;
        }

        String question = "";

        // if the questionCollection isn't empty, pull the first question from it. If it is, fill the collection for future use.
        if(questionCollection.countDocuments() < 1){
            // Pull string::questions from top searched questions page on google
            List<String> questions = Parse.TopSites.getMostSearchedQuestions();
            if(!questions.isEmpty()) {
                questions.parallelStream().forEach(q -> {
                    Document document = Bson.generateDocument(DocumentType.MONGO_QUESTION_DOCUMENT, q, null);
                    if(document != null) {
                        insertDocument(DocumentType.MONGO_QUESTION_DOCUMENT, document);
                    }
                });
                question = questions.get(0);
            }
        } else {
            Document document = (Document)questionCollection.find().first();
            if(document != null) {
                questionCollection.deleteOne(document);
                insertDocument(DocumentType.MONGO_QUESTION_DOCUMENT, document);
            }
            question = document.get(Constants.MONGO_ELEMENT_QUESTIONS_QUESTION_FIELD_NAME).toString();;
        }

        if(!question.isEmpty()){
            Document document = (Document) questionCollection.find().first();
            if(document != null){
                try {
                    // Remove the question from the front and re-insert it so its in the back; cycled
                    questionCollection.deleteOne(document);
                    insertDocument(DocumentType.MONGO_QUESTION_DOCUMENT, document);

                    // get the string::question from the document
                    question = document.get(Constants.MONGO_ELEMENT_QUESTIONS_QUESTION_FIELD_NAME).toString();

                    // use google to get website::results and grab the urls to be placed into the questions collection
                    List<String> googleResults = Parse.GoogleParser.getResults(question);

                    // now we have a list of urls (results) we got from google, next we create a document for them to insert into mongo
                    googleResults.parallelStream().forEach(result -> {
                        Document tempDocument = Bson.generateDocument(DocumentType.MONGO_QUEUE_DOCUMENT, result, 0);
                        if(tempDocument != null){
                            insertDocument(DocumentType.MONGO_QUEUE_DOCUMENT, tempDocument);
                        }
                    });

                    log.printOperationCompleted("Queue Populated");
                    return;
                } catch (Exception exception) {
                    log.exceptionThrown(exception);
                }
            }
        }
    }
}
