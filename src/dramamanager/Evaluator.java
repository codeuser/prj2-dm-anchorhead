package dramamanager;

import ifgameengine.History;
import ifgameengine.IFAction;
import ifgameengine.IFGameState;
import ifgameengine.UserActionTrace;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;

import java.util.List;
import storyengine.IFPlotPoint;
import storyengine.IFStory;

public class Evaluator {
    private static final int MAX_ERRORS=3;
    public static final int IDLE_FROM_START = 1;   // SECONDS BETWEEN START OF GAME TO IDLE STATE
    public static final int IDLE_BETWEEN_ACTIONS = 5; // SECONDS BETWEEN TWO ACTIONS TO IDLE STATE
  
    private double secondsSinceGameStarted;
    public double secondsSinceLastAction;
    public double secondsToFinishTyping;
    public boolean userStarted;
    public boolean playerStuck;
    public String lastPlot;
    public int actionCount = -1;
    public int lastActionCount = -1;
    public int zeroActionCount = 0;
    private IFGameState lastState = null;
    private IFGameState currentState = null;
    
    
    public List<IFAction> userActions = null;
    
    public Dictionary<String,Integer> hintsUsed;
    
    public Evaluator()
    {
        initVars();
    }
    
    private void initVars()
    {
         secondsSinceLastAction = 0;   
         secondsToFinishTyping = 0;
         userStarted = false;
         playerStuck = false;
    }
    
    public void update(IFGameState m_game, Calendar startTime) {
        // used to determine time since last action
         Calendar gameNow = Calendar.getInstance();
        secondsSinceGameStarted = secondsBetween(startTime.getTime(), gameNow.getTime());
        
        if(m_game.getUserActionTrace().size()>0)
        {
            userStarted = true;
            secondsSinceLastAction = secondsBetween(m_game.getUserActionTrace().getLastUserAction().actionDate.getTime(), gameNow.getTime());           
            //System.out.println("Time since last action (sec): " + secondsSinceLastAction);
        }
               
    /*    
        UserActionTrace userTrace = m_game.getUserActionTrace();
        actionCount = userTrace.size();
        // if no actions, then exit
        if (actionCount==0)
            return;
                
        userActions = m_game.getSucceededActions();
        if(userActions.size()==0)
            return;
        Calendar lastActionDate = userActions.get(0).actionDate;
        IFAction lastAction = userActions.get(0);
        for(IFAction eachAction: userActions)
        {
            if(eachAction.actionDate.after(lastActionDate))
            {
                lastAction = eachAction;
                lastActionDate = eachAction.actionDate;
            }
        }
        
        
        
        //System.out.println("Action contains" + userTrace.size());
        //IFAction userAction = userTrace.getLastUserAction();
        
        Calendar calNow = Calendar.getInstance();      
        
       // int seconds = userAction.actionDate.MINUTE;
        
       // System.out.println("Time since last action: " + seconds);       
      */                  
    }   
    
    public void updateState()
    {
       if(userStarted)
        {
            if(secondsSinceLastAction>IDLE_BETWEEN_ACTIONS)
                playerStuck=true;
            else
                playerStuck=false;
        }
        else if(secondsSinceGameStarted>IDLE_FROM_START)
            playerStuck=true;
        else
            playerStuck=false;
    }
    
    public void checkPlayerStuck(IFGameState m_game, IFStory playerStory)
    {
        
        
        
        
        /*
        playerStory.computeUserImportantActions(m_game);
        
        for(IFPlotPoint p: playerStory.getPlotPoints())
            System.out.println(p.getName());
            * 
            */      
        
    }
    
    public double secondsBetween(Date date1, Date date2)
    {
        long timeDiff;
        double secondDiff = 0;
        timeDiff = date2.getTime()-date1.getTime();
        secondDiff = (double) timeDiff/1000;
        
        return secondDiff;
    }
    
    public void resetGameTime()
    {
        secondsSinceGameStarted=0;
    }
    
    public double getGameTime()
    {
        return secondsSinceGameStarted;
    }
}
