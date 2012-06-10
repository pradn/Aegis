package aegis.experiments;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Random;

import aegis.actors.Food;
import aegis.actors.FoodCritter;
import aegis.core.Experiment;
import braincraft.Brain;
import braincraft.Braincraft;
import braincraft.Population;
import braincraft.TribePopulation;

/**
 * Simple experiment to see if hungry critters can evolve to get better at eating.
 * @author Prad
 *
 */
public class FoodExperiment extends Experiment
{
	Population pop;
	int maxFitness=0;
	int lastFitness=0;
	int avgFitness=0;
	int lastPopAvgFitness=0;

	Font font;

	/**
	 * Takes in a bunch of parameters, sets up the BrainCraft population
	 */
	public FoodExperiment(String[] args)
	{
		super(args);

		font = new Font(Font.SANS_SERIF, Font.BOLD, 12);

		pop = new TribePopulation(popSize, 3, 3);
		pop.nodeMutationRate = nodeMutationRate;
		pop.linkMutationRate = linkMutationRate;
		pop.weightMutationRate = weightMutationRate;
		pop.linkDisableRate = linkDisableRate;

	}

	/**
	 * Draws the info text at the top
	 */
	public void draw(Graphics2D g)
	{
		g.setColor(new Color(0,0,0,150));
		g.fillRect(0, 0, 150, 110);

		g.setColor(Color.WHITE);
		g.setFont(font);
		

		g.drawString("Critter: " + cn, 8, 40);
		g.drawString("Eaten: " + lastFitness, 8, 55);
		g.drawString("MaxFitness: " + maxFitness, 8, 70);
		g.drawString("PopAvgFitness: " + avgFitness, 8, 85);
		g.drawString("LastPopAvgFitness: " + lastPopAvgFitness, 8, 100);

	}

	@Override
	public void run() 
	{
		super.run();
		//Braincraft.gatherStats = true;

		//runs the brain evolution for a number of generations
		//calls evaluate(b) with each of the brains that it generates in the process 
		double fitness = 0;
		int counter = 0;
		while (counter < generations * popSize) {
			Brain b = pop.getBrain();
			fitness = evaluate(b);
			pop.reportFitness(b, fitness);
			counter++;
		}
		//Braincraft.writeStats("stats.txt");
	}

	//critter number
	int cn = 0;

	//critter's number in the population
	int cpopNum = 1;

	//total of a population's fitnesses
	int fitTotal=0;

	/**
	 * Uses a random seed to run a simulation of one critter.
	 * This method is called whenever a new brain is generated.
	 */
	public double evaluate(Brain b)
	{
		int seed = new Random().nextInt();
		Random r = new Random(seed);

		return runSimulation(b, r);
	}

	/**
	 * Simulates one critter. If the critter's fitness passes a threshold, it's actions are replayed. 
	 * 
	 * @param b
	 * @param r
	 * @return
	 */
	protected int runSimulation(Brain b, Random r)
	{
		stage.clear();

		//randomly sprinkle about 300 food
		for(int i=0; i < 300; i++)
		{
			Food f = new Food();
			stage.addRandomUnsafe(f, r);
		}

		//create critter
		FoodCritter c = new FoodCritter(b, stage);
		stage.addRandomUnsafe(c, r);

		//go around for a while
		for (int i = 0; i < 100; i++)
		{
			c.act();

			if(renderer != null && renderer.drawStage)
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
			fitTotal = 0;
			cpopNum=1;
		}
		else 
			cpopNum++;

		//store max fitness
		if(c.fitness() > maxFitness)
			maxFitness = c.fitness();

		lastFitness = c.fitness();
		fitTotal += c.fitness();
		avgFitness = fitTotal/cpopNum;

		cn++;
		
		log(cn + ", " + c.fitness() + ", " + maxFitness + ", " + lastPopAvgFitness);

		return c.fitness();
	}

}
