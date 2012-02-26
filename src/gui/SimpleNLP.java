/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import ifgameengine.*;
import java.util.HashMap;
import java.util.StringTokenizer;
import storyengine.*;

/**
 *
 * @author santi
 */
public class SimpleNLP {
           
    public static final HashMap<String,String> action_synonyms = new HashMap<String,String>();
    public static HashMap<String,String> object_synonyms = new HashMap<String,String>();

    static {
        action_synonyms.put("idle","idle");
        action_synonyms.put("go_to","go_to");
        action_synonyms.put("go","go_to");
        action_synonyms.put("walk","go_to");
        action_synonyms.put("move","go_to");
        action_synonyms.put("examine","examine");
        action_synonyms.put("look","examine");
        action_synonyms.put("explore","examine");
        action_synonyms.put("take","take");
        action_synonyms.put("pick up","take");
        action_synonyms.put("grab","take");
        action_synonyms.put("open","open");
        action_synonyms.put("close","close");
        action_synonyms.put("shut","close");
        action_synonyms.put("sleep","sleep");
        action_synonyms.put("lock","lock");
        action_synonyms.put("unlock","unlock");
        action_synonyms.put("use","use");
        action_synonyms.put("utilize","use");
        action_synonyms.put("talk","talk");
        action_synonyms.put("say","talk");
        action_synonyms.put("ask","talk");
        action_synonyms.put("tell","talk");
        action_synonyms.put("speak","talk");
        action_synonyms.put("buy","buy");
        action_synonyms.put("give","give");
    }
    

    public static void getObjectSynonyms(IFGameState game, IFStory story) {
        for(IFRoom room:game.getRooms()) {
            getObjectSynonyms(room);
        }
        
        for(IFPlotPoint pp:story.getPlotPoints()) {
            getObjectSynonyms(pp.getPrecondition());
            getObjectSynonyms(pp.getTrigger());
        }
        
        
        System.out.println("NLP system recognizes the following objects:");
        for(String o:object_synonyms.keySet()) System.out.println(o);
    }

    
    public static void getObjectSynonyms(IFCondition cond) {
        if (cond instanceof IFConditionAction) {
            IFAction a = ((IFConditionAction)cond).getAction();
            
            String o = a.getObject();
            if (o!=null) object_synonyms.put(o,o);
            o = a.getObject2();
            if (o!=null) object_synonyms.put(o,o);
        } else if (cond instanceof IFConditionAnd) {
            for(IFCondition cond2:((IFConditionAnd)cond).getConditions()) {
                getObjectSynonyms(cond2);
            }
        } else if (cond instanceof IFConditionOr) {
            for(IFCondition cond2:((IFConditionOr)cond).getConditions()) {
                getObjectSynonyms(cond2);
            }
        } else if (cond instanceof IFConditionNot) {
            getObjectSynonyms(((IFConditionNot)cond).getCondition());
        }
    }

    
    public static void getObjectSynonyms(IFRoom room) {
        for(IFObject object:room.getObjects()) {
            getObjectSynonyms(object);
        }
    }

    public static void getObjectSynonyms(IFObject object) {
        for(String name:object.getSynonyms()) {
            object_synonyms.put(name,object.getID());
        }
        if (object instanceof IFContainerObject) {
            for(IFObject object2:((IFContainerObject)object).getObjects()) {
                getObjectSynonyms(object2);
            }
        }
        if (object instanceof IFCharacter) {
            for(IFObject object2:((IFCharacter)object).getObjects()) {
                getObjectSynonyms(object2);
            }            
            for(String input:((IFCharacter)object).getAI().getVerbalInputsAccepted()) {
                if (input.length()>0) object_synonyms.put(input,input);
            }
        }
    }

    
    // This method is slightly smarter than the one below, but still VERY basic, it simply looks for appearances of the synonyms of actions and objects in the sentence
    public static IFAction synonymParseActionString(String focus_character, String text) { 
        // Assume the order in which the 3 important elements of the sentence (verb, object1 and object2) appear in that order:
        // look for the action:
        String action = null;
        String action_translated = null;
        int action_position = 0;
        for(String action2:action_synonyms.keySet()) {
            int action_position2 = text.indexOf(action2);
            if (action_position2>0 && text.charAt(action_position2-1)!=' ') action_position2 = -1;
            if (action_position2!=-1 && 
                action_position2+action2.length()<text.length() && 
                text.charAt(action_position2+action2.length())!=' ') action_position2 = -1;
            if (action_position2!=-1) {
                if (action==null || action_position2<action_position) {
                    action = action2;
                    action_translated = action_synonyms.get(action2);
                    action_position = action_position2;
                }
            }
        }
        if (action==null) return null;
        text = text.substring(action_position+action.length());
        
        // look for object 1:
        String object1 = null;
        String object1_translated = null;
        int object1_position = 0;
        for(String o:object_synonyms.keySet()) {
            int o_pos = text.indexOf(o);
            if (o_pos>0 && text.charAt(o_pos-1)!=' ') o_pos = -1;
            if (o_pos!=-1 && 
                o_pos+o.length()<text.length() && 
                text.charAt(o_pos+o.length())!=' ') o_pos = -1;
            if (o_pos!=-1) {
                if (object1==null || o_pos<object1_position) {
                    object1 = o;
                    object1_translated = object_synonyms.get(o);
                    object1_position = o_pos;
                }
            }
        }
        
        if (object1==null) return new IFAction(focus_character, action_translated);
        text = text.substring(object1_position+object1.length());
        
        // look for object 2:
        String object2 = null;
        String object2_translated = null;
        int object2_position = 0;
        for(String o:object_synonyms.keySet()) {
            int o_pos = text.indexOf(o);
            if (o_pos>0 && text.charAt(o_pos-1)!=' ') o_pos = -1;
            if (o_pos!=-1 && 
                o_pos+o.length()<text.length() && 
                text.charAt(o_pos+o.length())!=' ') o_pos = -1;
            if (o_pos!=-1) {
                if (object2==null || o_pos<object2_position) {
                    object2 = o;
                    object2_translated = object_synonyms.get(o);
                    object2_position = o_pos;
                }
            }
        }

        if (object2==null) return new IFAction(focus_character, action_translated, object1_translated);
        
        return new IFAction(focus_character, action_translated, object1_translated, object2_translated);
    }
    
    
    // This method basically needs the string to be <action> <object1> <object2> without any other words in between
    public static IFAction basicParseActionString(String focus_character, String text) {
        StringTokenizer t = new StringTokenizer(text);
        if (t.hasMoreElements()) {
            String action = t.nextToken();
            String object = null;
            String object2 = null;
            if (t.hasMoreElements()) {
                object = t.nextToken();
            }
            if (t.hasMoreElements()) {
                object2 = t.nextToken();
            }

            System.out.println(action + " - " + object + " - " + object2);

            if (object == null) {
                return new IFAction(focus_character, action);
            } else {
                if (object2 == null) {
                    return new IFAction(focus_character, action, object);
                } else {
                    if (action.equals(IFAction.TALK)) {
                        while (t.hasMoreElements()) {
                            object2 = object2 + " " + t.nextToken();
                        }
                        return new IFAction(focus_character, action, object, object2, object2);
                    } else {
                        return new IFAction(focus_character, action, object, object2);
                    }
                }
            }
        }
        return null;
    }
    
}
