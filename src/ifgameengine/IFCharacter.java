package ifgameengine;

import characterAI.IFCharacterAI;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import storyengine.IFStoryState;

public class IFCharacter extends IFObject {

    public static final int MOVE_SPEED = 8;
    public static Font m_talkFont = null;
    IFAction m_action = null;
    LinkedList<Point> m_path = null;
    HashMap<String, IFAnimation> m_animations = new HashMap<String, IFAnimation>();
    LinkedList<IFObject> m_inventory = new LinkedList<IFObject>();
    LinkedList<Pair<String, String>> m_verbailInputQueue = new LinkedList<Pair<String, String>>();
    LinkedList<IFAction> m_actionQueue = new LinkedList<IFAction>();
    IFCharacterAI m_ai = null;

    public IFCharacter(String a_ID, IFRoom a_room, float a_x, float a_y) {
        super(a_ID, a_room, a_x, a_y);

        m_action = null;
    }

    public static IFCharacter loadFromXML(Element root, IFRoom r, String path) {
        String id = null;
        float x = 0;
        float y = 0;

        id = root.getAttributeValue("id");
        x = Integer.parseInt(root.getAttributeValue("x"));
        y = Integer.parseInt(root.getAttributeValue("y"));

        IFCharacter character = new IFCharacter(id, r, x, y);

        character.synonyms.add(id);
        String synonym = root.getAttributeValue("synonym");
        if (synonym != null) {
            StringTokenizer st = new StringTokenizer(synonym, ";");
            while (st.hasMoreTokens()) {
                character.synonyms.add(st.nextToken());
            }
        }

        character.m_description = root.getChildText("description");

        Element inve = root.getChild("inventory");
        for (Object o : inve.getChildren("object")) {
            Element oe = (Element) o;
            character.m_inventory.add((IFItemObject) IFObject.loadFromXML(oe, r, path));
        }

        Element ase = root.getChild("animations");
        for (Object o : ase.getChildren("animation")) {
            Element ae = (Element) o;
            character.m_animations.put(ae.getAttributeValue("action"), IFAnimation.loadFromXML(ae, path));
        }

        Element aie = root.getChild("ai");
        if (aie != null) {
            String ai_filename = aie.getValue();

            try {
                SAXBuilder sb = new SAXBuilder(false);
                Document jdoc = sb.build(path + "/" + ai_filename);

                assert jdoc.getRootElement().getName().equals("ai") :
                        "Invalid TileSet XML root"
                        + jdoc.getRootElement().getName();
                character.m_ai = IFCharacterAI.loadFromXML(jdoc.getRootElement(), character, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            character.m_ai = new IFCharacterAI();
        }

        return character;
    }

    public IFCharacter clone() {
        IFCharacter o = new IFCharacter(m_ID, m_room, m_x, m_y);
        o.m_description = m_description;
        if (m_animation == null) {
            System.out.println("Null animation in " + m_ID);
        }
        o.m_animation = (m_animation == null ? null : new IFAnimation(m_animation));
        for (IFObject o2 : m_inventory) {
            IFRoom tmp = o2.m_room;
            o2.m_room = m_room;
            IFObject o3 = (IFObject) (o2.clone());
            o.m_inventory.add(o3);
            o2.m_room = tmp;
        }
        o.m_action = m_action;
        if (m_path == null) {
            o.m_path = null;
        } else {
            o.m_path = new LinkedList<Point>();
            o.m_path.addAll(m_path);
        } // if 
        o.m_animations = m_animations;

        o.m_verbailInputQueue = new LinkedList<Pair<String, String>>();
        o.m_verbailInputQueue.addAll(m_verbailInputQueue);

        o.m_actionQueue = new LinkedList<IFAction>();
        o.m_actionQueue.addAll(m_actionQueue);

        return o;
    }

    public void update(IFGameState game, IFStoryState story, List<String> output) throws Exception {
        if (m_actionQueue.size() > 0
                && m_action == null) {
            m_action = m_actionQueue.removeFirst();
            m_path = null;
        }

        if (m_action != null
                && (m_action.m_type.equals(IFAction.GOTO)
                || m_action.m_type.equals(IFAction.TAKE)
                || m_action.m_type.equals(IFAction.OPEN)
                || m_action.m_type.equals(IFAction.CLOSE)
                || m_action.m_type.equals(IFAction.SLEEP)
                || m_action.m_type.equals(IFAction.EXAMINE)
                || m_action.m_type.equals(IFAction.LOCK)
                || m_action.m_type.equals(IFAction.UNLOCK)
                || m_action.m_type.equals(IFAction.USE)
                || m_action.m_type.equals(IFAction.TALK)
                || m_action.m_type.equals(IFAction.BUY)
                || m_action.m_type.equals(IFAction.GIVE))) {

            IFObject destination = null;

            if (m_action.m_type.equals(IFAction.USE)) {
                destination = m_room.contains(m_action.m_object2);
            } else if (m_action.m_type.equals(IFAction.TALK) || 
                       m_action.m_type.equals(IFAction.GIVE) || 
                       m_action.m_type.equals(IFAction.BUY)) {
                // These actions require a character, look for it, and swap the order ot arguments if neccesary:
                IFCharacter other = null;
                for(IFObject o:m_room.getAllObjects()) {
                    if (o!=this && o instanceof IFCharacter) {
                        other = (IFCharacter)o;
                        break;
                    }
                }
                if (m_action.m_object2==null) {
                    IFObject tmp1 = m_room.contains(m_action.m_object);
                    if (tmp1==null) {
                        m_action.m_object2 = m_action.m_text = m_action.m_object;
                        m_action.m_object = other.m_ID;
                        destination = other;
                    } else {
                        if (!(tmp1 instanceof IFCharacter)) {
                            m_action.m_object2 = m_action.m_text = m_action.m_object;
                            m_action.m_object = other.m_ID;                            
                            destination = other;
                        } else {
                            destination = tmp1;
                        }
                    }
                } else {
                    IFObject tmp1 = m_room.contains(m_action.m_object);
                    IFObject tmp2 = m_room.contains(m_action.m_object2);

                    if (tmp1!=null && tmp1 instanceof IFCharacter) {
                        destination = tmp1;
                        if (m_action.m_text ==null) m_action.m_text = m_action.m_object2;
                    } else if (tmp2!=null && tmp2 instanceof IFCharacter) {
                        destination = tmp2;
                        m_action.m_object2 = m_action.m_text = m_action.m_object;
                        m_action.m_object = tmp2.m_ID;
                    } else {                    
                        destination = tmp1;
                    }
                }
            } else {
                destination = m_room.contains(m_action.m_object);                    
            }

            if (destination == null) {
                if (m_action.m_object == null) {
                    if (output != null) {
                        output.add(m_action.m_type + " what?");
                    }
                } else {
                    if (m_room.m_ID.equals(m_action.m_object)) {
                        if (output != null) {
                            output.add("I am already there!");
                        }
                    } else {
                        if (output != null) {
                            output.add("I don't see the " + m_action.m_object + " around here.");
                        }
                    }
                }

                m_action = null;
                m_animation = null;
            } else {
                Point pos = null;
//				pos = m_room.m_navigationGraph.getNextPositionSimple(new Point(m_x,m_y), new Point(destination.m_x,destination.m_y), MOVE_SPEED);
                if (m_path == null) {
                    m_path = m_room.m_navigationGraph.getPath(new Point(m_x, m_y), new Point(destination.m_x, destination.m_y));
                }
                if (m_path != null) {
                    pos = m_room.m_navigationGraph.walkPath(new Point(m_x, m_y), m_path, MOVE_SPEED);
                }

                if (pos != null) {
                    m_x = pos.m_x;
                    m_y = pos.m_y;
                } else {
                    IFAction next_action = null;
                    IFAnimation next_animation = null;

                    m_path = null;
                    if (m_action.m_type.equals(IFAction.GOTO)) {
                        if (destination instanceof IFLocationObject) {
                            IFLocationObject lo = (IFLocationObject) destination;
                            IFRoom dr = m_room.m_gamestate.containsRoom(lo.m_destinationRoom);

                            if (dr != null) {
                                m_room.removeObject(this);
                                m_room = dr;
                                dr.addObject(this);
                                m_x = lo.m_destinationX;
                                m_y = lo.m_destinationY;

                                m_room.m_gamestate.succeededAction(m_action);
                            } // if
                        } // if
                    } else if (m_action.m_type.equals(IFAction.TAKE)) {
                        if (destination instanceof IFItemObject) {
                            IFItemObject lo = (IFItemObject) destination;
                            m_room.removeObject(lo);
                            m_inventory.add(lo);

                            m_room.m_gamestate.succeededAction(m_action);
                        } else {
                            if (output != null) {
                                output.add("You cannot take that!");
                            }
                        } // if
                    } else if (m_action.m_type.equals(IFAction.OPEN)) {
                        if (destination instanceof IFContainerObject) {
                            IFContainerObject lo = (IFContainerObject) destination;
                            lo.open(output);

                            m_room.m_gamestate.succeededAction(m_action);
                        } else {
                            if (output != null) {
                                output.add("You cannot open that!");
                            }
                        } // if
                    } else if (m_action.m_type.equals(IFAction.CLOSE)) {
                        if (destination instanceof IFContainerObject) {
                            IFContainerObject lo = (IFContainerObject) destination;
                            lo.close(output);

                            m_room.m_gamestate.succeededAction(m_action);
                        } else {
                            if (output != null) {
                                output.add("You cannot close that!");
                            }
                        } // if	
                    } else if (m_action.m_type.equals(IFAction.SLEEP)) {
                        if (destination.m_ID.equals("bed")) {
                            if (output != null) {
                                output.add("Aquick nap! That was refreshing. Lets get started now!");
                            }

                            m_room.m_gamestate.succeededAction(m_action);
                        } else {
                            if (output != null) {
                                output.add("Nah... not confortable enough for sleeping!");
                            }
                        } // if
                    } else if (m_action.m_type.equals(IFAction.LOCK)) {
                        IFObject o = contains(m_action.m_object2);
                        if (destination instanceof IFContainerObject) {
                            IFContainerObject lo = (IFContainerObject) destination;
                            lo.lock(o, output);

                            m_room.m_gamestate.succeededAction(m_action);
                        } else {
                            if (output != null) {
                                output.add("You cannot close that!");
                            }
                        } // if							
                    } else if (m_action.m_type.equals(IFAction.UNLOCK)) {
                        IFObject o = contains(m_action.m_object2);
                        if (destination instanceof IFContainerObject) {
                            IFContainerObject lo = (IFContainerObject) destination;
                            lo.unlock(o, output);

                            m_room.m_gamestate.succeededAction(m_action);
                        } else {
                            if (output != null) {
                                output.add("You cannot close that!");
                            }
                        } // if							
                    } else if (m_action.m_type.equals(IFAction.USE)) {
                        IFObject o = contains(m_action.m_object);
                        destination.use(o, output);

                        m_room.m_gamestate.succeededAction(m_action);
                    } else if (m_action.m_type.equals(IFAction.EXAMINE)) {
                        if (output != null) {
                            output.add(destination.description());
                        }

                        m_room.m_gamestate.succeededAction(m_action);
                    } else if (m_action.m_type.equals(IFAction.BUY)) {

                        m_room.m_gamestate.succeededAction(m_action);
                    } else if (m_action.m_type.equals(IFAction.GIVE)) {
                        IFObject o = contains(m_action.m_object2);

//						System.out.println("The " + m_ID + " is giving " + m_action.m_object2 + " to " + m_action.m_object + "!");

                        if (o == null) {
                            if (output != null) {
                                output.add("You don't have the " + m_action.m_object2 + "!");
                            }
                        } else {
                            if (destination instanceof IFCharacter
                                    && o instanceof IFItemObject) {
                                IFCharacter c = (IFCharacter) destination;
                                m_inventory.remove(o);
                                c.m_inventory.add((IFItemObject) o);

                                m_room.m_gamestate.succeededAction(m_action);
                            } else {
                                if (output != null) {
                                    output.add("You can only give items to persons!");
                                }
                            }

                        }

                    } else if (m_action.m_type.equals(IFAction.TALK)) {
                        if (m_action.m_timmer > 0 && m_action.m_text != null) {
                            next_action = m_action;
                            if (m_action.m_timmer == m_action.m_timmerMax) {
                                next_animation = m_animations.get("talk");
                            } else {
                                next_animation = m_animation;
                            }

                            m_action.m_timmer--;
                        } else {
                            if (destination instanceof IFCharacter) {
                                IFCharacter c = (IFCharacter) destination;

                                c.receiveVerbalInput(m_ID, m_action.m_object2);

                                if (output != null) {
                                    output.add(this.m_ID + " says \"" + m_action.m_text + "\" to the " + m_action.m_object);
                                }
                            }

                            m_room.m_gamestate.succeededAction(m_action);

                            m_action = null;
                            m_animation = null;
                        }
                    } // if

                    m_action = next_action;
                    m_animation = next_animation;
                }
            }
        }

        if (m_ai != null) {
            m_ai.update(this, game, story);
        }

        if (m_animation == null) {
            if (m_action == null) {
                m_animation = m_animations.get(IFAction.IDLE);
            } else {
                m_animation = m_animations.get(m_action.m_type);
            } // if 
        }
        if (m_animation != null) {
            m_animation.update();
        }

    }

    public void action(IFAction a) {
        m_actionQueue.addLast(a);
    }

    public IFObject contains(String id) {
        for (IFObject o2 : m_inventory) {
            if (o2.m_ID.equals(id)) {
                return o2;
            }
            if (o2 instanceof IFContainerObject) {
                IFContainerObject co2 = (IFContainerObject) o2;
                if (co2.m_open) {
                    IFObject tmp = co2.contains(id);
                    if (tmp != null) {
                        return tmp;
                    }
                }
            }
        }
        return null;
    }

    public boolean contains(IFObject o) {
        for (IFObject o2 : m_inventory) {
            if (o2 == o) {
                return true;
            }
            if (o2 instanceof IFContainerObject) {
                IFContainerObject co2 = (IFContainerObject) o2;
                if (co2.m_open) {
                    if (co2.contains(o)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<IFObject> getObjects() {
        return m_inventory;
    }

    public String inventoryDescription() {
        String d = "";
        for (IFObject o : m_inventory) {
            d = d + o.m_ID + "\n";
        }

        return d;
    }

    List<String> wrapSentence(String s, int maxWidth, Graphics2D g2d) {
        List<String> l = new LinkedList<String>();
        String[] l2 = s.split(" ");
        String curr_s = null, new_s = null;
        int i;

        for (i = 0; i < l2.length;) {
            String s2 = l2[i];
            if (curr_s == null) {
                new_s = s2;
            } else {
                new_s = curr_s + " " + s2;
            } // if

            if (m_talkFont.getStringBounds(new_s, g2d.getFontRenderContext()).getWidth() > maxWidth) {
                if (curr_s == null) {
                    l.add(new_s);
                    i++;
                } else {
                    l.add(curr_s);
                    curr_s = null;
                }
            } else {
                curr_s = new_s;
                i++;
            }
        }
        if (curr_s != null) {
            l.add(curr_s);
        }
        return l;
    }

    public void draw(Graphics2D g2d) {
        if (m_talkFont == null) {
            try {
                m_talkFont = Font.createFont(Font.TRUETYPE_FONT, new File("graphics/arial.ttf"));
                m_talkFont = m_talkFont.deriveFont(16.0f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.draw(g2d);

        if (m_action != null && m_action.m_type.equals(IFAction.TALK) && m_action.m_timmer < m_action.m_timmerMax) {
            int center_x = (int) (m_x + 16);
            int width = 2 * Math.max(Math.min(center_x, g2d.getClipBounds().width - center_x) - 20, 128);
            int start_x, start_y;
            List<String> text_to_draw = wrapSentence(m_action.m_text, width, g2d);
            Rectangle2D r = null;

            for (String s : text_to_draw) {
                Rectangle2D r2 = m_talkFont.getStringBounds(s, g2d.getFontRenderContext());
                if (r == null) {
                    r = r2;
                } else {
                    r.setFrame(0, 0, Math.max(r.getWidth(), r2.getWidth()), r.getHeight() + r2.getHeight());
                }
            }

            start_x = Math.max((int) (m_x - (4 + r.getWidth() / 2)), 4);


            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(start_x, (int) (m_y - (48 + r.getHeight())), (int) r.getWidth() + 40, (int) r.getHeight() + 40, 24, 24);
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(start_x, (int) (m_y - (48 + r.getHeight())), (int) r.getWidth() + 40, (int) r.getHeight() + 40, 24, 24);

            if (r.getWidth() / 2 + 16 + 4 >= 56) {
                g2d.setColor(Color.WHITE);
                int xp[] = {(int) (m_x + 40), (int) m_x + 56, (int) m_x + 32};
                int yp[] = {(int) (m_y - 9), (int) (m_y - 9), (int) (m_y + 8)};
                g2d.fillPolygon(xp, yp, 3);
                g2d.setColor(Color.BLACK);
                g2d.drawLine(xp[0], yp[0], xp[2], yp[2]);
                g2d.drawLine(xp[1], yp[1], xp[2], yp[2]);
            } else {
                g2d.setColor(Color.WHITE);
                int xp[] = {(int) (m_x + 8), (int) m_x + 24, (int) m_x + 16};
                int yp[] = {(int) (m_y - 9), (int) (m_y - 9), (int) (m_y)};
                g2d.fillPolygon(xp, yp, 3);
                g2d.setColor(Color.BLACK);
                g2d.drawLine(xp[0], yp[0], xp[2], yp[2]);
                g2d.drawLine(xp[1], yp[1], xp[2], yp[2]);
            }

            g2d.setFont(m_talkFont);
            start_y = (int) (m_y - 14 - r.getHeight());
            for (String s : text_to_draw) {
                g2d.drawString(s, (int) (m_x + 16 - (r.getWidth() / 2)), start_y);
                start_y += m_talkFont.getStringBounds(s, g2d.getFontRenderContext()).getHeight();
            }
        }
    }

    public void receiveVerbalInput(String sender, String topic) {

        m_verbailInputQueue.add(new Pair<String, String>(sender, topic));
    }

    public LinkedList<Pair<String, String>> getVerbailInputQueue() {
        return m_verbailInputQueue;
    }

    public boolean stable() {
        if (m_action != null) {
            return false;
        }
        if (m_actionQueue.size() != 0) {
            return false;
        }
        if (m_verbailInputQueue.size() != 0) {
            return false;
        }

        return true;
    }

    public IFCharacterAI getAI() {
        return m_ai;
    }
}
