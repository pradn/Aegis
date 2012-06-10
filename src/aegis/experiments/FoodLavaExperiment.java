package aegis.experiments;

import java.util.Random;

import aegis.actors.Food;
import aegis.actors.FoodLavaCritter;
import aegis.actors.Lava;
import braincraft.Brain;
import braincraft.TribePopulation;

/**
 * Simple experiment to see if hungry critters can evolve to get better at eating.
 * @author Prad
 *
 */
public class FoodLavaExperiment extends FoodExperiment
{	
	/**
	 * Takes in a bunch of parameters, sets up the BrainCraft population
	 */
	public FoodLavaExperiment(String[] args)
	{
		super(args);
		
		//only difference from FoodExperiment is number of inputs
		pop = new TribePopulation(popSize, 6, 3);
		pop.nodeMutationRate = nodeMutationRate;
		pop.linkMutationRate = linkMutationRate;
		pop.weightMutationRate = weightMutationRate;
		pop.linkDisableRate = linkDisableRate;
		
	}
	
	/**
	 * Simulates one critter. If the critter's fitness passes a threshold, it's actions are replayed. 
	 * 
	 * @param b
	 * @param r
	 * @return
	 */
	@Override
	protected int runSimulation(Brain b, Random r)
	{
		stage.clear();
		
		//randomly sprinkle about 150 food
		for(int i=0; i < 150; i++)
		{
			Food f = new Food();
			stage.addRandomUnsafe(f, r);
		}
		
		//randomly sprinkle about 150 lava
		for(int i=0; i < 150; i++)
		{
			Lava f = new Lava();
			stage.addRandomUnsafe(f, r);
		}
		
		//create critter
		FoodLavaCritter c = new FoodLavaCritter(b, stage);
		stage.addRandomUnsafe(c, r);
		
		//go around for a while
		for (int i = 0; i < 100; i++)
		{
			c.act();
			
			if((renderer != null && renderer.drawStage))
			{
				lastFitness=c.fitness();
				
				try
				{
					//makes the thread sleep so we can see what's going on
					Thread.sleep(50);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		//reset population counter and calculate population stats
		if(cpopNum == popSize)
		{
			lastPopAvgFitness = fitTotal/popSize;
			//System.out.println("lastPop: " + lastPopAvgFitness);
			fitTotal = 0;
			cpopNum=1;
		}
		else 
			cpopNum++;

		//store max fitness
		if(c.fitness() > maxFitness)
		{
			maxFitness = c.fitness();
		}

		lastFitness = c.fitness();
		fitTotal += c.fitness();
		avgFitness = fitTotal/cpopNum;

		cn++;

		log(cn + ", " + c.fitness() + ", " + maxFitness + ", " + lastPopAvgFitness);
		
		return c.fitness();
	}

}
