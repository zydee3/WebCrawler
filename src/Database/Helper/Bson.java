package Database.Helper;

import Core.Constants.Constants;
import Core.Enums.DocumentType;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;


/**
 * @author vince zydea
 */
public class Bson {
    private final static Logging log = new Logging(Bson.class.getSimpleName(), LogManager.getLogger(Bson.class));

    /**
     * Function: Returns org.bson.Document generated from data retrieved
     * Params:
     1) type: type of document as enum
     2) T: variable string that represents either the url or question depending on type passed.
     3) document: copy of document; data from document used to generate new document with a new unique key; NULL if no need to provide document (ex: MONGO_QUEUE_DOCUMENT).
     * Return: returns generated org.bson.Document from operation.
     * Notes: trying to insert a document with the same key will cause Mongo.UNIQUE_KEY_EXCEPTION. Must create a new bsonDocument in order to generate a new unique key.
     **/
    public static Document generateDocument(DocumentType type, String T, Object o){
        org.jsoup.nodes.Document document = null;
        int depth = 0;

        if(o instanceof org.jsoup.nodes.Document){
            document = (org.jsoup.nodes.Document)o;
        } else if (o instanceof Integer){
            depth = (int)o;
        }
        if(T.isEmpty() || (DocumentType.MONGO_SITE_DOCUMENT.equals(type) && document == null)){
            return null;
        }

        org.bson.Document bsonDocument = new org.bson.Document();

        try {
            switch(type){
                case MONGO_SITE_DOCUMENT:
                    bsonDocument
                            .append(Constants.MONGO_ELEMENT_URL_FIELD_NAME, T)
                            .append(Constants.MONGO_ELEMENT_SITES_HEADER_FIELD_NAME, document.title())
                            .append(Constants.MONGO_ELEMENT_SITES_BODY_FIELD_NAME, document.body().text())
                            .append(Constants.MONGO_ELEMENT_TIMESTAMP_FIELD_NAME, System.currentTimeMillis());
                    break;

                case MONGO_QUEUE_DOCUMENT:
                    bsonDocument
                            .append(Constants.MONGO_ELEMENT_URL_FIELD_NAME, T)
                            .append(Constants.MONGO_ELEMENT_SITES_DEPTH_FIELD_NAME, depth)
                            .append(Constants.MONGO_ELEMENT_TIMESTAMP_FIELD_NAME, System.currentTimeMillis());
                    break;

                case MONGO_QUESTION_DOCUMENT:
                    bsonDocument.append(Constants.MONGO_ELEMENT_QUESTIONS_QUESTION_FIELD_NAME, T);
                    break;

                default:
                    log.unhandledOperation("Unknown DocumentType: " + type + " at insertDocument");
            }
        } catch (Exception exception){
            log.exceptionThrown(exception);
        }

        return bsonDocument;
    }
}
