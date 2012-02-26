package ifgameengine;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import org.jdom.Element;


public class IFTileManager {
	static HashMap<String,BufferedImage> m_tiles = new HashMap<String,BufferedImage>();
	
	public static BufferedImage getTile(String name) {
		return m_tiles.get(name);
	}
	
	public static void loadFromXML(Element root,String path) throws IOException {
		for (Object o : root.getChildren("tile")) {
			Element e = (Element) o;

			String id = e.getAttributeValue("id");
			String src = e.getChildText("img");
			int x = Integer.parseInt(e.getChildText("x"));
			int y = Integer.parseInt(e.getChildText("y"));
			int w = Integer.parseInt(e.getChildText("w"));
			int h = Integer.parseInt(e.getChildText("h"));
			
	        File file = new File(src);
	        ImageInputStream iis = ImageIO.createImageInputStream(file);
	        BufferedImage img = ImageIO.read(iis);
	        
	        BufferedImage tgt = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	        Graphics2D g = tgt.createGraphics();
	        g.drawImage(img.getSubimage(x, y, w, h), 0, 0, null);
	        g.dispose();	        
	        
	        m_tiles.put(id,tgt);	
	        
	        
	        
		}		
	}	
	
}
