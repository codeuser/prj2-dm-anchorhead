package characterAI;

import ifgameengine.IFAction;
import ifgameengine.IFCharacter;
import ifgameengine.IFGameState;
import ifgameengine.Pair;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.jdom.Element;
import storyengine.IFStoryState;

public class IFCharacterAI {
	HashMap<String,LinkedList<IFAction>> m_verbalRules = new HashMap<String,LinkedList<IFAction>>();
	LinkedList<Rule> m_rules = new LinkedList<Rule>();
	LinkedList<String> m_items_accepted = new LinkedList<String>();
	boolean m_accepts_all_items = false;
        
        
        public List<Rule> getRules() {
            return m_rules;
        }
        
        public List<String> getVerbalInputsAccepted() {
            List<String> l = new LinkedList<String>();
            l.addAll(m_verbalRules.keySet());
            return l;
        }
	
	public static IFCharacterAI loadFromXML(Element root,IFCharacter c,String path) {
		IFCharacterAI ai = new IFCharacterAI();
		
		for(Object ro:root.getChildren("conversation-rule")) {
			String head_string = null;
			LinkedList<IFAction> body_actions = null;
			
			Element re = (Element)ro;
			
			Element head_e = re.getChild("input");
			head_string = head_e.getAttributeValue("topic");
			body_actions = new LinkedList<IFAction>();
			for(Object oo:re.getChildren("output")) {
				Element oe = (Element)oo;				
				body_actions.add(new IFAction(c.getID(),"talk","",oe.getAttributeValue("topic"),oe.getAttributeValue("text")));				
			}
			
			ai.m_verbalRules.put(head_string, body_actions);			
		}
		
		for (Object gro : root.getChildren("general-rule")) {
			Element gre = (Element) gro;
			Rule gr = Rule.loadFromXML(gre,path);
			
			ai.m_rules.add(gr);
		}			
		
		if (root.getChild("accepts-items")!=null) {
			for (Object io : root.getChild("accepts-items").getChildren("item")) {
				Element ie = (Element) io;				
				ai.m_items_accepted.add(ie.getValue());
			}			
		}
		if (root.getChild("accepts-all-items")!=null) ai.m_accepts_all_items = true;

		return ai;
	}
	
	public void update(IFCharacter c,IFGameState game,IFStoryState story) {
		
		while(!c.getVerbailInputQueue().isEmpty()) {
			Pair<String,String> message = c.getVerbailInputQueue().pop();
			
			System.out.println(c.getID() + " received a message from " + message.m_a + " that says '" + message.m_b + "'");
			
			LinkedList<IFAction> responses = m_verbalRules.get(message.m_b);
			
			if (responses!=null && !responses.isEmpty()) {
				Random generator = new Random();				
				IFAction a = responses.get(generator.nextInt(responses.size()));
				game.enqueueAction(new IFAction(a.getActor(),a.getType(),message.m_a,a.getObject2(),a.getText()),story);
				System.out.println("AI generates action:\n" + a.toString());
			}
		}
		
		for(Rule r:m_rules) {				
			if (r.m_trigger.evaluate(game,story)) {
				for(IFAction a:r.m_effects) {
					game.enqueueAction(new IFAction(a),story);
					System.out.println("AI generates action:\n" + a.toString());
				} // for
			}
		}				
		
		for(IFAction a:game.getSucceededActions()) {
			if (a.getType().equals(IFAction.GIVE) &&
				a.getObject().equals(c.getID())) {
				if (!m_accepts_all_items && !m_items_accepted.contains(a.getObject2())) {
					game.enqueueAction(new IFAction(c.getID(),IFAction.GIVE,a.getActor(),a.getObject2()),story);
					game.enqueueAction(new IFAction(c.getID(),IFAction.TALK,a.getActor(),"reply","No thanks, you keep that!"),story);
				}
			}
		} // for
	}
}
