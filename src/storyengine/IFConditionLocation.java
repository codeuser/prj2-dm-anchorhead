package storyengine;

import ifgameengine.IFCharacter;
import ifgameengine.IFGameState;

import org.jdom.Element;

public class IFConditionLocation extends IFCondition {
	String m_character = null;
	String m_location = null;
	
	public boolean evaluate(IFGameState game,IFStoryState story) {
		IFCharacter c = (IFCharacter)game.containsObject(m_character);
		if (c!=null && c.getRoom().getID().equals(m_location)) return true;
		return false;
	}
	
	public static IFConditionLocation loadFromXML(Element root,String path) {
		IFConditionLocation c = new IFConditionLocation();
		
		c.m_character = root.getAttributeValue("character");
		c.m_location = root.getAttributeValue("location");

		return c;
	}	
}
