package ifgameengine;

import java.util.List;
import java.util.StringTokenizer;
import org.jdom.Element;
import storyengine.IFStoryState;

public class IFTileObject extends IFObject {
	String m_tile = null;
	
	public IFTileObject(String a_ID,IFRoom a_room,float a_x,float a_y) {
		super(a_ID,a_room,a_x,a_y);
	}
	
	public static IFTileObject loadFromXML(Element root,IFRoom r,String path) {
		String id = null;
		float x = 0;
		float y = 0;
		
		id = root.getAttributeValue("id");
		x = Integer.parseInt(root.getAttributeValue("x"));
		y = Integer.parseInt(root.getAttributeValue("y"));

		IFTileObject t =  new IFTileObject(id,r,x,y);

                t.m_tile=id;
                String synonym = root.getAttributeValue("synonym");
                t.synonyms.add(id);
                if (synonym!=null) {
                    StringTokenizer st = new StringTokenizer(synonym,";");
                    while(st.hasMoreTokens()) t.synonyms.add(st.nextToken());
                }
		
		t.m_description = root.getChildText("description");		
		
		// animations
		Element a = root.getChild("animation");
		if (a!=null) t.m_animation = IFAnimation.loadFromXML(a,path);
		
		return t;
	}
	
	public IFObject clone()
	{
		IFTileObject o = new IFTileObject(m_ID,m_room,m_x,m_y);
		o.m_description = m_description;
		o.m_animation = new IFAnimation(m_animation);

		o.m_tile = m_tile;
		
		return o;
	}
		
	public void update(IFGameState game,IFStoryState story,List<String> output) {
		
	}
}
