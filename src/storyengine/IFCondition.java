package storyengine;

import ifgameengine.IFGameState;

import org.jdom.Element;

public abstract class IFCondition {
	public abstract boolean evaluate(IFGameState game,IFStoryState story);
	
	public static IFCondition loadFromXML(Element root,String path) {
		String t = root.getAttributeValue("type");
		
		if (t.equals("and")) return IFConditionAnd.loadFromXML(root, path);
		if (t.equals("or")) return IFConditionOr.loadFromXML(root, path);
		if (t.equals("not")) return IFConditionNot.loadFromXML(root, path);
		if (t.equals("plotpoint")) return IFConditionPlotpoint.loadFromXML(root, path);
		if (t.equals("location")) return IFConditionLocation.loadFromXML(root, path);
		if (t.equals("action")) return IFConditionAction.loadFromXML(root, path);
		return null;
	}
}
