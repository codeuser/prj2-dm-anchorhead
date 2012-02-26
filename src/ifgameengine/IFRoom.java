package ifgameengine;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;
import storyengine.IFStoryState;

public class IFRoom {
	String m_ID;
	String m_description = "";
	IFGameState m_gamestate = null;
	List<IFObject> m_objects = new LinkedList<IFObject>();
	List<IFObject> m_background = new LinkedList<IFObject>();
	Navigation m_navigationGraph = null;
	
	public IFRoom(String a_id,IFGameState a_gamestate) {
		m_ID = a_id;
		m_gamestate = a_gamestate;
	}
	
	public IFRoom(IFRoom r) {
		m_ID = r.m_ID;
		m_description = r.m_description;
		m_gamestate = r.m_gamestate;
		for(IFObject o:r.m_objects) {
			IFRoom tmp = o.m_room;
			o.m_room = this;
			IFObject o2 = o.clone();
			m_objects.add(o2);
			o.m_room = tmp;
		}
		m_background = r.m_background;
		m_navigationGraph = r.m_navigationGraph;		
	}
	
	public static IFRoom loadFromXML(Element root,IFGameState game,String path) {		
		IFRoom room = new IFRoom(root.getAttributeValue("id"),game);

		room.m_description = root.getChildText("description");	
		
		Element bg = root.getChild("background");
		Element fg = root.getChild("objects");

		for(Object oo : bg.getChildren("object")) {
			Element oe = (Element) oo;
			room.m_background.add(IFObject.loadFromXML(oe,room,path));
		}
		for(Object oo : fg.getChildren("object")) {
			Element oe = (Element) oo;
			room.m_objects.add(IFObject.loadFromXML(oe,room,path));
		}
		
		Element ng = root.getChild("navigation");
		if (ng!=null) {
			try {
				room.m_navigationGraph = Navigation.loadFromXML(ng,path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return room;
	}
	
	public String getID() {
		return m_ID;
	}	
	
	public IFRoom clone() {
		// TODO: clone rooms
		
		return null;
	}	
	
	public void addBackgroundObject(IFObject o) {
		m_background.add(o);
	}	
	
	public void addObject(IFObject o) {
		m_objects.add(o);
	}	
	
	public List<IFObject> getObjects() {
		return m_objects;
	}
	
	public List<IFObject> getAllObjects() {
		List<IFObject> l = new LinkedList<IFObject>();
		for(IFObject o:m_objects) {
			if (o instanceof IFContainerObject) {
				l.addAll(((IFContainerObject)o).getAllObjects());
				l.add(o);
			} else {
				l.add(o);
			}
		}
		return l;		
	}	
	
	public void removeObject(IFObject o) {
		m_objects.remove(o);
		for(IFObject o2:m_objects) {
			if (o2 instanceof IFContainerObject) {
				IFContainerObject co2 = (IFContainerObject)o2;
				co2.removeObject(o);
			}
		}
	}		
	
	public IFObject contains(String id) {
		for(IFObject o2:m_objects) {
			if (o2.m_ID.equals(id)) return o2;
			if (o2 instanceof IFContainerObject) {
				IFContainerObject co2 = (IFContainerObject)o2;
				if (co2.m_open) {
					IFObject tmp = co2.contains(id);
					if (tmp!=null) return tmp;
				}
			}
		}
		return null;
	}	
	
	public boolean contains(IFObject o) {
		for(IFObject o2:m_objects) {
			if (o2==o) return true;
			if (o2 instanceof IFContainerObject) {
				IFContainerObject co2 = (IFContainerObject)o2;
				if (co2.m_open) {
					if (co2.contains(o)) return true;
				}
			}
		}
		return false;
	}

	public void update(IFGameState game,IFStoryState story,List<String> output) throws Exception {
		// We copy the objects to another list, since the update of objects might modify the "m_objects" list:
		LinkedList<IFObject> l = new LinkedList<IFObject>();
		l.addAll(m_objects);
		for(IFObject o:l) o.update(game,story,output);		
	}
	
	public void action(IFAction action, IFGameState gs,List<String> output) {
		for(IFObject o:m_objects) {
			if (o.m_ID.equals(action.m_actor)) o.action(action, this,gs,output);
		}
	}
	
	public void draw(Graphics2D g2d) {
		for(IFObject o:m_background) o.draw(g2d);
		for(IFObject o:m_objects) o.draw(g2d);
		
//		if (m_navigationGraph!=null) m_navigationGraph.draw(g2d);
	}		
	
	public String description(IFObject observer) {
		LinkedList<IFLocationObject> l = new LinkedList<IFLocationObject>();
		LinkedList<IFObject> l2 = new LinkedList<IFObject>();
		LinkedList<IFContainerObject> l3 = new LinkedList<IFContainerObject>();
		String itemsDes = "";
		String locDes = "";
				
		for(IFObject o:m_objects) {
			if (o instanceof IFLocationObject) l.add((IFLocationObject)o);		
			if (o instanceof IFItemObject) l2.add(o);
			if (o instanceof IFTileObject) l2.add(o);
			if (o instanceof IFCharacter && o!=observer) l2.add(o);
			if (o instanceof IFContainerObject)	l3.add((IFContainerObject)o);				
		}
		
		if (l2.size()!=0 || l3.size()!=0) {
			String tmp = "You also see ";
			for(IFObject o:l2) {
				tmp = tmp + "the " + o.m_ID;
				
				if (o!=l2.get(l2.size()-1) || l3.size()!=0) tmp = tmp + ", ";				
			}
			for(IFContainerObject o:l3) {
				tmp = tmp + "the " + o.m_ID;

				tmp = tmp + ", ";
			}
			tmp = tmp + ". ";
			
			itemsDes = tmp;
		}
		
		if (l.size()!=0) {
			String tmp = "You can go to ";
			for(IFLocationObject o:l) {
				tmp = tmp + "the " + o.m_ID;
				if (o!=l.get(l.size()-1)) tmp = tmp + ", ";				
			}
			tmp = tmp + " from here.";
			
			locDes = tmp;
		}	
		return m_description + " " + itemsDes + locDes;
	}
	
	// Return true, when no character in the game is executing any action, and there are no actions enqueued:
	public boolean stable() {
		for(IFObject o:m_objects) {
			if (o instanceof IFCharacter) {
				if (!((IFCharacter)o).stable()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
}
