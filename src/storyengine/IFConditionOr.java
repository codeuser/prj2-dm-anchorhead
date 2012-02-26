package storyengine;

import ifgameengine.IFGameState;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

public class IFConditionOr extends IFCondition {

	LinkedList<IFCondition> m_conditions = new LinkedList<IFCondition>();
	
        public List<IFCondition> getConditions() {
            return m_conditions;
        }

        public boolean evaluate(IFGameState game,IFStoryState story) {
		for(IFCondition c:m_conditions) if (c.evaluate(game,story)) return true;
		return false;
	}
	
	public static IFConditionOr loadFromXML(Element root,String path) {
		IFConditionOr oc = new IFConditionOr();
		
		for(Object o:root.getChildren("condition")) {
			Element e = (Element)o;
			oc.m_conditions.add(IFCondition.loadFromXML(e, path));
		}
		
		return oc;
	}		

}
