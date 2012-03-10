package dramamanager;

import ifgameengine.IFGameState;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import storyengine.IFStory;
import storyengine.IFStoryState;

public class Director {

    public States idleState = States.IDLE;
    public States helpState = States.HELP_NEEDED;
    public States busyState = States.BUSY;
    
    public States helpAll = States.HELP_ALL;
    public States helpWill = States.HELP_WILL;
    public States helpEvg = States.HELP_EVG;
    
    public Dictionary<Integer,Integer> plot_visits;
    public Dictionary<Integer,Integer> room_visits;
    
    
    public boolean offerHint = false;
    public Calendar gameStart;
    
    public enum States {
        HELP_NEEDED, IDLE, BUSY, HELP_ALL, HELP_WILL, HELP_EVG
    };   
    
    public States directState;
    public States playerState;
    
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
        assignState();
        
        gameadapt = new GameAdapter();
        gameeval = new Evaluator();
    }
    
    public void updateGame(IFGameState gameStat)
    {
        gameeval.update(gameStat, gameStart);  // check if user has done some action
        gameeval.updateState();
//        gameeval.checkPlayerStuck(gameStat, story);
                                                                                                                                            }
    
    public void MakeDecision(IFStory story, IFGameState game_state, IFStoryState story_state)
    {       
        Calendar gameNow = Calendar.getInstance();                      
                    
        double secondsSince = secondsBetween(gameStart.getTime(),gameNow.getTime());
        
        if(gameeval.playerStuck)
        {
//            directState=States.HELP_NEEDED;
            gameadapt.Adapt(gameeval, game_state, story, story_state, directState);
            gameadapt.offerHint=true;
        }      
        else
            gameadapt.offerHint=false;
        
    }
    
    public void Adapt()
    {
//        gameadapt.Adapt(gameeval);
    }
    
    public double secondsBetween(Date date1, Date date2)
    {
        long timeDiff;
        double secondDiff = 0;
        timeDiff = date2.getTime()-date1.getTime();
        secondDiff = (double) timeDiff/1000;
        
        return secondDiff;
    }
    
    public void assignState()
    {
        int stateIndex = 3;
        stateIndex = (int) Math.round( Math.random()*stateIndex);
              
        switch (stateIndex)
        {
                case 0:
                    directState = States.HELP_ALL;
                    break;
                case 1:
                    directState = States.HELP_WILL;
                    break;
                case 2:
                    directState = States.HELP_EVG;
                    break;
                default:
                    directState = States.HELP_ALL;
                    break;
        }
        
        
    }
    
}
