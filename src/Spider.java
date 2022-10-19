import Core.Constants.Constants;
import Core.Enums.DebugType;
import Database.DataHandler;
import Scraper.Worker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;


/**
 * @author vince zydea
 */
public class Spider {
    private static final Logger logger = LogManager.getLogger(Spider.class.getName());

    public static void main(String[] args){


        DataHandler.loadBloomAndRedis();
        Worker.loadWorkers();

        if(Constants.USE_SHUTDOWN_HOOK){
            Runtime.getRuntime().addShutdownHook(new Thread(shutdown()));

            if(Constants.DEBUG_TYPE.equals(DebugType.))
        }

        if(Constants.USE_RESULT_DISPLAY){
            try {
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new DataHandler.TimedResultsDisplay(), 0, Constants.RESULT_DISPLAY_INTERVAL);

                //if(Constants.DEBUG_TYPE.getVal() )
            } catch (Exception exception){
                logger.info("Exception Thrown: {}", exception.getMessage());
                logger.error("Exception Thrown: {}\n{}", exception.getMessage(), exception.getStackTrace());
            }
        }

        logger.info("Spider is now online");
    }


    @NotNull
    @Contract(value = " -> new", pure = true)
    public static final Runnable shutdown(){
        return new Runnable(){
            @Override
            public void run(){

            }
        };
    }
}
