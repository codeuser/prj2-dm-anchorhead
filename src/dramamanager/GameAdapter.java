package dramamanager;

public class GameAdapter {
    
    public States idleState = States.IDLE;
    public States helpWantedState = States.HELP_NEEDED;
    public States busyState = States.BUSY;
    
    public enum States {
        HELP_NEEDED, IDLE, BUSY
    };
    
    public static GameAdapter instance;
    
    public GameAdapter()
    {
        instance = this;
    }
}