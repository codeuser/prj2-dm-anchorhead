package dramamanager;

import ifgameengine.IFGameState;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import storyengine.IFStory;

public class Director {

    public States idleState = States.IDLE;
    public States helpState = States.HELP_NEEDED;
    public States busyState = States.BUSY;
    
    public Dictionary<Integer,Integer> plot_visits;
    public Dictionary<Integer,Integer> room_visits;
    
    public static final int HELP_SINCE_START = 2;
    public static final int HELP_SINCE_ACTION = 25;
    
    public boolean offerHint = false;
    public Calendar gameStart;
    
    public enum States {
        HELP_NEEDED, IDLE, BUSY
    };   
    
    public States directState;
    
    public GameAdapter gameadapt;
    public Evaluator gameeval;
    
    public Director instance;
    public double hintsPerHour;
    public double hintsGiven;       // in 3 intervals - per hour
    public double hintsTotal;
    
    public Director()
    {
        instance = this;
        gameStart = Calendar.getInstance();
        directState = idleState;
        
        gameadapt = new GameAdapter();
        gameeval = new Evaluator();
    }
    
    public void updateGame(IFGameState gameStat, IFStory story)
    {
        gameeval.update(gameStat);  // check if user has done some action
        //gameeval.checkPlayerStuck(story, gameStat);
    }
    
    public void MakeDecision()
    {        
        Calendar gameNow = Calendar.getInstance();                      
                    
        double secondsSince = secondsBetween(gameStart.getTime(),gameNow.getTime());
        
        if(gameeval.actionCount==0)
        {
            // if no actions have been taken for a while, then provide assistance
            if(secondsSince>HELP_SINCE_START)
                gameadapt.offerHint=true;            
        }
        else
            if(secondsSince>HELP_SINCE_ACTION)
                gameadapt.offerHint=true;
        
        
        
    }
    
    public void Adapt()
    {
        gameadapt.Adapt(gameeval);
    }
    
    public double secondsBetween(Date date1, Date date2)
    {
        long timeDiff;
        double secondDiff = 0;
        timeDiff = date2.getTime()-date1.getTime();
        secondDiff = (double) timeDiff/1000;
        
        return secondDiff;
    }
}
