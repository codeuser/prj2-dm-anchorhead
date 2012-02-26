/**
 * 
 */
package ifgameengine;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;


public class UserActionTrace implements Serializable{

	private static final long serialVersionUID = 1L;
	LinkedList<Pair<Integer,IFAction>> userActionSequence;
	
	public UserActionTrace() {
		userActionSequence = new LinkedList<Pair<Integer,IFAction>>();
	}
	
	public UserActionTrace(UserActionTrace UT){
		userActionSequence = new LinkedList<Pair<Integer,IFAction>>();
		userActionSequence.addAll(UT.userActionSequence);
	}
	
	public String toString() { return ("\nUserActionTrace:: " + userActionSequence); }
	
	public LinkedList<Pair<Integer, IFAction>> getActionTrace() { return userActionSequence; }
	public List<IFAction> getActions() 
	{
		List<IFAction> l = new LinkedList<IFAction>();
		for(Pair<Integer,IFAction> p:userActionSequence) {
			l.add(p.m_b);
		}
		return l; 
	}	
	
	public IFAction getActionFromTrace(int position) { return userActionSequence.get(position).m_b; }
	public void updateUserActionSequence(int time,IFAction action) { userActionSequence.add(new Pair<Integer,IFAction>(time,action)); }
	public IFAction getLastUserAction() { return userActionSequence.get(userActionSequence.size() - 1).m_b; }
	public void removeLastUserActionFromTrace() { userActionSequence.pop();};
	public void removeAllUserActionsFromTrace() { userActionSequence.clear(); }
	public int size() { return userActionSequence.size(); }
	
	public void print() {
		for(Pair<Integer,IFAction> p:userActionSequence) {
			System.out.println(p.m_a + " - " + p.m_b.toString());
		}
	}
	
    public static UserActionTrace loadFromXML(Element root,String path) {
    	UserActionTrace t = new UserActionTrace();
    	
    	t.userActionSequence = new LinkedList<Pair<Integer,IFAction>>();
		for(Object op : root.getChild("actions").getChildren("pair")) {
			Element ep = (Element) op;
					
			t.userActionSequence.add(new Pair<Integer,IFAction>(Integer.parseInt(ep.getChildText("time")),
																  IFAction.loadFromXML(ep.getChild("action"), path)));			
		}    	
    	return t;
    }
    
    public void saveToXML(PrintStream out,int tabs) {
    	int i;
    	for(i=0;i<tabs;i++) out.print(" ");
    	out.println("<UserActionTrace>");
    	
    	for(i=0;i<tabs;i++) out.print(" ");
    	out.println("  <actions>");
    	
    	for(Pair<Integer,IFAction> p:userActionSequence) {
        	for(i=0;i<tabs;i++) out.print(" ");
        	out.println("    <pair>");

        	for(i=0;i<tabs;i++) out.print(" ");
        	out.println("      <time>" + p.m_a + "</time>");
        	
        	p.m_b.saveToXML(out,tabs+6);
        	
        	for(i=0;i<tabs;i++) out.print(" ");
        	out.println("    </pair>");
    	}
    	
    	for(i=0;i<tabs;i++) out.print(" ");
    	out.println("  </actions>");
    	
    	for(i=0;i<tabs;i++) out.print(" ");
    	out.println("</UserActionTrace>");
    }		
	
}
