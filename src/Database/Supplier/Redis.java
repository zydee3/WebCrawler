package Database.Supplier;

import org.apache.logging.log4j.LogManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author vince zydea
 */
public class Redis {
    private static final Logging log = new Logging(Redis.class.getSimpleName(), LogManager.getLogger(Redis.class));

    private static final String host = "localhost";
    private static JedisPool pool = new JedisPool(new JedisPoolConfig(), host);

    static {
        log.initializedClassNotice();
    }

    public static boolean exists(String url){
        try(Jedis cache = pool.getResource()){
            return cache.exists(url);
        } catch (Exception exception){
            log.exceptionThrown(exception);
        }
        return false;
    }

    public static void add(String url){
        try(Jedis cache = pool.getResource()) {
            cache.set(url, String.valueOf(System.currentTimeMillis()));
        } catch (Exception exception){
            log.exceptionThrown(exception);
        }
    }

    public static void clear(){
        try(Jedis cache = pool.getResource()) {
            cache.flushAll();
            log.printOperationCompleted("Cleared Redis Pool");
        } catch (Exception exception){
            log.exceptionThrown(exception);
        }
    }
}
