package gui;

import dramamanager.Director;
import dramamanager.Evaluator;
import dramamanager.GameAdapter;
import ifgameengine.IFAction;
import ifgameengine.IFCharacter;
import ifgameengine.IFGameState;
import ifgameengine.IFTileManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import javax.swing.*;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import storyengine.IFStory;
import storyengine.IFStoryState;

public class GraphicalInterfaceJFrame extends JPanel {

    private static final long serialVersionUID = 1L;
    public IFGameState m_game = null;
    public IFStory m_story = null;
    public IFStoryState m_story_state = null;
    LinkedList<IFAction> m_actions_to_enqueue = new LinkedList<IFAction>();
    JTextField m_input = null;
    JTextPane m_output = null;
    JTextPane m_last_output = null;
    JTextPane m_inventory = null;
    String m_focus_character = "player";
    static PrintStream m_logger = null;
    
    public static Calendar gameInit;
    
    /**
     * Edited 2/25/2012
     */
    
    public static Director directgame;
    
    
    Action sendInputText = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            m_logger.println("> " + m_input.getText());

            if (m_input.getText().equals("quit")) {
                m_game.setEndGameTimmer(10);
                return;
            }
            IFAction action = SimpleNLP.synonymParseActionString(m_focus_character, m_input.getText());
//            IFAction action = SimpleNLP.basicParseActionString(m_focus_character, m_input.getText());
            if (action!=null) {
                m_actions_to_enqueue.add(action);
            } else {
                output("Say what?");
            }
            m_input.setText("");
        }
    };

    public static void main(String s[]) throws IOException 
    {
        gameInit = Calendar.getInstance();
        String loggerName = "IF-log-" + new Date().getTime() + ".txt";
        m_logger = new PrintStream(new FileOutputStream(new File(loggerName)));

        String gameToLoad = "games/anchorhead";
        GraphicalInterfaceJFrame game = new GraphicalInterfaceJFrame();

        game.loadGame(gameToLoad);
        game.setPreferredSize(new Dimension(640, 512));

        /*
         * try { Thread.sleep(3000); } catch (InterruptedException e) {
         * e.printStackTrace(); }
         */
        game.m_input = new JTextField();
        game.m_input.setPreferredSize(new Dimension(640, 24));
        game.m_input.setBackground(Color.black);
        game.m_input.setForeground(Color.white);
        game.m_input.setCaretColor(Color.green);
        game.m_input.getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0, false), game.sendInputText);

        /*
         * game.m_last_output = new JTextPane();
         * game.m_last_output.setPreferredSize(new Dimension(640,48));
         * game.m_last_output.setBackground(Color.darkGray);
         * game.m_last_output.setForeground(Color.white);
         * game.m_last_output.setCaretColor(Color.green);
         * game.m_last_output.setEditable(false);
         */

        game.m_output = new JTextPane();
        game.m_output.setBackground(Color.darkGray);
        game.m_output.setForeground(Color.white);
        game.m_output.setCaretColor(Color.green);
        game.m_output.setEditable(false);

        game.m_inventory = new JTextPane();
        game.m_inventory.setBackground(Color.darkGray);
        game.m_inventory.setForeground(Color.white);
        game.m_inventory.setCaretColor(Color.green);
        game.m_inventory.setEditable(false);

        JFrame frame = new JFrame("2D Anchorhead");
        frame.setResizable(false);
        Box box = new Box(BoxLayout.Y_AXIS);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (game.m_last_output == null) {
            frame.setSize(648, 720);
        } else {
            frame.setSize(648, 780);
        }
        frame.add(box);
        box.add(game);
        box.add(Box.createVerticalStrut(2));
        box.add(game.m_input);
        box.add(Box.createVerticalStrut(2));
        if (game.m_last_output != null) {
            box.add(game.m_last_output);
            box.add(Box.createVerticalStrut(2));
        } // if
        {
            Box box2 = new Box(BoxLayout.X_AXIS);
            JScrollPane sp1 = new JScrollPane(game.m_output);
            sp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            sp1.setPreferredSize(new Dimension(440, 148));
            box2.add(sp1);
            JScrollPane sp2 = new JScrollPane(game.m_inventory);
            sp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            sp2.setPreferredSize(new Dimension(200, 148));
            box2.add(sp2);
            box.add(box2);
        }

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        {
            System.out.println("Initializing game...");

            game.init();

            try {
                while (!game.m_game.finished()) {
                    game.update();
                    game.repaint();
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        directgame.OutUserTrace(game.m_game);
        System.out.println("Time spent (seconds): " + secondsBetween(gameInit.getTime(),Calendar.getInstance().getTime()));
        System.exit(0);

    }

    void loadGame(String game) {
        // Load tiles:
        try {
            SAXBuilder sb = new SAXBuilder(false);
            Document jdoc = sb.build(game + "/tiles.xml");

            assert jdoc.getRootElement().getName().equals("tileset") :
                    "Invalid TileSet XML root"
                    + jdoc.getRootElement().getName();
            IFTileManager.loadFromXML(jdoc.getRootElement(), game);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            SAXBuilder sb = new SAXBuilder(false);
            Document jdoc = sb.build(game + "/game.xml");
            Document jdoc_pp = sb.build(game + "/story.xml");

            assert jdoc.getRootElement().getName().equals("IFGameState") :
                    "Invalid Game XML root"
                    + jdoc.getRootElement().getName();
            assert jdoc_pp.getRootElement().getName().equals("story") :
                    "Invalid Story XML root"
                    + jdoc_pp.getRootElement().getName();
            m_game = IFGameState.loadFromXML(jdoc.getRootElement(), game);
            m_story = IFStory.loadFromXML(jdoc_pp.getRootElement(), game);
            m_story_state = new IFStoryState(m_story);
            
            SimpleNLP.getObjectSynonyms(m_game, m_story);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Story state:\n" + m_story_state.toString());

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        if (m_game != null) {
            m_game.draw(m_focus_character, g2d);
            IFCharacter c = (IFCharacter) (m_game.containsObject(m_focus_character));
            m_inventory.setText(c.inventoryDescription());
        }
    }

    public void output(String text) {
        if (m_last_output != null) {
            m_output.setText(m_output.getText() + text + "\n");
            m_last_output.setText(text);
        } else {
            m_output.setText(text);
        }
    }

    public void init() {
        LinkedList<String> l = new LinkedList<String>();
        m_game.init(l);
        m_story.init(l);
        for (String s : l) {
            m_logger.println(s);
            output(s);
        }
        m_story.computeUserImportantActions(m_game);
        
        
        directgame = new Director();
    }

    public void update() {
        
        

        if (m_actions_to_enqueue.size() > 0) {
            while (m_actions_to_enqueue.size() > 0) {
                IFAction a = m_actions_to_enqueue.pop();
                m_game.enqueueAction(a, m_story_state);
            } // while 
        }

        if (m_story_state != null && m_game != null) {
            LinkedList<String> l = new LinkedList<String>();
            try {
                m_story_state.update(m_game, l);
                m_game.update(m_story_state, l);
            } catch (Exception e) {
                e.printStackTrace();
            }

            {
                String last_output = "";
                for (String s : l) {
                    m_logger.println(s);
                    last_output = last_output + s + "\n";

                }
                if (l.size() > 0) {
                    output(last_output);
                    // scroll output to the bottom:
                    Rectangle visible = m_output.getVisibleRect();
                    visible.y = m_output.getHeight() - visible.height;
                    m_output.scrollRectToVisible(visible);
                } // if
            }

        }
        
        // Unique drama management begins here
        
      boolean useHints;
      
      // option for using hints
      useHints=true;
        
      if(useHints)
      {
        directgame.updateGame(m_game);      
        directgame.MakeDecision(m_story, m_game, m_story_state);
        //directgame.MakeDecision(m_story, m_story_state);
        //directgame.Adapt();

        DoSomething(directgame.gameadapt);
      }
                                                     
    }
    
    public void DoSomething(GameAdapter ag)
    {
        
        if(ag.offerHint)
        {
            String text = null;
            if(ag.plotHint==null)
                text = "The hero shrugs at what to do next...";
            else
                text = ag.plotHint.getHint();
				
            if(m_actions_to_enqueue.contains(new IFAction("player","talk","player","reply",text)))
                return;
				
            m_actions_to_enqueue.add(new IFAction("player","talk","player","reply",text));
            
        }
    }
    
    public static double secondsBetween(Date date1, Date date2)
    {
        long timeDiff;
        double secondDiff = 0;
        timeDiff = date2.getTime()-date1.getTime();
        secondDiff = (double) timeDiff/1000;
        
        return secondDiff;
    }

}
