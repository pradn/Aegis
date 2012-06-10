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
import aegis.core.Direction;
import aegis.core.Stage;
import braincraft.Brain;

public class FoodCritter extends Actor
{
	//stage
	protected Stage stage;
	//brain
	protected Brain brain;
	//direction
	protected Direction dir;
	//food eaten
	protected int score;
	
	BufferedImage image;
	Graphics2D g;
	
	public FoodCritter(Brain b, Stage s)
	{
		brain = b;
		stage = s;
		dir = Direction.NORTH;
		score=0;
		
		if(!Aegis.headless)
		{
			//set up image
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			image = gd.getDefaultConfiguration().createCompatibleImage(Stage.CELL_SIZE, Stage.CELL_SIZE);
			g = (Graphics2D) image.getGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setStroke(new BasicStroke(3, BasicStroke.JOIN_MITER, BasicStroke.JOIN_MITER));

			updateImage();
		}
	}
	
	public void updateImage()
	{
		int size = (int)(Stage.CELL_SIZE * .75) ;
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, Stage.CELL_SIZE, Stage.CELL_SIZE);
		
		g.setColor(Color.CYAN);
		g.fillOval((Stage.CELL_SIZE - size)/2, (Stage.CELL_SIZE - size)/2, size, size);
		
		g.setColor(Color.RED);
		g.drawLine(Stage.CELL_SIZE/2, Stage.CELL_SIZE/2, 
				Stage.CELL_SIZE/2 + dir.getX() * (int)(Stage.CELL_SIZE * .45),
				Stage.CELL_SIZE/2 + dir.getY() * (int)(Stage.CELL_SIZE * .45));
	}
	
	@Override
	public BufferedImage getImage()
	{
		return image;
	}
	
	@Override
	public void act() 
	{
		double in[]	 = getBrainInputs();
		
		double out[]= new double[3];
		for (int i = 0; i < 5; i++)
		{
			out = brain.pumpNet(in);
		}
		
		useBrainOutputs(out);
		
		if(!Aegis.headless)
			updateImage();
	}
	
	/**
	 * Gets data from the stage and converts it into inputs for the brain.
	 * @return inputs for the brain
	 */
	protected double[] getBrainInputs()
	{
		double in[] = new double[3];
		
		in[0] = (stage.get(x,y,dir.getLeft()) instanceof Food) ? 1.0 : 0.0;
		in[1] = (stage.get(x,y,dir) instanceof Food) ? 1.0 : 0.0;
		in[2] = (stage.get(x,y,dir.getRight()) instanceof Food) ? 1.0 : 0.0;
		
		return in;
	}
	
	/**
	 * Takes the outputs of the brain and applies them to the simulation world.
	 * @param out 
	 */
	protected void useBrainOutputs(double[] out)
	{
		if(out[0] > .5)
			turnLeft();
		
		if(out[1] > .5)
			moveForward();
		
		if(out[2] > .5)
			turnRight();
	}
	
	protected void moveForward()
	{
		int dx = x + dir.getX();
		int dy = y + dir.getY();
		
		Actor a = stage.remove(x, y);
		
		if(dx < 0)
			x = stage.getWidth() - 1;
		else if (dx >= stage.getWidth())
			x = 0;
		else 
			x = dx;
		
		if(dy < 0)
			y = stage.getHeight() - 1;
		else if (dy >= stage.getHeight())
			y = 0;
		else 
			y = dy;
		
		if(stage.get(x, y) instanceof Food)
			score++;
		
		stage.add(x, y, a);
	}
	
	protected void turnLeft()
	{
		dir = dir.getLeft();
	}

	protected void turnRight()
	{
		dir = dir.getRight();
	}
	
	public int fitness()
	{
		return score;
	}
}
