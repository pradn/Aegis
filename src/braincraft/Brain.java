package braincraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author Chris Donahue
 * 
 *         High level encapsulation of a single Neural Network. Usable outside
 *         of an evolution experiment if saved.
 */
public class Brain implements Serializable {
	// FIELDS:
	/**
	 * ID of this brain
	 */
	protected int ID;
	/**
	 * Represent whether a brain has been evaluated or not
	 */
	protected boolean alive;
	/**
	 * HashMap mapping a node ID to a node object
	 */
	protected HashMap<Integer, NNode> nodemap;
	/**
	 * HashMap mapping a node ID to an array list of genes that terminate at
	 * that node. (Speeds up activation computation)
	 */
	protected HashMap<Integer, ArrayList<Gene>> connections;
	/**
	 * Array representing the inputs of this Brain
	 */
	protected NNode[] inputs;
	/**
	 * Array representing the outputs of this Brain
	 */
	protected NNode[] outputs;
	/**
	 * The sigmoid coefficient for evaluation of this Brain
	 */
	protected Double sigmoidCoefficient;

	// EVOLVE FIELDS:
	/**
	 * The DNA that produced this brain. Only used to report fitness and
	 * identify DNA objects by Brain
	 */
	protected DNA dna;

	// CONSTRUCTORS:

	/**
	 * Constructor for a Brain
	 * 
	 * @param genome
	 *            the DNA that created this Brain
	 * @param id
	 *            the Brain's ID
	 * @param networkgenes
	 *            the Brain's genes (as a Collection to be restructured)
	 * @param nodes
	 *            the Brain's nodes
	 * @param sigmoid
	 *            the sigmoid value for this neural net
	 */
	protected Brain(DNA genome, int id, Collection<Gene> networkgenes,
			HashMap<Integer, NNode> nodes, Double sigmoid) {
		alive = true;
		ID = id;
		nodemap = nodes;
		setupNetworkMap(networkgenes);
		dna = genome;

		// setup I/O array
		int numin = 0;
		int numout = 0;
		for (NNode n : nodemap.values()) {
			if (n.type == NNode.INPUT)
				numin++;
			else if (n.type == NNode.OUTPUT)
				numout++;
		}
		inputs = new NNode[numin];
		outputs = new NNode[numout];
		for (int i = 1; i <= inputs.length; i++) {
			inputs[i - 1] = nodes.get(i);
		}
		for (int j = inputs.length + 1; j <= inputs.length + outputs.length; j++) {
			outputs[j - inputs.length - 1] = nodes.get(j);
		}

		sigmoidCoefficient = sigmoid;
	}

	/**
	 * Transforms an input Collection of Genes into a more computationally
	 * efficient data structure.
	 * 
	 * @param genes
	 *            a Collection of genes to restructure
	 */
	private void setupNetworkMap(Collection<Gene> genes) {
		connections = new HashMap<Integer, ArrayList<Gene>>();
		for (Gene g : genes) {
			if (!connections.containsKey(g.end)) {
				connections.put(g.end, new ArrayList<Gene>());
			}
			ArrayList<Gene> addto = connections.get(g.end);
			addto.add(g);
		}
	}

	// PUBLIC ACCESSOR METHODS:
	/**
	 * Accessor for ID
	 * 
	 * @return the ID of this Brain
	 */
	public int getID() {
		return ID;
	}

	/**
	 * Returns whether or not the Brain has been evaluated in an evolution run.
	 * 
	 * @return true if has not been evaluated, false if has been evaluated,
	 *         false or true if brain was loaded on file depending on when it
	 *         was saved
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * Prints the genetics of this Brain to a String.
	 * 
	 * @return a String representation of the underlying neural network
	 */
	public String toString() {
		// TODO: Make this and DNA use Braincraft.listToString()
		// First line
		String output = "genomestart " + ID + "\n";

		// Node list
		ArrayList<NNode> nodes = new ArrayList<NNode>();
		nodes.addAll(nodemap.values());
		Collections.sort(nodes);
		output += Braincraft.listToString(nodes);

		// Gene list
		Set<Gene> genes = new HashSet<Gene>();
		for (ArrayList<Gene> arl : connections.values()) {
			for (Gene g : arl) {
				genes.add(g);
			}
		}
		ArrayList<Gene> allgenes = new ArrayList<Gene>();
		allgenes.addAll(genes);
		Collections.sort(allgenes);
		output += Braincraft.listToString(allgenes);

		// Last line
		output += "genomeend" + "\n";

		return output;
	}

	// BRAIN I/O
	/**
	 * Allows the user to save this brain as a Java serialized object.
	 * 
	 * @param file
	 *            output filename for this Brain
	 */
	public void saveObject(String file) {
		DNA backupdna = dna;
		dna = null;
		try {
			ObjectOutput out = new ObjectOutputStream(
					new FileOutputStream(file));
			out.writeObject(this);
			out.close();
		} catch (IOException e) {
			Braincraft.report("Couldn't save object to file.");
			dna = backupdna;
		}
		dna = backupdna;
	}

	/**
	 * Loads an serialized Brain from file.
	 * 
	 * @param file
	 *            location of serializable file
	 * @return loaded Brain object
	 */
	public static Brain loadObject(String file) {
		try {
			// Deserialize from a file
			File brain = new File(file);
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					brain));
			// Deserialize the object
			Brain loaded = (Brain) in.readObject();
			in.close();
			return loaded;
		} catch (ClassNotFoundException e) {
		} catch (IOException e) {
			Braincraft.reportError("Could not load serialized Brain from file "
					+ file + ".");
		}
		return null;
	}

	/**
	 * Allows the user to save this brain as raw text data
	 * 
	 * @param file
	 *            output file for this Brain.
	 */
	public void saveText(String file) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write(toString());
			out.close();
		} catch (IOException e) {
			Braincraft.reportError("Could not write Brain " + ID
					+ " to location " + file + ".");
		}
	}

	/**
	 * Parses a saved text Brain and returns a Brain object
	 * 
	 * @param file
	 *            filename for the saved Brain
	 * @param sigmoid
	 *            sigmoid value for the saved Brain (maybe encode in text
	 *            representation in the future)
	 * @return a Brain object equivalent to the text representation
	 */
	public static Brain loadText(String file, double sigmoid) {
		// Set up pointer fields to populate
		ArrayList<NNode> nodes = new ArrayList<NNode>();
		ArrayList<Gene> genes = new ArrayList<Gene>();
		ArrayList<Integer> integers = new ArrayList<Integer>();

		// Attempt to process the text file
		try {
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = br.readLine();

			while (processLine(file, strLine, integers, nodes, genes)) {
				strLine = br.readLine();
			}
			in.close();
		} catch (IOException e) {
			Braincraft.reportError("Could not load Brain from file " + file
					+ ".");
			return null;
		}

		if (nodes.size() == 0 || genes.size() == 0 || integers.size() == 0) {
			Braincraft.reportError("Could not load Brain from file " + file
					+ ".");
			return null;
		}

		HashMap<Integer, NNode> loadedmap = new HashMap<Integer, NNode>();
		for (NNode n : nodes) {
			loadedmap.put(n.ID, n);
		}

		return new Brain(null, integers.get(0), genes, loadedmap, sigmoid);
	}

	/**
	 * Load a Brain from text with default sigmoid (for less experienced users).
	 * 
	 * @param file
	 *            filename of Brain
	 * @return a loaded Brain object
	 */
	public static Brain loadText(String file) {
		return loadText(file, Braincraft.sigmoidCoefficient);
	}

	/**
	 * Parses a line of text for loadText
	 * 
	 * @param file
	 * @param line
	 * @param integers
	 * @param nodes
	 * @param genes
	 * @return
	 */
	private static boolean processLine(String file, String line,
			ArrayList<Integer> integers, ArrayList<NNode> nodes,
			ArrayList<Gene> genes) {
		if (line == null)
			return false;
		try {
			String[] parts = line.split("\\s+");
			if (parts.length == 0)
				return false;
			if (parts[0].equals("genomestart")) {
				integers.add(Integer.parseInt(parts[1]));
			} else if (parts[0].equals("node")) {
				int NID = Integer.parseInt(parts[1]);
				int type = Integer.parseInt(parts[2]);
				NNode addition = new NNode(NID, type);
				nodes.add(addition);
			} else if (parts[0].equals("gene")) {
				int inno = Integer.parseInt(parts[1]);
				int start = Integer.parseInt(parts[2]);
				int end = Integer.parseInt(parts[3]);
				Double weight = Double.parseDouble(parts[4]);
				int enabled = Integer.parseInt(parts[5]);
				boolean enab = true;
				if (enabled == 0)
					enab = false;
				Gene addition = new Gene(inno, start, end, weight, enab);
				genes.add(addition);
			} else if (parts[0].equals("genomeend")) {
				return false;
			}
		} catch (NumberFormatException e) {
			Braincraft.reportError("Could not load Brain from file " + file
					+ ".");
			return false;
		} catch (NullPointerException e) {
			Braincraft.reportError("Could not load Brain from file " + file
					+ ".");
			return false;
		}
		return true;
	}

	// LIBRARY METHODS:
	/**
	 * Ends the life of this Brain upon the termination of evaluation. Should
	 * only be called during an evolution run.
	 * 
	 * @param fitvalue
	 *            the evaluated fitness value for this Brain
	 */
	protected void reportFitness(double fitvalue) {
		if (alive && dna != null) {
			// Clear all activation information
			for (NNode n : nodemap.values()) {
				n.activity = 0.0;
				n.output = 0.0;
			}

			// Report fitnesses
			dna.fitness = fitvalue;

			// Kill cleanly
			alive = false;
		}
	}

	/**
	 * Gets the fitness of a recently evaluated Brain. Should only be called in
	 * the context of evolution and will return null if the Brain hasn't been
	 * evaluated
	 * 
	 * @return the fitness value for this neural net in the context of an
	 *         evolution run
	 */
	protected double getFitness() {
		return dna.fitness;
	}

	// PUBLIC AI INTERACTION:
	/**
	 * Determines the outputs for a Neural Network using the given inputs.
	 * 
	 * @param inputs
	 *            double array representing input values for this neural net.
	 * @return double array representing output values for this neural net.
	 */
	public double[] pumpNet(double[] inputvals) {
		if (inputvals.length != inputs.length)
			return null;

		// Set input activations
		for (int i = 0; i < inputvals.length; i++) {
			inputs[i].activity = inputvals[i];
		}

		// EVALUATING NET:

		// Calculate hidden nodes activity sum first
		for (NNode n : nodemap.values()) {
			if (n.type == NNode.OUTPUT || n.type == NNode.INPUT)
				continue;

			n.activity = calculateActivity(n);
		}

		// Calculate output nodes activity sum second
		for (NNode n : outputs) {
			n.activity = calculateActivity(n);
		}

		// Transfer activity to output through sigmoid and set activity to 0
		for (NNode n : nodemap.values()) {
			n.output = sigmoidFunction(n.activity);
			if (n.type != NNode.INPUT)
				n.activity = 0.0;
		}

		// DONE EVALUATING, RETURN RESULSTS

		// Get output activations
		double[] retarray = new double[outputs.length];
		for (int i = 0; i < retarray.length; i++) {
			retarray[i] = outputs[i].output;
		}

		return retarray;
	}

	/**
	 * Used to clear neural network activity information between pumps. Use
	 * between pumps if you want to clear all data out of the network. Logically
	 * equivalent to removing the network's memory.
	 */
	public void clearActivity() {
		for (NNode n : nodemap.values()) {
			n.activity = 0;
			n.output = 0;
		}
	}

	/**
	 * Clears network activity only at the output nodes. Can't remember why I
	 * made this.
	 */
	public void clearOutputActivity() {
		for (NNode n : outputs) {
			n.activity = 0;
			n.output = 0;
		}
	}

	// NEURAL NET EVALUATION HELPERS:
	/**
	 * Calculates a node's activation
	 * 
	 * @param node
	 *            the NNode to calculate the activation for
	 * @return an activation value
	 */
	private double calculateActivity(NNode node) {
		Double sum = 0.0;
		for (Gene g : connections.get(node.ID)) {
			if (g.enabled) {
				sum += g.weight * getNode(g.start).output;
			}
		}
		return sum;
	}

	/**
	 * Computes the sigmoid function on a weighted sum
	 * 
	 * @param sum
	 *            the sum of the node's input activations times their weights
	 * @return a double between 0 and 1
	 */
	private double sigmoidFunction(Double sum) {
		return 1 / (1 + Math.pow(Math.E, sum * sigmoidCoefficient));
	}

	/**
	 * Gets an NNode from this Brain by its ID
	 * 
	 * @param id
	 *            the id to pull the node object for
	 * @return the found node object or null if it is an invalid id
	 */
	private NNode getNode(int id) {
		return nodemap.get(id);
	}

	// INTERFACE HELPERS:
	/**
	 * Version ID for serialization
	 */
	private static final long serialVersionUID = 1L;
}