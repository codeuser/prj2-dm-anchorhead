package storyengine;

import ifgameengine.IFAction;
import ifgameengine.IFGameState;

import org.jdom.Element;

public class IFConditionAction extends IFCondition {
	IFAction m_action = null;
	
        public IFAction getAction() {
            return m_action;
        }
        
	public boolean evaluate(IFGameState game,IFStoryState story) {
		return game.succeededActionP(m_action);
	}
	
	public static IFConditionAction loadFromXML(Element root,String path) {
		Element ae = root.getChild("action");
		if (ae!=null) {
			IFConditionAction c = new IFConditionAction();
			c.m_action = IFAction.loadFromXML(ae, path);
			return c;
		}
		return null;
	}

}
