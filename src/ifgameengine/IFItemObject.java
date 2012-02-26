package ifgameengine;

import java.awt.Graphics2D;
import java.util.List;
import java.util.StringTokenizer;
import org.jdom.Element;
import storyengine.IFStoryState;

public class IFItemObject extends IFObject {
		
	public IFItemObject(String a_ID,IFRoom a_room,float a_x,float a_y) {
		super(a_ID,a_room,a_x,a_y);
	}
	
	public static IFItemObject loadFromXML(Element root,IFRoom r,String path) {
		String id = null;
		float x = 0;
		float y = 0;
		
		id = root.getAttributeValue("id");
		x = Integer.parseInt(root.getAttributeValue("x"));
		y = Integer.parseInt(root.getAttributeValue("y"));
		
		IFItemObject item = new IFItemObject(id,r,x,y);
                
                String synonym = root.getAttributeValue("synonym");
                item.synonyms.add(id);
                if (synonym!=null) {
                    StringTokenizer st = new StringTokenizer(synonym,";");
                    while(st.hasMoreTokens()) item.synonyms.add(st.nextToken());
                }		
		
                item.m_description = root.getChildText("description");		
		
		Element ae = root.getChild("animation");
		item.m_animation = IFAnimation.loadFromXML(ae,path);		

		return item;
	}
	
	public IFObject clone()
	{
		IFItemObject o = new IFItemObject(m_ID,m_room,m_x,m_y);
		o.m_description = m_description;
		o.m_animation = new IFAnimation(m_animation);

		return o;
	}
		
	public void update(IFGameState game,IFStoryState story,List<String> output) {
		
	}
		
	public void draw(Graphics2D g2d) {
		if (m_animation!=null) {
			String frame = m_animation.getCurrentFrame();
			if (frame!=null) g2d.drawImage(IFTileManager.getTile(frame),(int)m_x,(int)m_y,null);
		}
	}		

}
