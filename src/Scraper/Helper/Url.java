package Scraper.Helper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author vince zydea
 */
public class Url {
    @NotNull
    @Contract(pure = true)
    public static String getSimpleUrl(@NotNull String url){
        return url.replaceAll("(.*(?=www)|(?<=com).*)|(.*(?=en\\.w)|(?<=org).*)", "");
    }
}
