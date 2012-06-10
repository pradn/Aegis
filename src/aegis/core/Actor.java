package aegis.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Superclass for game objects.
 * @author Prad
 *
 */
public abstract class Actor 
{
	//location in grid
	protected int x;
	protected int y;
	
	public abstract void act();
	public abstract BufferedImage getImage();
	
}
