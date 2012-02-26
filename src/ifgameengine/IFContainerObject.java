package ifgameengine;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.jdom.Element;
import storyengine.IFStoryState;

public class IFContainerObject extends IFObject{
	List<IFObject> m_objects = new LinkedList<IFObject>();
	IFAnimation m_openAnimation = null, m_closedAnimation = null;
	boolean m_open = true;
	boolean m_canOpen = false;
	boolean m_locked = false;
	String m_lock = null;
	String m_openMessage = null;
	String m_closeMessage = null;
	
	public IFContainerObject(String a_ID,IFRoom a_room,float a_x,float a_y,boolean open,boolean canOpen,boolean locked,String lock) {
		super(a_ID,a_room,a_x,a_y);
		m_open = open;
		m_canOpen = canOpen;
		m_locked = locked;
		m_lock = lock;
	}
	
	public static IFContainerObject loadFromXML(Element root,IFRoom r,String path) {
		String id = null;
		float x = 0;
		float y = 0;
		boolean open = true;
		boolean canOpen = false;
		boolean locked = false;
		String lock = null;
		
		id = root.getAttributeValue("id");
		x = Integer.parseInt(root.getAttributeValue("x"));
		y = Integer.parseInt(root.getAttributeValue("y"));
		if (root.getChildText("open")!=null) open = Boolean.parseBoolean(root.getChildText("open"));
		if (root.getChildText("canopen")!=null) canOpen = Boolean.parseBoolean(root.getChildText("canopen"));
		if (root.getChildText("locked")!=null) locked = Boolean.parseBoolean(root.getChildText("locked"));
		if (root.getChildText("locked")!=null) lock = root.getChildText("lock");
		
		IFContainerObject container = new IFContainerObject(id,r,x,y,open,canOpen,locked,lock);
		
                String synonym = root.getAttributeValue("synonym");
                container.synonyms.add(id);
                if (synonym!=null) {
                    StringTokenizer st = new StringTokenizer(synonym,";");
                    while(st.hasMoreTokens()) container.synonyms.add(st.nextToken());
                }

                container.m_description = root.getChildText("description");		
		
		if (root.getChildText("openmessage")!=null) container.m_openMessage = root.getChildText("openmessage");
		if (root.getChildText("closemessage")!=null) container.m_closeMessage = root.getChildText("closemessage");

		// animations
		Element oa = root.getChild("openanimation").getChild("animation");
		if (oa!=null) container.m_openAnimation = IFAnimation.loadFromXML(oa,path);
		Element ca = root.getChild("closedanimation").getChild("animation");
		if (ca!=null) container.m_closedAnimation = IFAnimation.loadFromXML(ca,path);
		
		// objects
		Element objects = root.getChild("objects");
		for(Object oo : objects.getChildren("object")) {
			Element oe = (Element) oo;
			container.m_objects.add(IFObject.loadFromXML(oe,r,path));
		}

		return container;
	}
	
	public IFObject clone()
	{
		IFContainerObject o = new IFContainerObject(m_ID,m_room,m_x,m_y,m_open,m_canOpen,m_locked,m_lock);
		o.m_description = m_description;
		o.m_animation = (o.m_animation==null ? null:new IFAnimation(m_animation));
		for(IFObject o2:m_objects) {
			IFRoom tmp = o2.m_room;
			o2.m_room = m_room;
			IFObject o3 = o2.clone();
			o.m_objects.add(o3);
			o2.m_room = tmp;
		}
		o.m_openAnimation = new IFAnimation(m_openAnimation);
		o.m_closedAnimation = new IFAnimation(m_closedAnimation);
		o.m_openMessage = m_openMessage; 
		o.m_closeMessage = m_closeMessage; 

		return o;
	}
	
	public void addObject(IFObject o) {
		m_objects.add(o);
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
	
	public void removeObject(IFObject o) {
		m_objects.remove(o);
		for(IFObject o2:m_objects) {
			if (o2 instanceof IFContainerObject) {
				IFContainerObject co2 = (IFContainerObject)o2;
				co2.removeObject(o);
			}
		}
	}	
	
	public void update(IFGameState game,IFStoryState story,List<String> output) {
		
	}
	
	public void draw(Graphics2D g2d) {
		if (m_open) {
			if (m_openAnimation!=null) {
				String frame = m_openAnimation.getCurrentFrame();
				if (frame!=null) g2d.drawImage(IFTileManager.getTile(frame),(int)m_x,(int)m_y,null);
			}			
		} else {
			if (m_closedAnimation!=null) {
				String frame = m_closedAnimation.getCurrentFrame();
				if (frame!=null) g2d.drawImage(IFTileManager.getTile(frame),(int)m_x,(int)m_y,null);
			}						
		}
	}		
	
	public String description() {
		String tmp = m_description;
		
		if (m_open) {
			if (m_objects.size()==0) {
				tmp = tmp + " (it is open and empty)";
			} else {
				tmp = tmp + " (it is open and contains ";
				for(IFObject o2:m_objects) {
					tmp = tmp + "the " + o2.m_ID + ", ";
				}
				tmp = tmp + ")";
			}
		} 
		
		return tmp;
	}	
	
	public void open(List<String> output) {
		if (!m_locked) {
			if (m_canOpen) {
				m_open = true;
				if (m_openMessage==null) if (output!=null) output.add("open!");
									else if (output!=null) output.add(m_openMessage);
			} else {
				if (output!=null) if (output!=null) output.add("You cannot open that!");
			}
		} else {
			if (output!=null) if (output!=null) output.add("It's locked!");
		}
	}	

	public void close(List<String> output) {
		if (!m_locked) {
			if (m_canOpen) {
				m_open = false;
				if (m_closeMessage==null) if (output!=null) output.add("closed!");
									 else if (output!=null) output.add(m_closeMessage);
			} else {
				if (output!=null) if (output!=null) output.add("You cannot close that!");
			}
		} else {
			if (output!=null) if (output!=null) output.add("It's locked!");
		}
	}
	
	public void lock(IFObject o,List<String> output) {
		if (!m_locked) {
			if (m_lock!=null) {
				if (o.m_ID.equals(m_lock)) {
					m_locked=true;
					if (output!=null) output.add("locked!");
				} else {
					if (output!=null) output.add("The " + o.m_ID + " is not the appropriate object to lock it");
				}
			} else {
				if (output!=null) output.add("You cannot lock that!");
			}
		} else {
			if (output!=null) output.add("It's already locked!");
		}
	}		
	
	public void unlock(IFObject o,List<String> output) {
		if (m_locked) {
			if (m_lock!=null) {
				if (o.m_ID.equals(m_lock)) {
					m_locked=false;
					if (output!=null) output.add("unlocked!");
				} else {
					if (output!=null) output.add("The " + o.m_ID + " is not the appropriate object to unlock it");
				}
			} else {
				if (output!=null) output.add("You cannot unlock that!");
			}
		} else {
			if (output!=null) output.add("It's already unlocked!");
		}
	}		
	
	public void use(IFObject o,List<String> output) {
		if (m_lock!=null) {
			if (m_locked) {
				if (o.m_ID.equals(m_lock)) {
					m_locked=false;
					if (output!=null) output.add("unlocked!");
				} else {
					if (output!=null) output.add("The " + o.m_ID + " is not the appropriate object to unlock it");
				}
			} else {
				if (o.m_ID.equals(m_lock)) {
					m_locked=true;
					if (output!=null) output.add("locked!");
				} else {
					if (output!=null) output.add("The " + o.m_ID + " is not the appropriate object to lock it");
				}
			}
		} else {
			if (output!=null) output.add("I don't know how.");
		}
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
	
	public String getLock() {
		return m_lock;
	}

}
