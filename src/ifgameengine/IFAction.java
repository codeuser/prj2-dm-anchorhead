package ifgameengine;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jdom.Element;

public class IFAction {
	public static final int TEXT_SPEED = 2; 
	public static final int TEXT_MINIMUM_SPEED = 10; 
	
	public static final String IDLE = "idle";
	public static final String GOTO = "go_to";
	public static final String EXAMINE = "examine";
	public static final String TAKE = "take";
	public static final String OPEN = "open";
	public static final String CLOSE = "close";
	public static final String SLEEP = "sleep";
	public static final String LOCK = "lock";
	public static final String UNLOCK = "unlock";
	public static final String USE = "use";
	public static final String TALK = "talk";
	public static final String BUY = "buy";
	public static final String GIVE = "give";
        
        
	String m_actor;
	String m_type;
	
	// parameters of actions:
	String m_object = null;
	String m_object2 = null;
	String m_text = null;
	
	int m_timmerMax = 0;
	int m_timmer = 0;
        
        
        // for program manager
        public Calendar actionDate;
	
	public IFAction(String actor,String type) {
		m_actor = actor;
		m_type = type;
		m_timmer = m_timmerMax = 0;
	}
	
	public IFAction(String actor,String type,String object) {
		m_actor = actor;
		m_type = type;
		m_object = object;
		m_timmer = m_timmerMax = 0;
	}	

	public IFAction(String actor,String type,String object,String object2) {
		m_actor = actor;
		m_type = type;
		m_object = object;
		m_object2 = object2;
//		if (m_text==null) m_text = "";
		if (m_type.equals(TALK) && m_text!=null) {
			m_timmer = m_timmerMax = TEXT_MINIMUM_SPEED + m_text.length()*TEXT_SPEED;
		} else {
			m_timmer = m_timmerMax = 0;
		}
	}	

	public IFAction(String actor,String type,String object,String object2,String text) {
		m_actor = actor;
		m_type = type;
		m_object = object;
		m_object2 = object2;
		m_text = text;
//		if (m_text==null) m_text = "";
		if (m_type.equals(TALK) && m_text!=null) {	
			m_timmer = m_timmerMax = TEXT_MINIMUM_SPEED + m_text.length()*TEXT_SPEED;
		} else {
			m_timmer = m_timmerMax = 0;
		}
	}	
	
	public IFAction(IFAction a) {
		m_actor = a.m_actor;
		m_type = a.m_type;
		m_object = a.m_object;
		m_object2 = a.m_object2;
		m_text = a.m_text;
		m_timmer = a.m_timmer;
		m_timmerMax = a.m_timmerMax;
	}		
		
	public boolean equals(Object o) {
		if (o instanceof IFAction) {
			IFAction a = (IFAction)o;
			if (m_actor==null) {
				if(a.m_actor!=null) return false;
			} else {
				if (!m_actor.equals(a.m_actor)) return false;
			}
			if (m_type==null) {
				if(a.m_type!=null) return false;
			} else {
				if (!m_type.equals(a.m_type)) return false;
			}
			if (m_object==null) {
				if(a.m_object!=null) return false;
			} else {
				if (!m_object.equals(a.m_object)) return false;
			}
			if (m_object2==null) {
				if(a.m_object2!=null) return false;
			} else {
				if (!m_object2.equals(a.m_object2)) return false;
			}
			
			// Since the text is irrelevant, it's not considered for comparison:
			/*
			if (m_text==null) {
				if(a.m_text!=null) return false;
			} else {
				if (!m_text.equals(a\.m_text)) return false;
			}
			*/
			return true;
		} else {
			return false;
		}
	}
	
	public static IFAction loadFromXML(Element root,String path) {
		IFAction a = new IFAction(root.getAttributeValue("actor"),
								  root.getAttributeValue("type"),
								  root.getAttributeValue("object"),
								  root.getAttributeValue("object2"),
								  root.getAttributeValue("text"));	
		return a;
	}

	
	
	public String toString() {
		return "<action type=\"" + m_type + "\" actor=\"" + m_actor + "\"" + 
			   (m_object!=null ? " object=\"" + m_object + "\"" : "") +
			   (m_object2!=null ? " m_object2=\"" + m_object2 + "\"" : "") +
			   (m_text!=null ? " text=\"" + m_text + "\"" : "") + "/>";			   		
	}
	
    public void saveToXML(PrintStream out,int tabs) {
    	int i;
    	for(i=0;i<tabs;i++) out.print(" ");
    	out.println(this.toString());
    }
	
	public void execute(IFObject o,IFRoom r,IFGameState gs,List<String> output) {
                actionDate = Calendar.getInstance();
            
		m_timmer = m_timmerMax;
		
		if (m_type.equals(IDLE)) {
			
		} else if (m_type.equals(GOTO) ||
				   m_type.equals(TAKE) ||
				   m_type.equals(OPEN) ||
				   m_type.equals(CLOSE) ||
				   m_type.equals(SLEEP) ||
				   m_type.equals(TALK) ||
				   m_type.equals(BUY) ||
				   m_type.equals(GIVE)) {
			IFCharacter c = (IFCharacter)o;
			c.action(this);
		} else if (m_type.equals(LOCK) ||
				   m_type.equals(UNLOCK)) {
			if (o instanceof IFCharacter) {
				IFCharacter c = (IFCharacter)o;
				if (c.contains(m_object2)!=null) {				
					c.action(this);
				} else {
					if (output!=null) output.add("you don't have the " + m_object2);
				} // if
			}
		} else if (m_type.equals(USE)) {
			if (o instanceof IFCharacter) {
				IFCharacter c = (IFCharacter)o;
				if (c.contains(m_object)!=null) {				
					c.action(this);
				} else {
					if (output!=null) output.add("you don't have the " + m_object);
				} // if
			}
		} else if (m_type.equals(IFAction.EXAMINE)) {
			if (m_object==null) {
				// Examine room:
				if (output!=null) output.add(r.description(o));
				gs.succeededAction(this);
			} else {
				if (o instanceof IFCharacter) {
					IFCharacter c = (IFCharacter)o;
					if (c.contains(m_object)!=null) {			
						if (output!=null) output.add(c.contains(m_object).description());
						gs.succeededAction(this);
					} else {
						c.action(this);
					}
				}
			}
		}
	}
	
	public String getType() {
		return m_type;
	}

	public String getActor() {
		return m_actor;
	}

	public String getObject() {
		return m_object;
	}

	public String getObject2() {
		return m_object2;
	}
	
	public String getText() {
		return m_text;
	}

	public boolean executable(IFObject o,IFRoom r,IFGameState gs) {	
		if (m_type.equals(IDLE)) {
			return true;
		} else if (m_type.equals(GOTO) ||
				   m_type.equals(TAKE) ||
				   m_type.equals(OPEN) ||
				   m_type.equals(CLOSE) ||
				   m_type.equals(TALK) ||
				   m_type.equals(BUY) ||
				   m_type.equals(SLEEP)) {
			if (r.contains(m_object)==null) return false;			
		} else if (m_type.equals(GIVE)) {
			if (r.contains(m_object)==null) return false;
			if (((IFCharacter)o).contains(m_object)==null) return false;		
		} else if (m_type.equals(LOCK) ||
				   m_type.equals(UNLOCK)) {
			if (o instanceof IFCharacter) {
				IFCharacter c = (IFCharacter)o;
				if (c.contains(m_object2)==null) return false;				
			}
		} else if (m_type.equals(USE)) {
			if (o instanceof IFCharacter) {
				IFCharacter c = (IFCharacter)o;
				if (c.contains(m_object)==null) return false;
			}
		} else if (m_type.equals(IFAction.EXAMINE)) {
			if (m_object!=null) {
				if (o instanceof IFCharacter) {
					IFCharacter c = (IFCharacter)o;
					if (c.contains(m_object)==null  &&
						r.contains(m_object)==null) return false;					
				}
			}
		}
		
		return true;
	}	
}
