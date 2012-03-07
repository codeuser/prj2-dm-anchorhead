package dramamanager;

import java.util.ArrayList;
import storyengine.IFPlotPoint;
import storyengine.IFStory;

public class GameAdapter {

    public static GameAdapter instance;
    public boolean offerHint;
    
    public GameAdapter()
    {
        instance = this;
        offerHint = false;
    }
    
    public void Adapt(Evaluator evalFindings, IFStory m_story)
    {
        // this is based on the findings of the evaluation function
        
        if(evalFindings.userStarted==false)
        {
           ProvideHint(getStartingHint(m_story));
           evalFindings.resetGameTime();
        }
    }
    
    public void ProvideHint(String hintText)
    {
        
    }
    
    public String getStartingHint(IFStory m_story)
    {
        int index;
        int randIndex;
        double randVal = Math.random();
                
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
        randIndex = (int) Math.round(randVal*starters.size());
        System.out.print(randIndex);
        return "Not sure if you know but..." + starters.get(randIndex).getHint();
    }

}