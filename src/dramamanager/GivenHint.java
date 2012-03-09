package dramamanager;

import storyengine.IFPlotPoint;

/**
 *
 * @author jumpy
 */
public class GivenHint {
    private IFPlotPoint plot;
    private int timesGiven;
    
    public GivenHint(IFPlotPoint newPlot)
    {
        plot=newPlot;
        timesGiven=1;
    }
    
    public IFPlotPoint getPlot()
    {
        return plot;
    }
    
    public void increment()
    {
        timesGiven++;
    }
    
    public int getTimesGiven()            
    {
        return timesGiven;
    }
}
