package aegis.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Holds information about the environment.
 * Contains a grid of Actors. Any Actor can be placed on the Stage.
 * @author Prad
 */
public class Stage 
{
	public static int CELL_SIZE= 15;
	
	private final int width_cells;
	private final int height_cells;
	private final int width_pixels;
	private final int height_pixels;
	
	private Actor[][] grid;
	
	private Color gridColor;
	private BasicStroke gridStroke;
	
	public Stage(int w, int h)
	{
		width_cells=w;
		height_cells=h;
		width_pixels=width_cells*CELL_SIZE;
		height_pixels=height_cells*CELL_SIZE;
		
		grid = new Actor[w][h];
		gridColor= Color.GRAY;
		gridStroke= new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);//default stroke
	}
	
	/**
	 * Draws all of the actors on the grid by calling their draw() method
	 * @param g
	 */
	public void draw(Graphics2D g)
	{
		//draw actors
		for (int i = 0; i < width_cells; i++)
		{
			for (int j = 0; j < height_cells; j++)
			{
				Actor a = grid[i][j];
				if(a != null)
				{
					g.drawImage(a.getImage(), i*CELL_SIZE, j*CELL_SIZE, Color.BLACK, null);			
				}
			}
		}
	}
	
	public void drawGrid(Graphics2D g)
	{
		//draw grid
		g.setColor(gridColor);
		g.setStroke(gridStroke);
		
		for(int i=0; i<width_pixels; i+=CELL_SIZE)
			g.drawLine(i, 0, i, height_pixels);
		for(int i=0; i<height_pixels; i+=CELL_SIZE)
			g.drawLine(0, i, width_pixels, i);
	}
	
	/**
	 * Adds an actor to the specified location, overriding anything that exists there
	 * @param x
	 * @param y
	 * @param a
	 */
	public void add(int x, int y, Actor a) 
	{
		grid[x][y]=a;
		a.x=x;
		a.y=y;
	}
	
	/**
	 * Adds an actor to a random unoccupied location on the stage
	 * @param a
	 */
	public void addRandom(Actor a)
	{
		int seed = new Random().nextInt();
		Random r = new Random(seed);
		
		int rx=0, ry=0;
		do
		{
			rx = r.nextInt(width_cells);
			ry = r.nextInt(height_cells);
		}
		while(grid[rx][ry]!=null);
		
		grid[rx][ry]=a;
		a.x=rx;
		a.y=ry;
	}
	
	/**
	 * Adds an actor to a random location on the stage, overriding anything present.
	 * @param a
	 */
	public void addRandomUnsafe(Actor a, Random r)
	{
		int rx = r.nextInt(width_cells); 
		int ry = r.nextInt(height_cells); 
		grid[rx][ry]=a;
		
		a.x=rx;
		a.y=ry;
	}
	
	public Actor get(int x, int y)
	{
		if(x<0 || x >= width_cells || y<0 || y >= height_cells)
			return null;
		
		return grid[x][y];
	}
	
	/**
	 * Gets what exists one square in the dir direction from (x,y)
	 * 
	 * @param x
	 * @param y
	 * @param dir
	 * @return
	 */
	public Actor get(int x, int y, Direction dir)
	{
		int dx = x + dir.getX();
		int dy = y + dir.getY();
		
		if(dx < 0)
			x = width_cells - 1;
		else if (dx >= width_cells)
			x = 0;
		else 
			x = dx;
		
		if(dy < 0)
			y = height_cells - 1;
		else if (dy >= height_cells)
			y = 0;
		else 
			y = dy;
		
		return grid[x][y];
	}
	
	/**
	 * Removes actor from the stage
	 * @param x
	 * @param y
	 * @return
	 */
	public Actor remove(int x, int y)
	{
		Actor a = grid[x][y];
		grid[x][y] = null;
		return a;
	}	
	
	public int getWidth()
	{
		return width_cells;
	}
	
	public int getHeight()
	{
		return height_cells;
	}
	
	public void clear()
	{
		grid = new Actor[width_cells][height_cells];
	}
}
