package braincraft;

import java.io.Serializable;

/**
 * @author Chris Donahue
 * 
 *         An NNode is a Node in a neural network.
 */
public class NNode implements Comparable<NNode>, Serializable {
	// NNODE TYPES:
	/**
	 * NNode is a sensor for the network
	 */
	protected static final int INPUT = 1;
	/**
	 * NNode is an output for the network
	 */
	protected static final int OUTPUT = 2;
	/**
	 * NNode is a middle node in the network
	 */
	protected static final int HIDDEN = 3;

	// FIELDS:
	/**
	 * ID of this NNode
	 */
	protected int ID;
	/**
	 * Type of this NNode
	 */
	protected int type;
	/**
	 * Output level of this NNode
	 */
	protected double output;
	/**
	 * Input activity of this NNode
	 */
	protected double activity;

	// CONSTRUCTORS:
	/**
	 * Constructs a new NNode
	 * 
	 * @param id
	 *            ID of this NNode
	 * @param typeNum
	 *            type of this NNode
	 */
	protected NNode(int id, int typeNum) {
		ID = id;
		type = typeNum;
		output = 0;
		activity = 0;
	}

	/**
	 * Constructs a pointer-independent copy of another NNode
	 * 
	 * @param n
	 *            NNode to copy
	 */
	protected NNode(NNode n) {
		ID = n.ID;
		type = n.type;
		output = 0;
		activity = 0;
	}

	/**
	 * Tests if this NNode has the same ID as another
	 * 
	 * @param n
	 *            other NNode
	 * @return true if this and n have the same ID number, otherwise false
	 */
	protected boolean equals(NNode n) {
		if (n.ID == this.ID) {
			return true;
		}
		return false;
	}

	// JAVA INTERFACE HELPERS:
	public int compareTo(NNode n) {
		if (ID < n.ID)
			return -1;
		else if (ID == n.ID)
			return 0;
		else
			return 1;
	}

	public String toString() {
		String nodestring = "node ";
		nodestring += Integer.toString(ID) + " ";
		nodestring += Integer.toString(type);
		return nodestring;
	}

	/**
	 * Version ID for serialization
	 */
	private static final long serialVersionUID = 1L;
}