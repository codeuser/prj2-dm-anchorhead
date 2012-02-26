package characterAI;

import ifgameengine.IFAction;
import java.util.LinkedList;
import org.jdom.Element;
import storyengine.IFCondition;

public class Rule {
	IFCondition m_trigger = null;
	LinkedList<IFAction> m_effects = new LinkedList<IFAction>();
	
	
	private Rule() {
		
	}
	
	public static Rule loadFromXML(Element root,String path) {
		Rule r = new Rule();

		// trigger:
		Element te = root.getChild("trigger");
		if (te!=null) {
			te = te.getChild("condition");
			r.m_trigger = IFCondition.loadFromXML(te,path);
		}
		
		// effects:
		Element ee = root.getChild("effects");
		if (ee!=null) {
			for(Object o:ee.getChildren("action")) {
				Element ae = (Element)o;
				IFAction a = IFAction.loadFromXML(ae,path);
				r.m_effects.add(a);
			}
		}
				
		return r;
	}	
	
}
