package Core.Enums;

/**
 * @author vince zydea
 *
 * Values:
 * disabled: nothing
 * low: basic reports
 * medium: low + basic operations
 * high: medium + all methods involked
 */
public enum DebugType {
    DISABLED(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private int val;

    DebugType(int val){
        this.val = val;
    }

    public int getVal(){
        return val;
    }
}
