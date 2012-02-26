package ifgameengine;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;
import storyengine.IFStoryState;

public abstract class IFObject {
	String m_ID = null;
        List<String> synonyms = new LinkedList<String>();
	IFRoom m_room = null;
	String m_description = "";
	IFAnimation m_animation = null;
	float m_x,m_y;
	
	public IFObject(String a_ID,IFRoom a_room,float a_x,float a_y) {
		m_ID = a_ID;
		m_room = a_room;
		m_x = a_x;
		m_y = a_y;
		m_animation = null;
	}
		
	public static IFObject loadFromXML(Element root,IFRoom r,String path) {
		String type = root.getAttributeValue("type");
		
		if (type.equals("tile")) return IFTileObject.loadFromXML(root,r,path);
		if (type.equals("character")) return IFCharacter.loadFromXML(root,r,path);
		if (type.equals("container")) return IFContainerObject.loadFromXML(root,r,path);
		if (type.equals("location")) return IFLocationObject.loadFromXML(root,r,path);
		if (type.equals("item")) return IFItemObject.loadFromXML(root,r,path);
		
		return null;
	}
	
	public String getID() {
		return m_ID;
	}

	public IFRoom getRoom() {
		return m_room;
	}
	
	abstract public IFObject clone();
	
	public abstract void update(IFGameState game,IFStoryState story,List<String> output) throws Exception;
	
	public void action(IFAction action, IFRoom r, IFGameState gs,List<String> output) {
		action.execute(this,r,gs,output);
	}
	
	public void draw(Graphics2D g2d) {
		if (m_animation!=null) {
			String frame = m_animation.getCurrentFrame();
			if (frame!=null) g2d.drawImage(IFTileManager.getTile(frame),(int)m_x,(int)m_y,null);
		}
	}		
	
	public String description() {
		return m_description;
	}
	
	public void use(IFObject o,List<String> output) {
		
	}
        
        public List<String> getSynonyms() {
            return synonyms;
        }
}
