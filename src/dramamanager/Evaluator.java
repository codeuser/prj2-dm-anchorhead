package dramamanager;

import ifgameengine.IFAction;
import ifgameengine.IFGameState;

import java.util.List;
import storyengine.IFStory;

public class Evaluator {
    private static final int MAX_ERRORS=3;
    
    public double secondsSinceLastAction;
    public double secondsToFinishTyping;
    public int lastActionCount = -1;
    public int zeroActionCount = 0;
    private IFGameState lastState = null;
    private IFGameState currentState = null;
    
    
    public List<IFAction> userActions = null;
    
    public Evaluator()
    {
        initVars();
    }
    
    private void initVars()
    {
         secondsSinceLastAction = 0;   
         secondsToFinishTyping = 0;
    }
    
    public void Update(IFGameState m_game) {
        userActions = m_game.getSucceededActions();
        
        if(userActions.isEmpty())
        {
            lastActionCount++;
            if(lastActionCount==MAX_ERRORS)
                provideAnyHint();            
        }        
        else
        {
                                
            System.out.println(userActions.size());
            lastActionCount = userActions.size();
            
            for(IFAction eachAction: userActions)
            {
                System.out.println(eachAction.getObject());
            }
        }
    }
    
    public void provideAnyHint()
    {
        // hints user to try any of the possible commands allowable
        
    }
    
    public boolean checkPlayerStuck(IFStory playerStory, IFGameState m_game)
    {
        playerStory.computeUserImportantActions(m_game);
        
        return true;
    }
}
