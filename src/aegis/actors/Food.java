package aegis.actors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import aegis.core.Actor;
import aegis.core.Aegis;
import aegis.core.Stage;

/**
 * 
 * @author Prad
 *
 */
public class Food extends Actor
{
	static BufferedImage image;
	
	static {
		//making graphics objects in headless mode throws errors
		//make the image once and store it statically
		if(!Aegis.headless && image == null)
		{
			//set up image
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			image = gd.getDefaultConfiguration().createCompatibleImage(Stage.CELL_SIZE, Stage.CELL_SIZE);
			Graphics2D g = (Graphics2D) image.getGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setStroke(new BasicStroke(3, BasicStroke.JOIN_MITER, BasicStroke.JOIN_MITER));

			//draw image
			int size = (int)(Stage.CELL_SIZE * .5);
			
			//draw background
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, Stage.CELL_SIZE, Stage.CELL_SIZE);

			//draw food circle
			g.setColor(Color.GREEN);
			g.fillOval((Stage.CELL_SIZE - size)/2, (Stage.CELL_SIZE - size)/2, size, size);
		}
	}
	
	public Food()
	{
		
	}
	
	@Override
	public void act()
	{
		//just sit there waiting to be eaten
	}

	@Override
	public BufferedImage getImage()
	{
		return image;
	}

}
