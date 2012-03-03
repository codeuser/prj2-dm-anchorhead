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
    
    public double secondsSinceLastAction;
    public double secondsToFinishTyping;
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
    }
    
    public void update(IFGameState m_game) {
        
        UserActionTrace userTrace = m_game.getUserActionTrace();
        actionCount = userTrace.size();
        // if no actions, then exit
        if (actionCount==0)
            return;
                
        userActions = m_game.getSucceededActions();
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
                        
    }
    
    public boolean checkPlayerStuck(IFStory playerStory, IFGameState m_game)
    {
        playerStory.computeUserImportantActions(m_game);
        
        for(IFPlotPoint p: playerStory.getPlotPoints())
            System.out.println(p.getName());
        
        return false;
    }
}
