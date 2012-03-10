package dramamanager;

import dramamanager.Director.States;
import ifgameengine.IFGameState;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import storyengine.IFCondition;
import storyengine.IFPlotPoint;
import storyengine.IFStory;
import storyengine.IFStoryState;

public class GameAdapter {

    public static GameAdapter instance;
    public boolean offerHint;
    public IFPlotPoint plotHint;
    //public ArrayList<GivenHint> listHintsGiven;
    public Map<String,Integer> listHintsGiven;
    public static final int MAX_HINTS=3;
    
    public GameAdapter()
    {
        instance = this;
        offerHint = false;
        plotHint = null;
        //listHintsGiven = new ArrayList<GivenHint>();
        listHintsGiven = new HashMap<String,Integer>();
    }
    
    public void Adapt(Evaluator evalFindings, IFGameState game_state, IFStory m_story, IFStoryState m_state, States director)
    {
        // this is based on the findings of the evaluation function
        
        //if(evalFindings.userStarted==false)
        //{
           plotHint = ProvideHint(game_state, m_story, m_state, director);
           evalFindings.resetGameTime();
        //}
    }
    
    public IFPlotPoint ProvideHint(IFGameState game_state, IFStory m_story, IFStoryState m_state, States director)
    {
        String filterString = null;
        boolean isFiltered = false;        
            
        IFPlotPoint hintPlot = null;
        
        m_state.getStory().computeUserImportantActions(game_state);
        
        //System.out.println("director hint state is " + director.toString());
        
        if(director.equals(States.HELP_ALL))
	{
            isFiltered = false;
	}
        else if(director.equals(States.HELP_WILL))
        {
            isFiltered = true;
            filterString = "william";
        }
        else if(director.equals(States.HELP_EVG))
        {
            isFiltered = true;
            filterString = "evil-god";
        }
              
            // loop through the available plots to see which one can be 
            // approached by the player
            int points = m_story.getPlotPoints().size();
            int randIndex = (int) (Math.round(points*Math.random()));
            
            
            for(IFPlotPoint eachPoint: m_state.getStory().getPlotPoints())
            {   
                if((eachPoint.getPlot()==null)||(eachPoint.getHint()==null))
                    continue;
                
                boolean completedPreconditions;
                IFCondition currentCondition = eachPoint.getPrecondition();
                if(currentCondition==null)
                    completedPreconditions=true;
                else
                    completedPreconditions=currentCondition.evaluate(game_state, m_state);
                
                if(isFiltered)
                {
                    //System.out.println("Current plot is " + eachPoint.getName());
                   // System.out.println("Current plot is " + eachPoint.getPlot());
                    if(currentCondition == null||completedPreconditions)
                        if(eachPoint.getPlot().equals(filterString) && !PlotExceeded(eachPoint))
                        {                               
                            //System.out.println("Current plot is " + eachPoint.getName());
                            //System.out.println("Current plot is " + eachPoint.getPlot());
                            UpdateHints(eachPoint);   
                            hintPlot = eachPoint;                            
                            break;
                        }
                }
                else
                    if(currentCondition == null || currentCondition.evaluate(game_state, m_state))
                    {
                        if(!PlotExceeded(eachPoint))
                        {   
                            //System.out.println("Current plot is " + eachPoint.getName());
                           // System.out.println("Current plot is " + eachPoint.getPlot());
                            UpdateHints(eachPoint);
                            hintPlot = eachPoint;
                            break;
                        }
                    }
            }
            
            return hintPlot;
        
    }
    
    public String getStartingHint(IFStory m_story, IFStoryState m_state, States director)
    {
        int index;
        int randIndex;
        double randVal = Math.random();
        boolean goodRand = false;
        randIndex = 0;
                
        ArrayList<IFPlotPoint> starters = new ArrayList<IFPlotPoint>();
        
        for(IFPlotPoint eachPoint: m_story.getPlotPoints())
        {
            if(eachPoint.getPrecondition()==null)
            {
                //System.out.println("no preconditions");
                //if(eachPoint.getBasicStatus())
                    starters.add(eachPoint);    
            }
        }
        
        while(!goodRand)        // make sure that this doesn't cause and out of bounds error
        {
            randIndex = (int) Math.round(randVal*starters.size());
            if(randIndex>=0 && randIndex<=starters.size()-1)
                goodRand=true;
        }
    //    System.out.print(randIndex);
        return starters.get(randIndex).getHint();
    }
    
    public boolean PlotExceeded(IFPlotPoint plot)
    {
        boolean exceedBool = false;
        if(listHintsGiven.isEmpty())
            exceedBool=false;
        else
        {
            if(listHintsGiven.containsKey(plot.getHint()))                
               if(listHintsGiven.get(plot.getHint())==MAX_HINTS)
                   exceedBool=true;
        }
        
        
        return exceedBool;
    }

    public void UpdateHints(IFPlotPoint newHint)
    {
      //  System.out.println("hints given are:" + listHintsGiven.size());
      //  System.out.println("for " + newHint.getHint() + ": " + listHintsGiven.get(newHint.getHint()));
        if(listHintsGiven.isEmpty())
            listHintsGiven.put(newHint.getHint(), 1);
        else if(listHintsGiven.containsKey(newHint.getHint()))
            listHintsGiven.put(newHint.getHint(),listHintsGiven.get(newHint.getHint())+1);
        else
            listHintsGiven.put(newHint.getHint(), 1);
    }
}
