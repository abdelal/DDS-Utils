/**
 * 
 */
package de.danielsenff.radds.view.canvas;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JScrollPane;
import javax.swing.JViewport;


/**
 * @author danielsenff
 *
 */
public class ScrollCanvasListener implements MouseMotionListener, MouseWheelListener, KeyListener {

	
	private JScrollPane scrollViewPane;
	
	/**
	 * @param pane 
	 */
	public ScrollCanvasListener(JScrollPane pane) {
		this.scrollViewPane = pane;
	}

	int xOld = 0;
	int yOld = 0;
	
	public void mouseDragged(MouseEvent event) {
		drag(event);
	}

	/**
	 * 
	 */
	private void drag(MouseEvent event) {
		
		JViewport viewport = scrollViewPane.getViewport();
		Point position = viewport.getViewPosition();
		Rectangle rectViewport = viewport.getViewRect();
		
	
		// get new click coordinates
		int xNew = event.getX();
		int yNew = event.getY();
		
		// translation aka difference
		int deltaX = xOld - xNew;
		int deltaY = yOld - yNew;
		
		int x = position.x + deltaX;
		int y = position.y + deltaY;

		// boundaries
		int canvasWidth = (int) viewport.getViewSize().getWidth();
		int canvasHeight = (int) viewport.getViewSize().getHeight();
		
		if(rectViewport.width < canvasWidth) 
		{
			// left and top edge
			if (x <= 0) x = 0;
			
			// right edge
			int maxX = canvasWidth - rectViewport.width;
			if (x > maxX)	x = maxX;
			position.x = x;
		}
		if(rectViewport.height < canvasHeight) {
			if (y <= 0) y = 0;
			int maxY = canvasHeight - rectViewport.height;
			// bottom edge
			if (y > maxY)	y = maxY;
			position.y = y;
		}
		
		viewport.setViewPosition(position);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent event) {
		xOld = event.getX();
		yOld = event.getY();
	}

	
	
	/**
	 * Maximum factor for zooming
	 */
	public static final float UPPER_ZOOM_LIMIT = 20.0f;

	/**
	 * Minimum factor for zooming
	 */
	public static final float LOWER_ZOOM_LIMIT = 0.1f;
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent wheelEvent) {
		BICanvas canvas = ((BICanvas) wheelEvent.getSource());
		
		float originalZoomFactor = canvas.getZoomFactor();
		float zoomFactor = 1;
		int rectCanvasX = 0, rectCanvasY = 0;
		
		// zoom direction
		if(wheelEvent.getWheelRotation() < 0) {
			// increase zoom
			zoomFactor = originalZoomFactor + 0.2f;
			rectCanvasX = (int) (wheelEvent.getX() + wheelEvent.getX()*0.2);
			
			if (originalZoomFactor > UPPER_ZOOM_LIMIT) {
				zoomFactor = UPPER_ZOOM_LIMIT;
			}
		} else if(wheelEvent.getWheelRotation() > 0 ) {
			// decrease zoom
			zoomFactor = originalZoomFactor - 0.2f;
			rectCanvasY = (int) (wheelEvent.getY() + wheelEvent.getY()*0.2);
			if(zoomFactor < LOWER_ZOOM_LIMIT) {
				zoomFactor = LOWER_ZOOM_LIMIT;
			}
		}
		canvas.setZoomFactor(zoomFactor);
		
		// mouse position offset
		JViewport viewport = scrollViewPane.getViewport();
		Point position = viewport.getViewPosition();
		Rectangle viewRect = viewport.getViewRect();
		Dimension canvasDimension = canvas.getViewDimension();
		
		
		// get new scroll coordinates on canvas
		int xNew = wheelEvent.getX();
		int yNew = wheelEvent.getY();
		// get new scroll coordinates on viewport
		int xViewNew = wheelEvent.getX() - viewRect.x;
		int yViewNew = wheelEvent.getY() - viewRect.y;
		
		
		
		// translation aka centering
		int deltaX = position.x - xNew;
		int deltaY = position.y - yNew;
		
		double zoomOffsetX = wheelEvent.getX() / canvasDimension.getWidth();
		double zoomOffsetY = wheelEvent.getY() / canvasDimension.getHeight();
		
		int xView = rectCanvasX - xViewNew;
		int yView = rectCanvasY - yViewNew;
		
//		int xView = (int) ((canvasDimension.getWidth() - viewRect.width) * 1);
//		int yView = (int) ((canvasDimension.getHeight() - viewRect.height) * 1);
		
		// write coordinates back
		position.x = xView;
		position.y = yView;
		
		viewport.setViewPosition(position);
	}

	public void keyPressed(KeyEvent keyEvent) {
		BICanvas canvas = ((BICanvas) keyEvent.getSource());
		
		float originalZoomFactor = canvas.getZoomFactor();
		float zoomFactor = 1;
		
		// zoom direction
		if(keyEvent.getKeyChar() == '+') {
			// increase zoom
			zoomFactor = originalZoomFactor + 0.2f;
			
			if (originalZoomFactor > UPPER_ZOOM_LIMIT) {
				zoomFactor = UPPER_ZOOM_LIMIT;
			}
		} else if(keyEvent.getKeyChar() == '-') {
			// decrease zoom
			zoomFactor = originalZoomFactor - 0.2f;
			if(zoomFactor < LOWER_ZOOM_LIMIT) {
				zoomFactor = LOWER_ZOOM_LIMIT;
			}
		} else if(keyEvent.getKeyChar() == '0') {
			// decrease zoom
			zoomFactor = 1.0f;
		}
		
		canvas.setZoomFactor(zoomFactor);
	}

	public void keyReleased(KeyEvent arg0) {}

	public void keyTyped(KeyEvent arg0) {}

}
