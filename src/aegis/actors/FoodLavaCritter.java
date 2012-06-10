package aegis.actors;

import aegis.core.Actor;
import aegis.core.Stage;
import braincraft.Brain;

public class FoodLavaCritter extends FoodCritter
{
	public FoodLavaCritter(Brain b, Stage s)
	{
		super(b, s);
	}
	
	/**
	 * Gets data from the stage and converts it into inputs for the brain.
	 * @return inputs for the brain
	 */
	@Override
	protected double[] getBrainInputs()
	{
		double in[] = new double[6];
		
		in[0] = (stage.get(x,y,dir.getLeft()) instanceof Food) ? 1.0 : 0.0;
		in[1] = (stage.get(x,y,dir) instanceof Food) ? 1.0 : 0.0;
		in[2] = (stage.get(x,y,dir.getRight()) instanceof Food) ? 1.0 : 0.0;
		
		in[3] = (stage.get(x,y,dir.getLeft()) instanceof Lava) ? 1.0 : 0.0;
		in[4] = (stage.get(x,y,dir) instanceof Lava) ? 1.0 : 0.0;
		in[5] = (stage.get(x,y,dir.getRight()) instanceof Lava) ? 1.0 : 0.0;
		
		return in;
	}
	
	/**
	 * Takes the outputs of the brain and applies them to the simulation world.
	 * @param out 
	 */
	@Override
	protected void useBrainOutputs(double[] out)
	{
		if(out[0] > .5)
			turnLeft();
		
		if(out[1] > .5)
			moveForward();
		
		if(out[2] > .5)
			turnRight();
	}
	
	@Override
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
		else if(stage.get(x, y) instanceof Lava)
			score--;
		
		stage.add(x, y, a);
	}
}
