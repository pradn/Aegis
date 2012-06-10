package braincraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Chris Donahue
 * 
 *         A Population object encapsulates all of the neural networks that are
 *         being evolved for a specific task. All members of the same Population
 *         have the same number of inputs and outputs and can breed among
 *         themselves.
 */
public abstract class Population {
	// POPULATION PARAMETERS:
	public double sigmoidCoefficient;
	public double perWeightMutationRate;
	public double weightMutationRate;
	public double linkMutationRate;
	public double linkDisableRate;
	public double nodeMutationRate;
	public double disabledRate;
	public double inheritFromHigherFitRate;

	// SUBCLASS ACCESSIBLE FIELDS:
	/**
	 * Whether this population is alive or not
	 */
	protected boolean alive;
	/**
	 * ID of this population
	 */
	protected int ID;
	/**
	 * Number of sensors for all Neural Nets in this Population
	 */
	protected int numInputs;
	/**
	 * Number of outputs for all Neural Nets in this Population
	 */
	protected int numOutputs;
	/**
	 * Number of members in the Population
	 */
	protected int populationSize;
	/**
	 * List of evaluated Brains
	 */
	protected LinkedList<Brain> evaluated;
	/**
	 * List of unevaluated DNA that has not been issued
	 */
	protected LinkedList<DNA> unevaluated;
	/**
	 * List of Brains that an experiment has currently requested
	 */
	protected LinkedList<Brain> issued;

	// SUPER ONLY FIELDS:
	/**
	 * The number of completed generations of this Population
	 */
	private int currentGeneration;
	/**
	 * ID that will be assigned to the next DNA produced
	 */
	private int nextDNAID;
	/**
	 * Keeps track of the Genes in this Population
	 */
	private ArrayList<Gene> genes;
	/**
	 * Keeps track of the Nodes in this Population
	 */
	private ArrayList<NNode> nodes;

	// CONSTRUCTORS:
	/**
	 * Constructor for a new Population.
	 * 
	 * @param popSize
	 *            the size of this Population
	 * @param in
	 *            number of input nodes for this Population
	 * @param out
	 *            number of output nodes for this Population
	 */
	protected Population(int popSize, int in, int out) {
		initialSetup();

		ID = Braincraft.getNewPopulationID(this);
		numInputs = in;
		numOutputs = out;
		populationSize = popSize;
		Braincraft.report("POPULATION " + ID + " has been created with "
				+ numInputs + " inputs, " + numOutputs
				+ " outputs, and a population size of " + popSize + ".");

		Braincraft.report("POPULATION " + ID + ": Start of generation "
				+ currentGeneration + ".");

		for (int i = 0; i < numInputs; i++) {
			registerNode(new NNode(getNewNodeID(), NNode.INPUT));
		}
		for (int j = 0; j < numOutputs; j++) {
			registerNode(new NNode(getNewNodeID(), NNode.OUTPUT));
		}

		for (int i = 0; i < populationSize; i++) {
			DNA d = new DNA(this, true);
			registerDNA(d);
		}
		postPopulate();
	}

	/**
	 * Constructor for a new Population with default population size.
	 * 
	 * @param numInputs
	 *            the number of inputs for this Population
	 * @param numOutputs
	 *            the number of outputs for this Population
	 */
	protected Population(int numInputs, int numOutputs) {
		this(Braincraft.populationSize, numInputs, numOutputs);
	}

	/**
	 * Constructor for a new Population from a seed neural network.
	 * 
	 * @param popSize
	 *            size of the Population
	 * @param b
	 *            seed Brain
	 */
	protected Population(int popSize, Brain b) {
		initialSetup();

		ID = Braincraft.getNewPopulationID(this);
		populationSize = popSize;

		Braincraft.report("POPULATION " + ID + " has been created from brain "
				+ b.ID + ".");

		Braincraft.report("POPULATION " + ID + ": Start of generation "
				+ currentGeneration + ".");

		numInputs = b.inputs.length;
		numOutputs = b.outputs.length;

		// create the new nodes
		ArrayList<NNode> newnodes = new ArrayList<NNode>();
		newnodes.addAll(b.nodemap.values());
		Collections.sort(newnodes);

		HashMap<Integer, Integer> nidmap = new HashMap<Integer, Integer>();

		for (NNode node : newnodes) {
			int nid = getNewNodeID();
			nidmap.put(node.ID, nid);
			node.ID = nid;
			registerNode(node);
		}

		// create the new genes
		Set<Gene> genes = new HashSet<Gene>();
		for (ArrayList<Gene> arl : b.connections.values()) {
			for (Gene g : arl) {
				genes.add(g);
			}
		}

		ArrayList<Gene> newgenes = new ArrayList<Gene>();
		newgenes.addAll(genes);
		Collections.sort(newgenes);

		for (Gene gene : newgenes) {
			gene.start = nidmap.get(gene.start);
			gene.end = nidmap.get(gene.end);
			gene.innovation = getInnovation(gene.start, gene.end);
			registerGene(gene);
		}

		// create the new DNAs
		for (int i = 0; i < populationSize; i++) {
			DNA d = new DNA(this, false);

			for (NNode n : newnodes)
				d.submitNewNode(n);
			for (Gene g : newgenes)
				d.submitNewGene(g);

			registerDNA(d);
		}
		postPopulate();
	}

	// PUBLIC METHODS:
	/**
	 * Get the ID for this Population object.
	 * 
	 * @return this Population's ID
	 */
	public int getID() {
		return ID;
	}

	/**
	 * Returns the current generation of this Population
	 * 
	 * @return the current generation number of this Population
	 */
	public int currentGeneration() {
		return currentGeneration;
	}

	/**
	 * Accessor method for largest number of Brains you can currently get from a
	 * call to getBrains()
	 * 
	 * @return number of Brains in this current generation that have not been
	 *         evaluated
	 */
	public int brainsAvailable() {
		return unevaluated.size();
	}

	/**
	 * Gets a Collection of neural nets from this Population. Number requested
	 * must be between 0 and population size (inclusive). Number requested must
	 * also be less than or equal to the number of Brains left to evaluate in
	 * this generation. In other words, no more Brains can be created until all
	 * from the previous generation have been evaluated. Subclasses may override
	 * this for different functionality.
	 * 
	 * @param num
	 *            the number of Brains to get
	 * @return a Collection of num Brains or null if any violations arise
	 */
	public Collection<Brain> getBrains(int num) {
		// check if num is in bounds
		if (!alive || num < 0 || num > populationSize) {
			return null;
		}

		// if all evaluated, repopulate
		if (evaluated.size() == populationSize && unevaluated.size() == 0
				&& issued.size() == 0) {
			incrementGeneration();
			repopulate();
			postPopulate();
		}

		// check if num brains are available for delivery
		if (num <= unevaluated.size()) {
			ArrayList<Brain> ret = new ArrayList<Brain>();
			for (int i = 0; i < num; i++) {
				DNA d = unevaluated.poll();
				Brain b = d.DNAtoBrain();
				ret.add(b);
				issued.add(b);
			}
			return ret;
		} else {
			return null;
		}
	}

	/**
	 * Gets a single Brain from the population. All Brains in a generation must
	 * be evaluated before reproduction can occur.
	 * 
	 * @return a Brain object
	 */
	public Brain getBrain() {
		if (!alive) {
			return null;
		}

		if (evaluated.size() == populationSize && unevaluated.size() == 0
				&& issued.size() == 0) {
			incrementGeneration();
			repopulate();
			postPopulate();
		}

		if (!unevaluated.isEmpty()) {
			DNA d = unevaluated.poll();
			Brain b = d.DNAtoBrain();
			issued.add(b);
			return b;
		} else {
			return null;
		}
	}

	/**
	 * Report a tested fitness to the library. Must be called on every Brain in
	 * a generation before more can be produced.
	 * 
	 * @param b
	 *            Brain tested
	 * @param fitness
	 *            fitness for b
	 */
	public void reportFitness(Brain b, Double fitness) {
		// TODO: Change issued data structure from Linked List once stable
		if (issued.contains(b)) {
			b.reportFitness(fitness);
			evaluated.add(b);
			issued.remove(b);
			Braincraft.report("POPULATION " + ID + ": Brain " + b.ID
					+ " just died with fitness " + fitness + ".");
		} else {
			System.out.println("Shouldn't have done this");
		}
	}

	/**
	 * Kills a Population so it can no longer produce networks.
	 * 
	 * @param message
	 *            message to kill with
	 */
	public void killPopulation(String message) {
		alive = false;
		Braincraft.report("POPULATION " + ID + " was killed: " + message);
	}

	// CLASS ID METHODS
	/**
	 * Gets an innovation number for a new gene
	 * 
	 * @param start
	 *            starting node of the gene
	 * @param end
	 *            ending node of the gene
	 * @return a new ID number or a previous one of the gene already existed
	 */
	protected int getInnovation(int start, int end) {
		for (Gene g : genes) {
			if (g.start == start && g.end == end)
				return g.innovation;
		}
		return genes.size() + 1;
	}

	/**
	 * Gets a new node ID
	 * 
	 * @return a new node ID
	 */
	protected int getNewNodeID() {
		return nodes.size() + 1;
	}

	/**
	 * Gets a new DNA ID
	 * 
	 * @return a new DNA ID
	 */
	protected int getNewDNAID() {
		return nextDNAID++;
	}

	// LIBRARY METHODS:
	/**
	 * Registers a DNA object with the library
	 * 
	 * @param d
	 *            DNA to register
	 */
	protected void registerDNA(DNA d) {
		unevaluated.add(d);
		Braincraft.report("POPULATION " + ID + ": DNA " + d.ID
				+ " was just made.");
	}

	/**
	 * Registers an NNode with the library
	 * 
	 * @param node
	 *            NNode to register
	 */
	protected void registerNode(NNode node) {
		nodes.add(node);
		Braincraft.report("POPULATION " + ID + ": Node " + node.ID
				+ " was just made.");
	}

	/**
	 * Registers a Gene with the library
	 * 
	 * @param gene
	 *            Gene to register
	 */
	protected void registerGene(Gene gene) {
		if (gene.innovation - 1 == genes.size()) {
			genes.add(gene);
			Braincraft.report("POPULATION " + ID + ": Innovation "
					+ gene.innovation + " was just made.");
		}
	}

	// EVOLUTION AND HELPER METHODS:
	/**
	 * Increments the library Generation number and calculates averages for
	 * previous generation.
	 */
	private void incrementGeneration() {
		// Calculate average, assign champ
		double totalFitness = 0;
		for (Brain b : evaluated) {
			totalFitness += b.dna.fitness;
		}
		double averageFit = totalFitness / populationSize;

		if (Braincraft.gatherStats)
			Braincraft.generationAverages.add(averageFit);

		Braincraft.report("POPULATION " + ID
				+ ": Average fitness for generation " + currentGeneration
				+ " was " + averageFit);
		Braincraft.report("POPULATION " + ID + ": End of generation "
				+ currentGeneration + ".");
		Braincraft
				.report("-----------------------------------------------------");

		currentGeneration++;
	}

	// SUBCLASS METHODS (TO BE USED BY SUBCLASS):
	/**
	 * Copies parameters from another Population. Used by various subclasses.
	 * 
	 * @param p
	 *            Population to copy parameters from.
	 */
	protected void copyParametersFrom(Population p) {
		sigmoidCoefficient = p.sigmoidCoefficient;
		perWeightMutationRate = p.perWeightMutationRate;
		weightMutationRate = p.weightMutationRate;
		linkMutationRate = p.linkMutationRate;
		linkDisableRate = p.linkDisableRate;
		nodeMutationRate = p.nodeMutationRate;
		disabledRate = p.disabledRate;
		inheritFromHigherFitRate = p.inheritFromHigherFitRate;
	}

	// SUBCLASS HOOK METHODS (TO BE OVERRIDEN BY SUBCLASS):
	/**
	 * Main reproduction method of the library. Must be overridden/implemented
	 * by subclass and super must be called.
	 */
	protected void repopulate() {
		Braincraft.report("POPULATION " + ID + ": Start of generation "
				+ currentGeneration + ".");
	}

	/**
	 * Sets up initial things for the library. Necessary for allocation at
	 * construction time in subclasses.
	 */
	protected void initialSetup() {
		alive = true;
		currentGeneration = 1;
		nextDNAID = 1;
		genes = new ArrayList<Gene>();
		unevaluated = new LinkedList<DNA>();
		issued = new LinkedList<Brain>();
		evaluated = new LinkedList<Brain>();
		nodes = new ArrayList<NNode>();
		sigmoidCoefficient = Braincraft.sigmoidCoefficient;
		perWeightMutationRate = Braincraft.perWeightMutationRate;
		weightMutationRate = Braincraft.weightMutationRate;
		linkMutationRate = Braincraft.linkMutationRate;
		linkDisableRate = Braincraft.linkDisableRate;
		nodeMutationRate = Braincraft.nodeMutationRate;
		disabledRate = Braincraft.disabledRate;
		inheritFromHigherFitRate = Braincraft.inheritFromHigherFitRate;
	}

	/**
	 * Cleans up things after reproduction. Useful for subclasses.
	 */
	protected void postPopulate() {
		evaluated.clear();
		// sanityCheck();
	}

	/**
	 * Sanity check, must return true at all times otherwise something is wrong
	 * with the Population
	 */
	protected abstract void sanityCheck();
}