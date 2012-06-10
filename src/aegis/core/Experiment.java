package aegis.core;

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Random;

/**
 * Superclass of all experiments. Provides utility methods to reduce boilerplate in creating future experiments.
 * @author Prad
 *
 */
public class Experiment 
{
	//stage
	protected Stage stage;
	//renderer
	protected Renderer renderer;
	//log
	PrintStream log; 
	//debug log
	PrintStream debug;
	
	//experiment parameters
	protected int width = 30;
	protected int height = 30;
	
	protected double nodeMutationRate = 0.02;
	protected double linkMutationRate = 0.04;
	protected double weightMutationRate = 0.2;
	protected double linkDisableRate = 0;
	
	protected int popSize = 50;
	protected int generations = 100;
	
	public Experiment(String[] args)
	{
		//assigns program parameters to variables
		for(String s: args)
		{
			//parameters with values
			if(s.contains("="))
			{
				String[] st = s.split("=");
				
				if(st[0].equals("w"))
					width = Integer.parseInt(st[1]);
				else if(st[0].equals("h"))
					height = Integer.parseInt(st[1]);
				else if(st[0].equals("s"))
					Stage.CELL_SIZE = Integer.parseInt(st[1]);
				else if(st[0].equals("nmr"))
					nodeMutationRate = Double.parseDouble(st[1]);
				else if(st[0].equals("lmr"))
					linkMutationRate = Double.parseDouble(st[1]);
				else if(st[0].equals("wmr"))
					weightMutationRate = Double.parseDouble(st[1]);
				else if(st[0].equals("ldr"))
					linkDisableRate = Double.parseDouble(st[1]);
				else if(st[0].equals("pop"))
					popSize = Integer.parseInt(st[1]);
				else if(st[0].equals("gen"))
					generations = Integer.parseInt(st[1]);
			}
		}		
		
		stage = new Stage(width,height);
		
		if(!Aegis.headless)
			renderer = new Renderer(stage, this);
		
		//initializes streams to log to
		try
		{
			//creates random files to log to, you might want to change the file names
			long id = Math.abs(new Random().nextInt());
			if (Aegis.logging)
				log = new PrintStream(new File(id + "_aegis_log" + ".txt"));
			if(Aegis.debug)
				debug = new PrintStream(new File(id + "_aegis_debug" + ".txt"));
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	public void draw(Graphics2D graphic)
	{
				
	}
	
	public void run()
	{
		//brings up a window and starts drawing graphics
		if(renderer != null)
		{
			renderer.run(20);
		}
	}
	
	public void log(String s)
	{
		if(Aegis.logging)
			log.println(s);
	}
	
	public void debug(String s)
	{
		if(Aegis.debug)
			debug.println(s);
	}
	
	/**
	 * Closes resources
	 */
	@Override
	public void finalize()
	{
		if(Aegis.logging)
		{
			log.flush();
			log.close();
		}
		
		if(Aegis.debug)
		{
			debug.flush();
			debug.close();
		}
	}

}