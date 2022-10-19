package Core.Helper;

/**
 * @author vince zydea
 */
public class Random {
    public static int nextInt(int min, int max){
        return (int)(Math.random() * ((max - min) + 1)) + min;
    }

    public static double nextDouble(int min, int max){
        return (Math.random() * ((max - min) + 1)) + min;
    }
}
