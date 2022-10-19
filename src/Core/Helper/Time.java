package Core.Helper;

import java.util.concurrent.TimeUnit;

/**
 * @author vince zydea
 */
public class Time {
    public static String milliToHHMMSS(long milliseconds){
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) % TimeUnit.MINUTES.toSeconds(1));
    }
}
