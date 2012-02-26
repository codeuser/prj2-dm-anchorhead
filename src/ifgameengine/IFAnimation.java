package ifgameengine;

import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;

public class IFAnimation {
	class IFAnimationFrame {
		String m_graphic = null;
		int m_time = 1;
	}

	List<IFAnimationFrame> m_animation = new LinkedList<IFAnimationFrame>();
	int m_currentTime = 0;
	int m_length = 1;
	
	public IFAnimation() {
		m_currentTime = 0;
	}
	
	public IFAnimation(IFAnimation a) {
		m_animation = a.m_animation;
		m_currentTime = a.m_currentTime;
		m_length = a.m_length;
	}
	
	public static IFAnimation loadFromXML(Element root,String path) {
		IFAnimation a = new IFAnimation();
		
		a.m_length=0;
		
		for(Object o:root.getChildren("frame")) {
			Element e = (Element)o;
			IFAnimationFrame f = a.new IFAnimationFrame();
			f.m_graphic = e.getValue();
			f.m_time = Integer.parseInt(e.getAttributeValue("time"));
			a.m_animation.add(f);
			a.m_length+=f.m_time;
		}
		
		if (a.m_length==0) a.m_length=1;
		
		return a;
	}
	
	public void update() {
		m_currentTime++;
		if (m_currentTime>=m_length) m_currentTime=0;
	}
	
	public void reset() {
		m_currentTime=0;
	}
	
	public String getCurrentFrame() {
		int accumTime = 0;
		for(IFAnimationFrame f:m_animation) {
			accumTime+=f.m_time;
			if (accumTime>m_currentTime) return f.m_graphic;
		}
		
		return null;
	}
	
}
