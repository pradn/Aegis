package aegis.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.Timer;

/**
 * Renders the environment in a window
 * 
 * @author Prad
 */
@SuppressWarnings("serial")
public class Renderer extends JFrame implements KeyListener
{
	private static int renderers=0;
	
	private Timer renderTimer;
	
	private Stage stage;
	private Experiment experiment;
	private int width;
	private int height;
	
	public boolean drawStage = true;
	public boolean drawHUD = true;
	public boolean drawGrid = true;
	
	private BufferedImage buffer;
	private Graphics2D graphic;
	private Color backgroundColor;
	
	private static final BasicStroke s = 
		new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);//default stroke

	/**
	 * Sets up the window for drawing.
	 * Uses double-buffering to prevent render artifacts.
	 * 
	 * @param st
	 * @param exp
	 */
	public Renderer(Stage st, Experiment exp)
	{
		super("Aegis");
		
		stage = st;
		experiment= exp;
		width = st.getWidth() * Stage.CELL_SIZE;
		height = st.getHeight() * Stage.CELL_SIZE;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setSize(width, height);
		setLocationByPlatform(true);

		setVisible(true);

		//compatible images are much faster
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		buffer = gd.getDefaultConfiguration().createCompatibleImage(width, height);
		graphic = (Graphics2D) buffer.getGraphics();

		graphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		/*graphic.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);*/
		
		backgroundColor = Color.BLACK;
		
		addKeyListener(this);
		renderers++;
	}

	/**
	 * Starts a timer that calls paint()
	 * @param fps
	 */
	public void run(int fps)
	{
		renderTimer = new Timer(
				1000/fps, 
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						repaint();
					}
				}
		);
		
		renderTimer.start();
	}
	
	/**
	 * Draws images on the screen every frame
	 */
	public void paint(Graphics screen)
	{
		graphic.setColor(backgroundColor);
		graphic.fillRect(0, 0, width, height);
		
		if(drawStage)
			stage.draw(graphic);
		
		if(drawGrid)
			stage.drawGrid(graphic);
		
		if(drawHUD)
			experiment.draw(graphic);
				
		screen.drawImage(buffer, 0, 0, null);
	}

	/**
	 * Handles key presses to toggle draw flags 
	 */
	@Override
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_Q)
		{
			drawStage=!drawStage;
		}
		else if(e.getKeyCode() == KeyEvent.VK_W)
		{
			drawHUD = !drawHUD;
		}
		else if(e.getKeyCode() == KeyEvent.VK_E)
		{
			drawGrid = !drawGrid;
		}
	}
	
	/**
	 * Escape to kill the program
	 */
	@Override
	public void keyReleased(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			renderers--;
			if(renderers>0)
				dispose();
			else 
				System.exit(0);
		}
		
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		
	}
}
