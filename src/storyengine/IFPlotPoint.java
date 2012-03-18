package storyengine;

import ifgameengine.IFAction;
import java.util.Calendar;

import java.util.LinkedList;

import org.jdom.Element;

public class IFPlotPoint {
	public static final int READY = 0;
	public static final int EXECUTING = 1;
	public static final int SUSPENDED = 2;
	public static final int FINISHED = 3;
	public static final int DENIED = 4;
	
	String m_name = null;
	String m_plot = null;
        boolean m_isBasic = false;
        String m_hint = null;           // ADDED FOR PROJECT CS680
        public static Calendar plotDate;
	IFCondition m_precondition = null; 
	IFCondition m_trigger = null;
	LinkedList<IFAction> m_effects = new LinkedList<IFAction>();
	int m_endgame = -1;		// if this is different than -1, the game will end in 'm_endgame' cycles
		
	private IFPlotPoint() {
		
	}
        
        public IFCondition getPrecondition() {
            return m_precondition;
        }

        public IFCondition getTrigger() {
            return m_trigger;
        }
	
	public static IFPlotPoint loadFromXML(Element root,String path) {
		IFPlotPoint pp = new IFPlotPoint();

		pp.m_name = root.getAttributeValue("name");
		pp.m_plot = root.getAttributeValue("plot");
                pp.m_hint = root.getAttributeValue("hint");
                //plotDate = Calendar.getInstance();
                pp.m_isBasic =Boolean.getBoolean(root.getAttributeValue("isbasic"));
		
		// preconditions
		Element ppe = root.getChild("preconditions");
		if (ppe!=null) {
			ppe = ppe.getChild("condition");
			pp.m_precondition = IFCondition.loadFromXML(ppe,path);
		}
		
		// trigger:
		Element te = root.getChild("trigger");
		if (te!=null) {
			te = te.getChild("condition");
			pp.m_trigger = IFCondition.loadFromXML(te,path);
		}
		
		// effects:
		Element ee = root.getChild("effects");
		if (ee!=null) {
			for(Object o:ee.getChildren("action")) {
				Element ae = (Element)o;
				IFAction a = IFAction.loadFromXML(ae,path);
				pp.m_effects.add(a);
			}
			for(Object o:ee.getChildren("endgame")) {
				Element ae = (Element)o;
				pp.m_endgame = Integer.parseInt(ae.getAttributeValue("delay"));				
			}			
		}
				
		return pp;
	}	
	
	public String toString() {
		return "<plotpoint name=\"" + m_name + "\"/>";
	}
	
	public String getName() {
		return m_name;
	}

	public String getPlot() {
		return m_plot;
	}
	
	public LinkedList<IFAction> getEffects() {
		return m_effects;
	}
	
	public int getEndGame() {
		return m_endgame;
	}
	
        public String getHint() 
        {
            return m_hint;
        }
        
        public boolean getBasicStatus()
        {
            return m_isBasic;
        }
}
