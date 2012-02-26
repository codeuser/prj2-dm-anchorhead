package ifgameengine;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;
import storyengine.IFStory;
import storyengine.IFStoryState;

public class History {
	private List<Object> m_history = null;
		
	public History() {m_history = new LinkedList<Object>();}
	public History(History a_history) { 
		m_history = new LinkedList<Object>();
		m_history.addAll(a_history.m_history); 
	}
	
	public List<Object> getHistory() { return m_history; }
	public void appendToHistory(Object o) { m_history.add(o); }
	
	public String toString() { 
		String str = "";
		str += "History::\n";
		for ( Object o : m_history ) { 
			if (o instanceof IFStoryState) str += ((IFStoryState) o).toString();
			else if (o instanceof IFAction) str+= ("\n" + ((IFAction) o).toString());
			else if (o == null) str+= " NULL ";
		}
		return str;
	}
	
    public static History loadFromXML(Element root,String path, IFStory story) {
    	History h = new History();
    	
    	for(Object o : root.getChildren()) {
    		Element e = (Element)o;
    		
    		if (e.getName().equals("IFStoryState")) h.appendToHistory(IFStoryState.loadFromXML(e,path,story));
    		if (e.getName().equals("IFGameState")) h.appendToHistory(IFGameState.loadFromXML(e,path));
    		if (e.getName().equals("IFAction")) h.appendToHistory(IFAction.loadFromXML(e,path));
    	}
    	
    	return h;
    }
    
    public void saveToXML(PrintStream out,int tabs) {
    	int i;
    	for(i=0;i<tabs;i++) out.print(" ");
    	out.println("<History>");
    	
    	for(Object o:m_history) {
    		if (o instanceof IFStoryState) ((IFStoryState)o).saveToXML(out,tabs+2);
    		if (o instanceof IFGameState) ((IFGameState)o).saveToXML(out,tabs+2);
    		if (o instanceof IFAction) ((IFAction)o).saveToXML(out,tabs+2);
    	}    	
    	for(i=0;i<tabs;i++) out.print(" ");
    	out.println("</History>");
    }	
}
