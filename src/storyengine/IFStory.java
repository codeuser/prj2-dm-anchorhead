package storyengine;

import ifgameengine.IFAction;
import ifgameengine.IFContainerObject;
import ifgameengine.IFGameState;
import ifgameengine.IFItemObject;
import ifgameengine.IFObject;
import ifgameengine.IFRoom;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

public class IFStory {
	HashMap<String,IFPlotPoint> m_plot_points = new HashMap<String,IFPlotPoint>();
	Set<IFAction> m_important_user_actions = new HashSet<IFAction>();
		
	String m_intromessage = null;	
	
	private IFStory() {
		
	}
	
	public static IFStory loadFromXML(Element story_root,String path) {
		IFStory story = new IFStory();
		
		for (Object ppo : story_root.getChildren("plotpoint")) {
			Element ppe = (Element) ppo;
			IFPlotPoint pp = IFPlotPoint.loadFromXML(ppe,path);
			
			story.m_plot_points.put(pp.m_name,pp);
		}
		
		if (story_root.getChild("intromessage")!=null) story.m_intromessage = story_root.getChild("intromessage").getValue();
		
		return story;
	}
	
	public void computeUserImportantActions(IFGameState gs) {
		// Compute the list of important user actions:
		// From the plotpoint preconditions:
		
		for(IFPlotPoint pp:m_plot_points.values()) {			
			m_important_user_actions.addAll(getConditionActions(pp.m_precondition));
			m_important_user_actions.addAll(getConditionActions(pp.m_trigger));
		}
		
		// Movement actions:
		for(IFRoom r:gs.getRooms()) {
			m_important_user_actions.add(new IFAction("player",IFAction.GOTO,r.getID()));
		}
		
		// Pickup / open / unlock actions:
		for(IFObject o:gs.getAllObjects()) {
			if (o instanceof IFItemObject) {
				m_important_user_actions.add(new IFAction("player",IFAction.TAKE,o.getID()));
			} else if (o instanceof IFContainerObject) {
				IFContainerObject co = (IFContainerObject)o;
				
				m_important_user_actions.add(new IFAction("player",IFAction.OPEN,o.getID()));
				
				if (co.getLock()!=null)	m_important_user_actions.add(new IFAction("player",IFAction.UNLOCK,o.getID(),co.getLock()));
			}
		}
		
		System.out.println("Important User Actions: " + m_important_user_actions.size());
		for(IFAction a:m_important_user_actions) {
			System.out.println(a.toString());
		}
	}
	
	private List<IFAction> getConditionActions(IFCondition c) {
		List<IFAction> l = new LinkedList<IFAction>();
		if (c instanceof IFConditionAction) {			
			l.add(((IFConditionAction)c).m_action);
		} else if (c instanceof IFConditionAnd) {
			for(IFCondition c2:((IFConditionAnd)c).m_conditions) {
				l.addAll(getConditionActions(c2));
			}
		} else if (c instanceof IFConditionOr) {
			for(IFCondition c2:((IFConditionOr)c).m_conditions) {
				l.addAll(getConditionActions(c2));
			}
		} else if (c instanceof IFConditionNot) {
			l.addAll(getConditionActions(((IFConditionNot)c).m_condition));
		}
		
		return l;
	}
	
	public void init(List<String> output) {
		if (m_intromessage!=null) output.add(m_intromessage);
	}	
	
	public IFPlotPoint getPlotPoint(String name) {
		return m_plot_points.get(name);
	}
	
	public int numPlotPoints() {
		return m_plot_points.entrySet().size();
	}
	
	public Collection<IFPlotPoint> getPlotPoints() {
		return m_plot_points.values();
	}
	
	public Set<String> getPlotPointNames() {
		return m_plot_points.keySet();
	}
	
	public Set<IFAction> getImportantUserActions() {
		return m_important_user_actions;
	}
		
}
