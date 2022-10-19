package Core.Helper;

/**
 * @author vince zydea
 */
public class StringModifier {
    public static java.lang.String buildString(java.lang.String... args){
        java.lang.StringBuilder builder = new java.lang.StringBuilder();

        for(java.lang.String s : args){
            builder.append(s);
        }

        return builder.toString();
    }
}
