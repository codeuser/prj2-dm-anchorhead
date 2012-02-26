package ifgameengine;

import java.awt.Graphics2D;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;
import storyengine.IFStoryState;


public class IFGameState {
	List<IFRoom> m_map = new LinkedList<IFRoom>();
	
	boolean m_actionQueueLock = false;
	LinkedList<IFAction> m_actionQueue = new LinkedList<IFAction>();
	LinkedList<IFAction> m_succeededActions = new LinkedList<IFAction>();
	LinkedList<IFAction> m_succeededActionsLastCycle = new LinkedList<IFAction>();

	UserActionTrace m_userTrace = new UserActionTrace();
	History m_history = new History();
	int m_cycle = 0;
	
	int m_endGameTimmer = -1;
	boolean m_gameFinished = false;
		
	private IFGameState() {
	}
	
	public static IFGameState loadFromXML(Element root,String path) {
		IFGameState game = new IFGameState();
		
		for (Object ro : root.getChildren("room")) {
			Element re = (Element) ro;
			
			game.m_map.add(IFRoom.loadFromXML(re,game,path));
		}					
			
		return game;
	}
	
	public IFGameState(IFGameState gs) {
		for(IFRoom r:gs.m_map) {
			IFRoom r2 = new IFRoom(r);
			r2.m_gamestate = this;
			m_map.add(r2);
		}
		m_actionQueue.addAll(gs.m_actionQueue);
		m_succeededActions.addAll(m_succeededActions);
		m_succeededActionsLastCycle.addAll(gs.m_succeededActionsLastCycle);
		m_endGameTimmer = gs.m_endGameTimmer;
		m_gameFinished = gs.m_gameFinished;
		m_cycle = gs.m_cycle;
		m_userTrace = new UserActionTrace(gs.m_userTrace);	
		m_history = new History(gs.m_history);
	}
	public History getHistory()
	{
		return m_history;
	}
	public List<IFRoom> getRooms() {
		return m_map;
	}
	
	public void addRoom(IFRoom r) {
		m_map.add(r);
	}
	
	public IFRoom containsRoom(String id) {
		for(IFRoom r:m_map) {
			if (r.getID().equals(id)) return r;
		}
		return null;
	}		
	
	public boolean containsObject(IFObject id) {
		for(IFRoom r:m_map) if (r.contains(id)) return true;
		return false;
	}	
	
	public IFObject containsObject(String id) {
		for(IFRoom r:m_map) {
			IFObject o = r.contains(id);
			if (o!=null) return o;
		}
		return null;
	}		
	
	public void init(List<String> output) {
	}
	
	public boolean update(IFStoryState story,List<String> output) throws Exception {
		m_succeededActionsLastCycle.clear();
		m_succeededActionsLastCycle.addAll(m_succeededActions);

/*		
		{
			if (!m_succeededActionsLastCycle.isEmpty()) {
				System.out.println("Succeded actions:");
				for(IFAction a:m_succeededActionsLastCycle) System.out.println(a.toString());
			}
		}
*/
		
		m_succeededActions.clear();
		
		lockActionQueue();
		while(m_actionQueue.size()>0) {
			IFAction a = m_actionQueue.pop();
			action(a,output);
		}
		unlockActionQueue();
		for(IFRoom r:m_map) r.update(this,story,output);
//		m_history.appendToHistory(new IFStoryState(story));
		if (m_endGameTimmer!=-1) {
			m_endGameTimmer--;
			if (m_endGameTimmer<=0) m_gameFinished=true;
		}
		
		m_cycle++;
		
		return true;
	}
	
	private void action(IFAction a,List<String> output) {
		for(IFRoom r:m_map) r.action(a,this,output);
	}
	
	public void draw(String focus,Graphics2D g2d) {
		for(IFRoom r:m_map) {
			if (r.contains(focus)!=null) {
				r.draw(g2d);
				return;
			}
		}
	}		
	
	public void draw(IFObject focus,Graphics2D g2d) {
		for(IFRoom r:m_map) {
			if (r.contains(focus)) {
				r.draw(g2d);
				return;
			}
		}
	}	
	
	public synchronized void lockActionQueue() {
		while(m_actionQueueLock) {
			System.out.flush();
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
		m_actionQueueLock = true;
	}

	public synchronized void unlockActionQueue() {
		m_actionQueueLock = false;
		notifyAll();
	}
	
	public void enqueueAction(IFAction action,IFStoryState storyState) {
		lockActionQueue();
		m_actionQueue.add(action);
		m_userTrace.updateUserActionSequence(getCycle(), action);
		m_history.appendToHistory(new IFStoryState(storyState));
		m_history.appendToHistory(action);
		unlockActionQueue();
	}
	
	public void enqueueActionWithoutHistoryLog(IFAction action) {
		lockActionQueue();
		m_actionQueue.add(action);
		m_userTrace.updateUserActionSequence(getCycle(), action);
		unlockActionQueue();
	}
		
	public void succeededAction(IFAction action) {
		m_succeededActions.add(action);
	}
	
	public boolean succeededActionP(IFAction action) {
		return m_succeededActionsLastCycle.contains(action);
	}		

	public List<IFAction> getSucceededActions() {
		return m_succeededActionsLastCycle;
	}		
	
	public boolean finished() {
		return m_gameFinished;
	}
	
	public void setEndGameTimmer(int timmer) {
		m_endGameTimmer = timmer;
	}
	
	public String findOtherCharacter(String me) {
		IFObject meObj = containsObject(me);
		IFRoom meRoom = (meObj.getRoom());
		for(IFObject o:meRoom.getObjects()) {
			if (o instanceof IFCharacter) {
				if (!o.getID().equals(me)) return o.getID();
			}
		}
		
		return null;
	}
	
	// Return true, when no character in the game is executing any action, and there are no actions enqueued:
	public boolean stable() {
		if (m_actionQueue.size()>0) return false;
		for(IFRoom r:m_map) {
			if (!r.stable()) {
				return false;
			}
		}
		
		return true;
	}
	
	public int getCycle() {
		return m_cycle;
	}
	
	public UserActionTrace getUserActionTrace() {
		return m_userTrace;
	}
	
	public List<IFObject> getAllObjects() {
		List<IFObject> l = new LinkedList<IFObject>();
		for(IFRoom r:m_map) l.addAll(r.getAllObjects());
		return l;
	}
	
	public IFObject contains(String id) {
		for(IFRoom r:m_map) {
			IFObject o = r.contains(id);
			if (o!=null) return o;
		}
		return null;
	}
	
	public IFRoom getObjectRoom(String id) {
		for(IFRoom r:m_map) {
			IFObject o = r.contains(id);
			if (o!=null) return r;
		}
		return null;		
	}

	public void saveToXML(PrintStream out, int i) {

		out.println("<IFGameState>");

		//...
		
		out.println("</IFGameState>");
	}		
}
