package storyengine;

import ifgameengine.IFGameState;

import org.jdom.Element;

public class IFConditionPlotpoint extends IFCondition {
	String m_plotpoint = null;
	
	public boolean evaluate(IFGameState game,IFStoryState story) {
		Integer status = story.m_storyState.get(m_plotpoint);
		if (status!=null && status==IFPlotPoint.FINISHED) return true;		
		return false;
	}
	
	public static IFConditionPlotpoint loadFromXML(Element root,String path) {
		IFConditionPlotpoint c = new IFConditionPlotpoint();
		
		c.m_plotpoint = root.getAttributeValue("name");

		return c;
	}	

}
