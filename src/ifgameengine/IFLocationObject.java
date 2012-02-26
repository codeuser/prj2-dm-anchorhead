package ifgameengine;

import java.util.List;
import java.util.StringTokenizer;
import org.jdom.Element;
import storyengine.IFStoryState;

public class IFLocationObject extends IFObject {
	int m_destinationX = 0,m_destinationY = 0;
	String m_destinationRoom = null;
	
	public IFLocationObject(String a_ID,IFRoom a_room,float a_x,float a_y) {
		super(a_ID,a_room,a_x,a_y);
	}
	
	public static IFLocationObject loadFromXML(Element root,IFRoom r,String path) {

		String id = null;
		float x = 0;
		float y = 0;
		
		id = root.getAttributeValue("id");
		x = Integer.parseInt(root.getAttributeValue("x"));
		y = Integer.parseInt(root.getAttributeValue("y"));
		
		IFLocationObject location = new IFLocationObject(id,r,x,y);
		
                String synonym = root.getAttributeValue("synonym");
                location.synonyms.add(id);
                if (synonym!=null) {
                    StringTokenizer st = new StringTokenizer(synonym,";");
                    while(st.hasMoreTokens()) location.synonyms.add(st.nextToken());
                }
                
                location.m_description = root.getChildText("description");			
		
		Element d = root.getChild("destination");
		
		if (d!=null) {	
			location.m_destinationRoom = d.getAttributeValue("room");
			location.m_destinationX = Integer.parseInt(d.getAttributeValue("x"));
			location.m_destinationY = Integer.parseInt(d.getAttributeValue("y"));
		}
		return location;
	}

	public IFObject clone()
	{
		IFLocationObject o = new IFLocationObject(m_ID,m_room,m_x,m_y);
		o.m_description = m_description;
		o.m_animation = (o.m_animation==null ? null:new IFAnimation(m_animation));

		o.m_destinationX = m_destinationX;
		o.m_destinationY = m_destinationY;
		o.m_destinationRoom = m_destinationRoom;
		
		return o;
	}
		
	public void update(IFGameState game,IFStoryState story,List<String> output) {
		
	}
	
}
