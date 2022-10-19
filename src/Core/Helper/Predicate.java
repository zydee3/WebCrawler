package Core.Helper;

import Core.Constants.Constants;
import Scraper.Helper.Url;

/**
 * @author vince zydea
 */
public class Predicate {

    public static boolean isBlockedURL(String url){
        url = Url.getSimpleUrl(url);
        for(String blocked : Constants.blackListedSites){
            if(url.toLowerCase().equals(blocked)){
                return true;
            }
        }
        return false;
    }

}
