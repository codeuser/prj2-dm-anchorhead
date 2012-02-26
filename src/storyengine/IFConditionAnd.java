package storyengine;

import ifgameengine.IFGameState;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

public class IFConditionAnd extends IFCondition {

	LinkedList<IFCondition> m_conditions = new LinkedList<IFCondition>();
	
        public List<IFCondition> getConditions() {
            return m_conditions;
        }
        
	public boolean evaluate(IFGameState game,IFStoryState story) {
		for(IFCondition c:m_conditions) if (!c.evaluate(game,story)) return false;
		return true;
	}
	
	public static IFConditionAnd loadFromXML(Element root,String path) {
		IFConditionAnd ac = new IFConditionAnd();
		
		for(Object o:root.getChildren("condition")) {
			Element e = (Element)o;
			ac.m_conditions.add(IFCondition.loadFromXML(e, path));
		}
		
		return ac;
	}	

}
