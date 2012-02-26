package dramamanager;

import ifgameengine.IFGameState;
import java.util.Calendar;
import java.util.Date;
import storyengine.IFStory;

public class Director {

    public States idleState = States.IDLE;
    public States helpWantedState = States.HELP_NEEDED;
    public States busyState = States.BUSY;
    
    public boolean offerHint = false;
    public Calendar gameStart;
    
    public enum States {
        HELP_NEEDED, IDLE, BUSY
    };   
    
    public States directionState;
    
    public GameAdapter gameadapt;
    public Evaluator gameeval;
    
    public Director instance;
    
    public Director()
    {
        instance = this;
        gameStart = Calendar.getInstance();
        directionState = idleState;
        
        gameadapt = new GameAdapter();
        gameeval = new Evaluator();
    }
    
    public void updateGame(IFGameState gameStat, IFStory story)
    {
        gameeval.update(gameStat);        
        gameeval.checkPlayerStuck(story, gameStat);
    }
    
    public void MakeDecision()
    {        
        if(gameeval.actionCount==0)
        {
            Calendar gameNow = Calendar.getInstance();                      
            if(secondsBetween(gameStart.getTime(),gameNow.getTime())>10)
                offerHint=true;
            
        }
        
        
    }
    
    public void Adapt()
    {
        gameadapt.Adapt();
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
