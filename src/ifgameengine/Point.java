package ifgameengine;

class Point {
	public String m_id;
	public float m_x,m_y;
	
	public Point(float x,float y) {
		m_id = null;
		m_x = x;
		m_y = y;
	}
	
	public Point(String id,float x,float y) {
		m_id = id;
		m_x = x;
		m_y = y;
	}
	
	public float distance(Point p) {
		return (float)Math.sqrt((m_x-p.m_x)*(m_x-p.m_x)+(m_y-p.m_y)*(m_y-p.m_y));
	}
}
