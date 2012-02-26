package storyengine;

import ifgameengine.IFAction;
import ifgameengine.IFGameState;
import ifgameengine.Pair;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

public class IFStoryState {
	IFStory m_story = null;
	
	// This list is different in each instance, and characterizes the story state:
	HashMap<String,Integer> m_storyState = null;	
	List<IFAction> m_hinted_actions = new LinkedList<IFAction>(); 

	
	public IFStoryState(IFStory s) {
		m_story = s;
		m_storyState = new HashMap<String,Integer>();

		for (String pp : s.getPlotPointNames()) {
			m_storyState.put(pp,IFPlotPoint.READY);
		}
	}
	
	public IFStoryState(IFStoryState ss) {
		m_story = ss.m_story;
		m_storyState = new HashMap<String,Integer>();
		m_storyState.putAll(ss.m_storyState);
		m_hinted_actions.addAll(ss.m_hinted_actions);
	}
	
	public IFStory getStory() {
		return m_story;
	}
	
	public void printActivePlotpoints() {
		for(IFPlotPoint pp:m_story.getPlotPoints()) {
			if(m_storyState.get(pp.m_name)!=IFPlotPoint.READY)
				System.out.println(pp.m_name + " -> " + m_storyState.get(pp.m_name));
		}		
	}
		
	public void update(IFGameState game,List<String> output) throws Exception {
		// We need this "changes" list, to prevent chained plotpoints being fired in the same cycle:
		LinkedList<Pair<String,Integer>> changes = new LinkedList<Pair<String,Integer>>();
		
		for(IFPlotPoint pp:m_story.getPlotPoints()) {
			if (m_storyState.get(pp.m_name)==IFPlotPoint.READY &&
				pp.m_trigger!=null && pp.m_trigger.evaluate(game,this)) {
				if (pp.m_precondition==null || pp.m_precondition.evaluate(game,this)) {
					for(IFAction a:pp.m_effects) game.enqueueAction(new IFAction(a),this);
					if (pp.m_endgame!=-1) game.setEndGameTimmer(pp.m_endgame);
					
					changes.add(new Pair<String,Integer>(pp.m_name, IFPlotPoint.FINISHED));
					
//					System.out.println("IFStoryState: plotpoint '" + pp.m_name + "' is FINISHED");
				}
			}
		}
		
		while(!changes.isEmpty()) {
			Pair<String,Integer> p = changes.pop();
			m_storyState.put(p.m_a, p.m_b);
		}
	}	
	
	
	public void firePlotPoint(IFGameState game,IFPlotPoint pp) {
		for(IFAction a:pp.m_effects) game.enqueueAction(new IFAction(a),this);
		if (pp.m_endgame!=-1) game.setEndGameTimmer(pp.m_endgame);
		m_storyState.put(pp.m_name, IFPlotPoint.FINISHED);
	}
	
	public void denyPlotPoint(IFPlotPoint pp) {
		m_storyState.put(pp.m_name, IFPlotPoint.DENIED);
	}
	
	public int getPlotPointState(IFPlotPoint pp) {
		return m_storyState.get(pp.m_name);
	}
	
	public String toString() {
		String tmp = "";
		for(IFPlotPoint pp : m_story.getPlotPoints()) {
			tmp = tmp + pp.toString() + " -> " + m_storyState.get(pp.m_name) + "\n";
		}
		
		return tmp;
	}

	public List<String> getPlotPointsVisitedNames() {
		List<String> l = new LinkedList<String>();
		
		for(Map.Entry<String,Integer> pp_e:m_storyState.entrySet()) {
			if (pp_e.getValue()==IFPlotPoint.FINISHED) l.add(pp_e.getKey());
		}

		return l;
	}
	
	public List<IFPlotPoint> getPlotPointsVisited() {
		List<IFPlotPoint> l = new LinkedList<IFPlotPoint>();
		
		for(Map.Entry<String,Integer> pp_e:m_storyState.entrySet()) {
			if (pp_e.getValue()==IFPlotPoint.FINISHED) l.add(m_story.getPlotPoint(pp_e.getKey()));
		}

		return l;
	}
	
	
	public List<IFAction> getHintedActions() {
		return m_hinted_actions;
	}
	
    public static IFStoryState loadFromXML(Element root,String path,IFStory story) {
    	IFStoryState ss = new IFStoryState(story);

    	for(Object o:root.getChildren("plotpoint")) {
    		Element e = (Element)o;
    		ss.m_storyState.put(e.getChildText("name"),Integer.parseInt(e.getChildText("status")));    		
    	}

    	for(Object o:root.getChild("hinted").getChildren("action")) {
    		Element e = (Element)o;
    		ss.m_hinted_actions.add(IFAction.loadFromXML(e, path));
    	}
    	
    	return ss;
    }	
	
    public void saveToXML(PrintStream out,int tabs) {
    	int i;
    	for(i=0;i<tabs;i++) out.print(" ");
    	out.println("<IFStoryState>");
    	
		for(Map.Entry<String,Integer> pp_e:m_storyState.entrySet()) {
			if (pp_e.getValue()==IFPlotPoint.FINISHED) {
		    	for(i=0;i<tabs;i++) out.print(" ");
		    	out.println("  <plotpoint>");
		    	for(i=0;i<tabs;i++) out.print(" ");
		    	out.println("    <name>" + pp_e.getKey() + "</name>");
		    	for(i=0;i<tabs;i++) out.print(" ");
		    	out.println("    <status>" + pp_e.getValue() + "</status>");				
		    	for(i=0;i<tabs;i++) out.print(" ");
		    	out.println("  </plotpoint>");
			}
		}
		
    	for(i=0;i<tabs;i++) out.print(" ");
    	out.println("  <hinted>");
		for(IFAction a:m_hinted_actions) {
			a.saveToXML(out, tabs+4);
		}
    	for(i=0;i<tabs;i++) out.print(" ");
    	out.println("  </hinted>");
    	
    	for(i=0;i<tabs;i++) out.print(" ");
    	out.println("</IFStoryState>");
    }	

	
}
