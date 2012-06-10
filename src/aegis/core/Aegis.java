package aegis.core;

import aegis.experiments.FoodExperiment;
import aegis.experiments.FoodLavaExperiment;

/**
 * 
 * make lava example
 * get output
 * aggregate
 * 
 * change to new braincraft
 * rerun analytics
 * 
 * brains left in pop
 * 
 */

/**
 * Main class.
 * Parses parameters and initializes appropriate experiments.
 * 
 * @author Prad
 */
public class Aegis 
{
	public static boolean logging=false;
	public static boolean debug=false;
	public static boolean headless = false;
	
	/**
	 * Main driver
	 * 
	 * @param args
	 * 
	 * Default parameters:
	 * "exp=1 n=1 w=30 h=30 s=10 nmr=.02 lmr=.04 wmr=.2 ldr=0 gen=100"
	 * 
	 * All parameters:
	 * "exp=1 n=1 w=30 h=30 s=10 nmr=.02 lmr=.04 wmr=.2 ldr=0 gen=100 hl seq log debug "
	 * 
	 * Parameters:
	 * exp=1	 : the number of the experiment to run
	 * n=1       : number of times to execute experiment
	 * w=30      : width of environment in cells 
	 * h=30      : height of environment incells
	 * s=15      : size of each cell in pixels
	 * hl        : enables headless mode - turns off graphics
	 * seq       : run the experiments one after another instead of concurrently
	 * log       : enables logging
	 * debug     : enables debug logging
	 * nmr=.02   : mutation rate of nodes in the evolved neural networks
	 * lmr=.04   : mutation rate of links in the evolved neural networks
	 * wmr=.2    : mutation rate of weights in the evolved neural networks
	 * ldr=0     : rate of links between nodes being disabled in the evolved neural networks
	 * pop=50    : number of brains to evaluate per generation
	 * gen=100   : number of generations to run the experiment
	 * 
	 */
	public static void main(String[] args)
	{	
		int experiment = 1;
		int trials = 1;
		boolean sequential = false;
		
		//assigns program parameters to variables
		for(String s: args)
		{
			//parameters with values
			if(s.contains("="))
			{
				String[] st = s.split("=");
				
				if(st[0].equals("exp"))
					experiment = Integer.parseInt(st[1]);
				else if(st[0].equals("n"))
					trials = Integer.parseInt(st[1]);
				
			}
			//boolean parameters
			else 
			{
				if(s.equals("seq"))
					sequential=true;
				else if(s.equals("log"))
					logging=true;
				else if(s.equals("debug"))
					debug=true;
				else if(s.equals("hl"))
					headless=true;
			}
		}		
		
		for(int i=0; i<trials; i++)
		{
			Experiment e=null;
			
			switch(experiment)
			{
			case 1: e = new FoodExperiment(args);
			break;
			
			case 2: e = new FoodLavaExperiment(args);
			break;
			}
			
			if(sequential)
				e.run();
			else
				new ConcurrentRunner(e).start();
		}
	}
	
	private static class ConcurrentRunner extends Thread
	{
		private Experiment e;
		public ConcurrentRunner(Experiment e)
		{
			this.e = e;
		}
		
		@Override
		public void run()
		{
			e.run();
		}
	}
}
