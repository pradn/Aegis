package braincraft;

import java.io.Serializable;

/**
 * @author Chris Donahue
 * 
 *         A Gene is a link in a neural network. Gene innovation numbers are a
 *         major factor in crossing two networks.
 */
public class Gene implements Comparable<Gene>, Serializable {
	// FIELDS:
	/**
	 * Innovation number (essentially ID) for this Gene
	 */
	protected int innovation;
	/**
	 * ID of Gene's start node
	 */
	protected int start;
	/**
	 * ID of Gene's end node
	 */
	protected int end;
	/**
	 * Double weight of this gene
	 */
	protected double weight;
	/**
	 * Boolean representing whether or not this Gene is active
	 */
	protected boolean enabled;

	// CONSTUCTORS:
	/**
	 * Constructor for a new Gene
	 * 
	 * @param innoNum
	 *            innovation
	 * @param startNode
	 *            start node ID
	 * @param endNode
	 *            end node ID
	 * @param weightValue
	 *            weight
	 * @param enab
	 *            whether this gene is enabled or not
	 */
	protected Gene(int innoNum, int startNode, int endNode, double weightValue,
			boolean enab) {
		innovation = innoNum;
		start = startNode;
		end = endNode;
		weight = weightValue;
		enabled = enab;
	}

	/**
	 * Constructs a pointer-independent copy Gene from another Gene
	 * 
	 * @param g
	 *            Gene to copy
	 */
	protected Gene(Gene g) {
		innovation = g.innovation;
		start = g.start;
		end = g.end;
		weight = g.weight;
		enabled = g.enabled;
	}

	// LIBRARY METHODS:
	/**
	 * Is this Gene logically equivalent to another?
	 * 
	 * @param g
	 *            another gene
	 * @return true if this Gene starts and ends at the same node as g,
	 *         otherwise false
	 */
	protected boolean equals(Gene g) {
		if (g.start == start && g.end == end) {
			return true;
		}
		return false;
	}

	// JAVA INTERFACE HELPERS:
	public int compareTo(Gene g) {
		if (innovation < g.innovation)
			return -1;
		else if (innovation == g.innovation)
			return 0;
		else
			return 1;
	}

	public String toString() {
		int bit = 0;
		if (enabled)
			bit = 1;
		String genestring = "gene ";
		genestring += Integer.toString(innovation) + " ";
		genestring += Integer.toString(start) + " ";
		genestring += Integer.toString(end) + " ";
		genestring += Double.toString(weight) + " ";
		genestring += Integer.toString(bit);
		return genestring;
	}

	/**
	 * Version ID for serialization
	 */
	private static final long serialVersionUID = 1L;
}