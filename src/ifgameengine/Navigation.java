package ifgameengine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.LinkedList;
import org.jdom.Element;

public class Navigation {

	class Link {
		public Point m_p1,m_p2;
		
		public Link(Point p1,Point p2) {
			m_p1 = p1;
			m_p2 = p2;
		}
	}
	
	LinkedList<Point> m_points = new LinkedList<Point>();
	LinkedList<Link> m_links = new LinkedList<Link>();
	
	public Navigation() {		
	}
	
	public Point getPoint(String id) throws Exception {
		for(Point p:m_points) {
			if (p.m_id.equals(id)) return p;
		}
		throw new Exception("Point not found: " + id);
	}
	
	public static Navigation loadFromXML(Element root,String path) throws Exception {
		Navigation n = new Navigation();
		
		for(Object o:root.getChildren("point")) {
			Element pe = (Element)o;
			
			n.m_points.add(new Point(pe.getAttributeValue("id"),Float.parseFloat(pe.getAttributeValue("x")),Float.parseFloat(pe.getAttributeValue("y"))));
		}
		for(Object o:root.getChildren("link")) {
			Element pe = (Element)o;
			
			n.m_links.add(n.new Link(n.getPoint(pe.getAttributeValue("p1")),n.getPoint(pe.getAttributeValue("p2"))));
		}
		return n;
	}	
	
	public Point getNextPositionSimple(Point startingPosition,Point destination,float speed) {
		
		Point dest = new Point(startingPosition.m_x,startingPosition.m_y);
		if (dest.m_x<destination.m_x-speed) {
			dest.m_x+=speed;
		} else {
			if (dest.m_x>destination.m_x+speed) {
				dest.m_x-=speed;
			} else {
				dest.m_x = destination.m_x;
			}
		}
		if (dest.m_y<destination.m_y-speed) {
			dest.m_y+=speed;
		} else {
			if (dest.m_y>destination.m_y+speed) {
				dest.m_y-=speed;
			} else {
				dest.m_y = destination.m_y;
			}
		}	
		
		if (dest.m_x == startingPosition.m_x &&
			dest.m_y == startingPosition.m_y) return null;
		
		return dest;
	}
	
	public LinkedList<Point> getPath(Point startingPosition,Point destinationPosition) throws Exception {
		Point origin_node = null /*, destination_node = null*/;
		
		{
			float min = 0;
			for(Point p:m_points) {
				float d = p.distance(startingPosition);
				if (origin_node==null || d<min) {
					min = d;
					origin_node = p;
				}
			}
/*			
			for(Point p:m_points) {
				float d = p.distance(destinationPosition);
				if (destination_node==null || d<min) {
					min = d;
					destination_node = p;
				}
			}
*/			
		}
		
		if (origin_node!=null && destinationPosition!=null) {
			return findNodeSequence(origin_node,destinationPosition);
		}
		
		return null;
	}	
	
	// Use Dijkstra's algorithm to find the ebst path:
	public LinkedList<Point> findNodeSequence(Point p1,Point p2) throws Exception {
		LinkedList<Point> path = new LinkedList<Point>();
		HashMap<String,Float> dist = new HashMap<String,Float>();
		HashMap<String,String> previous = new HashMap<String,String>();
		LinkedList<Point> unvisited = new LinkedList<Point>();
		LinkedList<Point> visited = new LinkedList<Point>();
		boolean terminate = false;

		for(Point p:m_points) {
			dist.put(p.m_id, Float.POSITIVE_INFINITY);
			previous.put(p.m_id, null);
		}
		dist.put(p1.m_id,0.0F);
		unvisited.addAll(m_points);
		while(!unvisited.isEmpty() && !terminate) {
			float minDist = 0.0f,d;
			Point u = null;
			for(Point p:unvisited) {
				d = dist.get(p.m_id);
				if (d!=Float.POSITIVE_INFINITY && (u==null || d<minDist)) {
					u=p;
					minDist = d;
				}
			}
			
			if (u!=null) {
				visited.add(u);
				unvisited.remove(u);

//				System.out.println("Considering " + u.m_id + ", " + unvisited.size() + " to go.");
				
				for(Link l:m_links) {
					Point v = null;
					if (l.m_p1.m_id.equals(u.m_id)) v = l.m_p2;
					if (l.m_p2.m_id.equals(u.m_id)) v = l.m_p1;
					
					if (v!=null) {
	
						float du = dist.get(u.m_id);
						float dv = dist.get(v.m_id);
						float alt = du + u.distance(v);
						
						if (alt<dv) {
							dist.put(v.m_id, alt);
							previous.put(v.m_id, u.m_id);						
						}
					}
				}
			} else {
				terminate = true;
			}
		}
		
//		for(Point p:m_points) {
//			System.out.println("Previous of " + p.m_id + " is " + previous.get(p.m_id));
//		}
		{
			float min = 0;
			Point destination_node = null;
			
			for(Point p:visited) {
				float d = p.distance(p2);
				if (destination_node==null || d<min) {
					min = d;
					destination_node = p;
				}
			}		
			
			path.addFirst(destination_node);
			while(destination_node!=null && destination_node!=p1) {
				destination_node = getPoint(previous.get(destination_node.m_id));
				path.addFirst(destination_node);
			}
		}
		
//		System.out.print("Node Sequence: ");
//		for(Point p:path) System.out.print(p.m_id + " ");
//		System.out.println("");
		return path;
	}
	
	public Point walkPath(Point startingPosition,LinkedList<Point> sequence,float speed) {
		LinkedList<Point> copy = new LinkedList<Point>();
		copy.addAll(sequence);
		for(Point p:copy) {
			Point p2 = getNextPositionSimple(startingPosition,p,speed);
			if (p2!=null) return p2;
			sequence.remove(p);
		}
		return null; 

	}
	
	public void draw(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		for(Point p:m_points) {
			g2d.drawOval((int)p.m_x+12, (int)p.m_y+12, 8, 8);
		}
		for(Link l:m_links) {
			g2d.drawLine((int)l.m_p1.m_x+16, (int)l.m_p1.m_y+16, (int)l.m_p2.m_x+16, (int)l.m_p2.m_y+16);
		}
	}		
	
}
