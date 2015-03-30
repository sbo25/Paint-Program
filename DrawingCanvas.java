import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import java.awt.Toolkit;
import java.util.*;

/**  This class represents the painting canvas and implements all relevant
 * functionality. */
public class DrawingCanvas extends JPanel implements MouseListener, MouseMotionListener  {
	/** This is useful for creating custom cursors. */
	private static Toolkit tk = Toolkit.getDefaultToolkit();
	
	private final Color defColor= Color.BLACK; // Default foreground color
	
	private BufferedImage img; // The image.
    private int width;  // width of the image
	private int height;  // height of the image
	
	private PaintGUI window; // main window of the program
	
	private Tool activeTool; // the active tool.
	private int toolSize; // size of the tool.
	private double half; // half = toolSize/2.0
	
	private Point2D.Double pMouse; // Position of mouse
    private Point2D.Double prevMousePos; // previous mouse position (used to interpolate)
	
	private boolean pointGiven;       // State for line drawing. */
	private Point2D.Double linePoint; // State for line drawing.

	
	private boolean centerGiven;  // State for circle drawing
	private Point2D.Double center; // State for circle drawing
	
	private Color color;  // Foreground color (used for drawing).
	private Color backColor; // Background color (used for erasing).

	/** Random generator for airbrush. */
	private Random gen= new Random(System.currentTimeMillis());

	/**  Constructor: a new drawing pane.
	 * 
	 * @param window Main window of application.
	 * @param width Width of image.
	 * @param height Height of image.
	 * @param bckColor Background color.
	 * @param toolSize Tool size.
	 */
	public DrawingCanvas(PaintGUI window, int width, int height, Color bckColor, int toolSize) {
		this.window= window;
		this.width= width;
		this.height= height;
		this.activeTool= null;
		setToolSize(toolSize);
		
		// Create image with background color bckColor
		img= new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d= (Graphics2D) img.getGraphics();
		g2d.setColor(bckColor);
		g2d.fillRect(0, 0, width, height);
		
		color= defColor;
		backColor= bckColor;
		
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	/** Set the foreground color to c.
	 * Throw an IllegalArgumentException if c is null */
	public void setColor(Color c) {
		if (c == null)
			throw new IllegalArgumentException();
		
		color= c;
		
		if (activeTool == Tool.LINE  &&  pointGiven) {
			repaint();
		}
		if (activeTool == Tool.CIRCLE  &&  centerGiven) {
			repaint();
		}
	}
	
	/** Set the background color to c.
     * Throw an IllegalArgumentException if c is null */
	public void setBackColor(Color c) {
		if (c == null)
			throw new IllegalArgumentException();
		
		backColor= c;
	}
	
	/** return the Foreground color.  */
	public Color getColor() {
		return color;
	}
	
	/** Return the Background color. */
	public Color getBackColor() {
		return backColor;
	}
	
	/** Return the image. */
	public BufferedImage getImg() {
		return img;
	}
	
	/** Return the tool size. */
	public int getToolSize() {
		return toolSize;
	}
	
	/** Set the tool size to v+1.
	 * Throw an IllegalArgumentException if v < 0. */
	public void setToolSize(int v) {
		if (v < 0)
			throw new IllegalArgumentException("setToolSize: v < 0");
		toolSize= v+1;
		half= toolSize / 2.0;
	}
	
	/** Create new blank image of width w and height h with
	 * background color c. */
	public void newBlankImage(int w, int h, Color c) {
		width= w;
		height= h;
		
		// reset line/circle state
		pointGiven= false;
		centerGiven= false;
		
		img= new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d= (Graphics2D) img.getGraphics();
		g2d.setColor(c);
		g2d.fillRect(0, 0, w, h);
		
		repaint();
		revalidate();
	}
	
	/** Change the image to img. */
	public void newImage(BufferedImage img) {
		System.out.println("newImage");
		
		// reset line/circle state
		pointGiven= false;
		centerGiven= false;

		width= img.getWidth();
		height= img.getHeight();
		this.img= img;
		
		repaint();
		revalidate();
	}
	

	/** Return the dimension of this image. */
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}
	
	/** Paint this component using g. */
	public void paintComponent(Graphics g) {
		System.out.println("Paint drawing pane.");
		
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		// Draw a border around the image.
		int z= 0;
		for (int i= 0; i<5; i++) {
			Color c= new Color(z,z,z);
			g2d.setColor(c);
			g2d.drawLine(0, height+i, width+i, height+i);
			g2d.drawLine(width+i, 0, width+i, height+i);
			z += 63;
		}
		g2d.drawImage(img, null, 0, 0);
		
		// TODO: Implement me!
		
		// HINTS:
		// There are potentially several things to do here.
		// You definitely need to paint the image.
		// It is also possible you want to draw things that are in the process of drawing:
		// e.g. lines & circles that are not actually drawn on the image until the process
		// of specifying the line/circle is complete.
		
		
		//Point start = new Point(e.getPoint());
		
		g2d.setColor(color);
		if (pointGiven) {
			g2d.setStroke(new BasicStroke(toolSize));
			g2d.draw(new Line2D.Double(linePoint.x, linePoint.y, pMouse.x, pMouse.y));
			repaint();
		}
		if (centerGiven) {
			g2d.setStroke(new BasicStroke(toolSize));
			double radius = center.distance(pMouse);
			g2d.draw(new Ellipse2D.Double(center.x-radius, center.y-radius, 2*radius, 2*radius));
			repaint();
		}
			
		
		
	}
	
	/** Return the active tool. */
	public Tool getActiveTool() {
		return activeTool;
	}

	/** Set the active tool to t. */
	public void setActiveTool(Tool t) {
		// reset line/circle state
		pointGiven= false;
		centerGiven= false;
		
		// Change cursor.
		if (t == Tool.PENCIL) {
			Point hotspot= new Point(2,30);
			Image cursorImage= tk.getImage("pencil-cursor.png");
			Cursor cursor= tk.createCustomCursor(cursorImage, hotspot, "Custom Cursor");
			setCursor(cursor);
		}
		else if (t == Tool.ERASER) {
			Point hotspot= new Point(5,27);
			Image cursorImage= tk.getImage("eraser-cursor.png");
			Cursor cursor= tk.createCustomCursor(cursorImage, hotspot, "Custom Cursor");
			setCursor(cursor);
		}
		else if (t == Tool.COLOR_PICKER) {
			Point hotspot= new Point(9,23);
			Image cursorImage= tk.getImage("picker-cursor.png");
			Cursor cursor= tk.createCustomCursor(cursorImage, hotspot, "Custom Cursor");
			setCursor(cursor);
		}
		else if (t == Tool.AIRBRUSH) {
			Point hotspot= new Point(1,25);
			Image cursorImage= tk.getImage("airbrush-cursor.png");
			Cursor cursor= tk.createCustomCursor(cursorImage, hotspot, "Custom Cursor");
			setCursor(cursor);
		}
		else if (t == Tool.LINE) {
			Point hotspot= new Point(0,0);
			Image cursorImage= tk.getImage("line-cursor.png");
			Cursor cursor= tk.createCustomCursor(cursorImage, hotspot, "Custom Cursor");
			setCursor(cursor);
		}
		else if (t == Tool.CIRCLE) {
			Point hotspot= new Point(16,16);
			Image cursorImage= tk.getImage("circle-cursor.png");
			Cursor cursor= tk.createCustomCursor(cursorImage, hotspot, "Custom Cursor");
			setCursor(cursor);
		}
		else {
			System.err.println("setActiveTool " + t);
		}

		activeTool= t;		
	}
	
	public void mouseClicked(MouseEvent e) {
		// Nothing to do here.
	}
	
	public void mouseEntered(MouseEvent e) {
		// Nothing to do here.
	}
	
	/** Update the position of the mouse to the position given by e. */
	private void updateMousePosition(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		// center of pixel
		pMouse= new Point2D.Double(x+0.5, y+0.5);
	}
	
	/** Process the press of the mouse, given by e. */
	public void mousePressed(MouseEvent e) {
		updateMousePosition(e);
		System.out.println("mousePressed: " + pMouse + ", active tool: " + getActiveTool());
		
		Graphics2D g2d= (Graphics2D) img.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // anti-aliasing
		g2d.setColor(color);
		if (activeTool == Tool.PENCIL) {
			System.out.println("mousePressed: pencil");
			
			// TODO:
			// Draw a square of size (toolSize x toolSize) filled with the current foreground color.
			// Its center should be at the position of the mouse.
			
			g2d.fillRect(e.getX()-toolSize/2, e.getY()-toolSize/2, toolSize, toolSize);
			repaint();
		}
		else if (activeTool == Tool.ERASER) {
			System.out.println("mousePressed: eraser");
			// Paint with the active background color.

			// TODO:
			// Draw a square of size (toolSize x toolSize) filled with the current background color.
			// Its center should be at the position of the mouse.
			g2d.setColor(backColor);
			g2d.fillRect(e.getX()-toolSize/2, e.getY()-toolSize/2, toolSize, toolSize);
			
		}
		else if (activeTool == Tool.COLOR_PICKER) {
			System.out.println("mousePressed: pick color");

			// TODO: Pick the color of the pixel the mouse is currently over.			
			// Left mouse button pressed: pick new foreground color
			// Right mouse button pressed: pick new background color
			if (e.getButton() == MouseEvent.BUTTON1) {
				color = new Color(img.getRGB(e.getX(), e.getY()));
				window.updateColor();
			}
			if (e.getButton() == MouseEvent.BUTTON3) {
				backColor = new Color(img.getRGB(e.getX(), e.getY()));
				window.updateBackColor();
			}
			
		}
		else if (activeTool == Tool.AIRBRUSH) {
			System.out.println("mousePressed: airbrush");
			
			// TODO:
			// Airbrush with the current foreground color in an area of size (toolSize x toolSize)
			// centered at the current position of the mouse.
			// HINT: Draw a few random pixels within the specified area.
			// You are free to choose any distribution you like. 
			
			for (int i=0; i<=75; i++) {
				int x = (int) Math.floor(Math.random()*toolSize);
				int y = (int) Math.floor(Math.random()*toolSize);
				
				g2d.fillRect(e.getX()+x-toolSize/2, e.getY()+y-toolSize/2, 1, 1);
			}
			repaint();
		}
		else if (activeTool == Tool.LINE){
			System.out.println("mousePressed: line");

			// TODO:
			// May need to draw a line.
			// 
			// HINT:
			// One way to specify a line is by pressing the mouse twice to give the two endpoints
			// of the line. So, in the first press you just save the location where this
			// occurred and in the second one you do the actual drawing on the image.
			// 
			// After the first press, the user should be able to see a "tentative" line,
			// while they are moving the mouse around to find the spot the want for the
			// second endpoint.
			
			pointGiven = !(pointGiven);
			if (pointGiven) {
				linePoint = pMouse;
				repaint();
			}
			else {
				g2d.setStroke(new BasicStroke(toolSize));
				g2d.draw(new Line2D.Double(linePoint.x, linePoint.y, pMouse.x, pMouse.y));
				repaint();
			}
			
		}
		else if (activeTool == Tool.CIRCLE){
			System.out.println("mousePressed: circle");
			
			// TODO:
			// May need to draw a circle.
			// 
			// HINT:
			// One way to specify a circle is by pressing the mouse twice to give the center
			// and one point that belongs to the circle.
			// 
			// After the first press, the user should be able to see a "tentative" circle,
			// while they are moving the mouse around to find the spot the want for the
			// second endpoint.
			centerGiven = !(centerGiven);
			if (centerGiven) {
				center = pMouse;
				repaint();
			}
			else {
				g2d.setStroke(new BasicStroke(toolSize));
				double radius = center.distance(pMouse);
				g2d.draw(new Ellipse2D.Double(center.x-radius, center.y-radius, 2*radius, 2*radius));
				repaint();
			}
		}
		else {
			System.err.println("Unknown tool: " + activeTool);
		}
		
		// set prevMousePos
		prevMousePos= pMouse;
	}
	
	public void mouseExited(MouseEvent e) {
		// Nothing to do here.
	}
	
	public void mouseReleased(MouseEvent e) {
		// End of drawing, reset prevMousePos.
		prevMousePos= null;
	}

	/** Draw a certain polygon that encloses points from and to, which are
	 * expected to be opposite corners of a square. half is expected to be
	 * half the width/height of the interpolated square. You have to execute
	 * this by hand to see the polygon that is being drawn.
	 * 
	 * YOU DO NOT HAVE TO USE THIS METHOD IF YOU DON'T WANT TO.
	 * 
	 * If you choose to use it, you need to read it and understand it.
	 * This might be useful for drawing with the pencil and for erasing.
	 *
	 * @param from Start point.
	 * @param to End point.
	 * @param half Half the width/height of the interpolated square.
	 * @return
	 */
	private static Path2D.Double createPolygon(Point2D.Double from, Point2D.Double to, double half) {
		Path2D.Double polygon= new Path2D.Double();
		
		// W.l.o.g. from.y <= to.y
		if (from.y > to.y) {
			Point2D.Double tmp= from;
			from= to;
			to= tmp;
		}
		
		// So, there are two cases to examine:
		// (1) from.x <= to.x
		// (2) from.x > to.x
		boolean fromXLess = from.x <= to.x;
		
		// Start point
		double startX = from.x-half;
		double startY = from.y-half;
		
		// 6 points: A,B,C,D,E,F
		Point2D.Double pA = new Point2D.Double(startX, startY);
		Point2D.Double pB = new Point2D.Double(from.x+half, startY);
		
		Point2D.Double pC;
		if (fromXLess)
			pC = new Point2D.Double(to.x+half, to.y-half);
		else
			pC = new Point2D.Double(from.x+half, from.y+half);
		
		Point2D.Double pD = new Point2D.Double(to.x+half, to.y+half);
		Point2D.Double pE = new Point2D.Double(to.x-half, to.y+half);
		
		Point2D.Double pF;
		if (fromXLess)
			pF = new Point2D.Double(startX, from.y+half);
		else
			pF = new Point2D.Double(to.x-half,to.y-half);
		
		// Draw the polygon
		polygon.moveTo(pA.x,pA.y);
		polygon.lineTo(pB.x,pB.y);
		polygon.lineTo(pC.x,pC.y);
		polygon.lineTo(pD.x,pD.y);
		polygon.lineTo(pE.x,pE.y);
		polygon.lineTo(pF.x,pF.y);
		polygon.lineTo(pA.x,pA.y);
		
		return polygon;
	}
	
	/** Process the dragging of the mouse given by e. */
	public void mouseDragged(MouseEvent e) {
		updateMousePosition(e);
		System.out.println("mouseDragged: " + pMouse + ", active tool: " + activeTool);		
		
		Graphics2D g2d= (Graphics2D) img.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		g2d.setColor(color);
		if (activeTool == Tool.PENCIL) {
			// TODO:
			// When the active tool is the pencil and the mouse is pressed and
			// dragged around, the trace of the mouse movement should be drawn.
			// 
			// HINT:
			// Notice that the mouse dragged events are not necessarily close to
			// each other (e.g. if you move the mouse fast). You have to interpolate
			// somehow. It is your work to find out exactly how.
			
			//g2d.fillRect(e.getX()-toolSize/2, e.getY()-toolSize/2, toolSize, toolSize);
			
			Path2D.Double path = DrawingCanvas.createPolygon(prevMousePos, pMouse, half);
			path.closePath();
			g2d.fill(path);
			repaint();
		}
		else if (activeTool == Tool.ERASER) {
			// TODO:
			// When the active tool is the eraser and the mouse is pressed and
			// dragged around, the trace of the mouse movement should be drawn
			// (with the background color).
			g2d.setColor(backColor);
			//g2d.fillRect(e.getX()-toolSize/2, e.getY()-toolSize/2, toolSize, toolSize);
			
			Path2D.Double path = DrawingCanvas.createPolygon(prevMousePos, pMouse, half);
			path.closePath();
			g2d.fill(path);
			repaint();
		}
		else if (activeTool == Tool.COLOR_PICKER) {
			// Nothing to do here.
		}
		else if (activeTool == Tool.AIRBRUSH) {
			// TODO:
			// You have to draw, but you do not need to interpolate.
			mousePressed(e);
		}
		else {
			System.err.println("active tool: " + activeTool);
		}
		
		// update prevMousePos
		prevMousePos= pMouse;
	}
	
	public void mouseMoved(MouseEvent e) {
		updateMousePosition(e);
		
		// TODO: Implement me!
		window.setMousePosition(e.getX(), e.getY());
	}

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
}
