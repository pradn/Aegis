package aegis.core;

/**
 * Enum for holding information about a direction.
 * This code generates 11 class files...
 * 
 * @author Prad
 *
 */
public enum Direction
{
	NORTH 
	{
		public Direction getLeft() {return NORTHWEST;}
		public Direction getRight() {return NORTHEAST;}
		public int getX() {return 0;}
		public int getY() {return -1;}
	},
	
	NORTHEAST
	{
		public Direction getLeft() {return NORTH;}
		public Direction getRight() {return EAST;}
		public int getX() {return 1;}
		public int getY() {return -1;}
	},
	
	EAST
	{
		public Direction getLeft() {return NORTHEAST;}
		public Direction getRight() {return SOUTHEAST;}
		public int getX() {return 1;}
		public int getY() {return 0;}
	},
	
	SOUTHEAST
	{
		public Direction getLeft() {return EAST;}
		public Direction getRight() {return SOUTH;}
		public int getX() {return 1;}
		public int getY() {return 1;}
	},
	
	SOUTH
	{
		public Direction getLeft() {return SOUTHEAST;}
		public Direction getRight() {return SOUTHWEST;}
		public int getX() {return 0;}
		public int getY() {return 1;}
	},
	
	SOUTHWEST
	{
		public Direction getLeft() {return SOUTH;}
		public Direction getRight() {return WEST;}
		public int getX() {return -1;}
		public int getY() {return 1;}
	},
	
	WEST
	{
		public Direction getLeft() {return SOUTHWEST;}
		public Direction getRight() {return NORTHWEST;}
		public int getX() {return -1;}
		public int getY() {return 0;}
	},
	
	NORTHWEST
	{
		public Direction getLeft() {return WEST;}
		public Direction getRight() {return NORTH;}
		public int getX() {return -1;}
		public int getY() {return -1;}
	},
	
	INVALID
	{
		public Direction getLeft() {return NORTH;}
		public Direction getRight() {return SOUTH;}
		public int getX() {return 0;}
		public int getY() {return 0;}
	};
	
	
	public abstract Direction getLeft();
	public abstract Direction getRight();
	public abstract int getX();
	public abstract int getY();
}
