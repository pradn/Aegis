package braincraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Chris Donahue
 * 
 *         DNA stores all of the genetic information used to create a Brain.
 *         Used in the context of evolution but discarded when a Brain is saved.
 */
public class DNA implements Comparable<DNA> {
	// FIELDS:
	/**
	 * ID of this DNA
	 */
	protected Integer ID;
	/**
	 * Fitness for this DNA as reported through the corresponding Brain object.
	 * Starts as null.
	 */
	protected Double fitness;
	/**
	 * Highest innovation number in this DNA.
	 */
	private int highest;
	/**
	 * A HashMap mapping innovation numbers to Gene objects
	 */
	private HashMap<Integer, Gene> genes;
	/**
	 * A HashMap mapping node IDs to NNode objects
	 */
	private HashMap<Integer, NNode> nodes;
	/**
	 * Population that this DNA belongs to
	 */
	private Population population;

	// CONSTRUCTOR:
	/**
	 * Constructor for a new DNA object
	 * 
	 * @param pop
	 *            the population that produced this DNA
	 * @param init
	 *            if true, will fully connect the Populations inputs/outputs and
	 *            add them to this DNA
	 */
	protected DNA(Population pop, boolean init) {
		population = pop;
		ID = population.getNewDNAID();
		highest = 0;
		genes = new HashMap<Integer, Gene>();
		nodes = new HashMap<Integer, NNode>();
		if (init) {
			initializeDNA();
		}
		if (Braincraft.gatherStats)
			Braincraft.allDNA.add(this);
	}

	/**
	 * Makes a pointer-independent clone of a DNA object
	 * 
	 * @param pop
	 *            Population that produced this DNA
	 * @param d
	 *            DNA object to clone
	 */
	protected DNA(Population pop, DNA d) {
		// TODO: implement this
	}

	// GENE METHODS:
	/**
	 * Returns the number of genes in this DNA
	 * 
	 * @return number of genes in this DNA
	 */
	protected int numGenes() {
		return genes.size();
	}

	/**
	 * Returns a random gene from within this DNA
	 * 
	 * @return a random gene
	 */
	protected Gene getRandomGene() {
		ArrayList<Gene> genearr = new ArrayList<Gene>();
		genearr.addAll(genes.values());
		return genearr.get(Braincraft.randomInteger(genearr.size()));
	}

	/**
	 * Properly adds a new gene to this DNA
	 * 
	 * @param element
	 *            the gene to submit
	 */
	protected void submitNewGene(Gene element) {
		Gene newgene = new Gene(element);
		genes.put(newgene.innovation, newgene);

		if (newgene.innovation > highest)
			highest = newgene.innovation;
	}

	/**
	 * Query to see if this DNA has a particular Gene
	 * 
	 * @param innovation
	 *            Gene innovation in question
	 * @return true if this DNA has this Gene, otherwise false
	 */
	protected boolean hasGene(int innovation) {
		return genes.containsKey(innovation);
	}

	/**
	 * Gets the gene at index innovation in this DNA
	 * 
	 * @param innovation
	 *            the innovation number to look for
	 * @return the gene at index innovation or null if it is not in this DNA
	 */
	protected Gene getGene(int innovation) {
		return genes.get(innovation);
	}

	/**
	 * Get the highest innovation number of any gene in this DNA
	 * 
	 * @return highest innovation number in this DNA
	 */
	protected int getHighestInnovation() {
		return highest;
	}

	/**
	 * Get a set of all the gene innovation numbers in this DNA
	 * 
	 * @return set of innovation numbers in this DNA
	 */
	protected Set<Integer> getInnovations() {
		return genes.keySet();
	}

	/**
	 * Returns true if this DNA has the specified gene
	 * 
	 * @param start
	 *            starting node ID
	 * @param end
	 *            ending node ID
	 * @return whether or not this DNA has this gene
	 */
	protected boolean hasConnection(int start, int end) {
		for (Gene g : genes.values()) {
			if (g.start == start && g.end == end)
				return true;
		}
		return false;
	}

	// NODE METHODS:
	/**
	 * Get the number of nodes in this DNA
	 * 
	 * @return number of nodes in this DNA
	 */
	protected int numNodes() {
		return nodes.size();
	}

	/**
	 * Get a random node from this DNA
	 * 
	 * @return random node
	 */
	protected NNode getRandomNode() {
		ArrayList<NNode> nodearr = new ArrayList<NNode>();
		nodearr.addAll(nodes.values());
		return nodearr.get(Braincraft.randomInteger(nodearr.size()));
	}

	/**
	 * Gets a specific node in this DNA by node ID
	 * 
	 * @param ID
	 *            the ID of the NNode
	 * @return an NNode from this DNA with the specified ID number, null if the
	 *         DNA does not have this NNode
	 */
	protected NNode getNode(int ID) {
		return nodes.get(ID);
	}

	/**
	 * Properly submit a new node to this DNA
	 * 
	 * @param node
	 *            the new node for the DNA
	 */
	protected void submitNewNode(NNode node) {
		NNode newnode = new NNode(node);
		nodes.put(newnode.ID, newnode);
	}

	/**
	 * Returns true if this DNA has the node specified
	 * 
	 * @param ID
	 *            node to check
	 * @return true if DNA has node, otherwise false
	 */
	protected boolean hasNode(int ID) {
		return nodes.containsKey(ID);
	}

	// DNA ALTERATIONS:
	/**
	 * Sets up a fully connected DNA with only inputs, outputs and random
	 * weights.
	 * 
	 * @param init
	 *            set up a new fully connected Genome or not
	 */
	private void initializeDNA() {
		int numInputs = population.numInputs;
		int numOutputs = population.numOutputs;
		for (int i = 1; i <= numInputs; i++) {
			submitNewNode(new NNode(i, NNode.INPUT));
		}
		for (int j = numInputs + 1; j <= numInputs + numOutputs; j++) {
			submitNewNode(new NNode(j, NNode.OUTPUT));
		}

		for (int i = 0; i < numInputs; i++) {
			for (int j = 0; j < numOutputs; j++) {
				int start = i + 1;
				int end = numInputs + 1 + j;
				int innovation = population.getInnovation(start, end);
				Gene g = new Gene(innovation, start, end, Braincraft
						.randomWeight(), true);
				population.registerGene(g);
				submitNewGene(g);
			}
		}
	}

	/**
	 * Mutate this DNA to add a node
	 */
	protected void mutateAddNode() {
		// Test if genome is fully disabled
		boolean fullydisabled = true;
		for (Gene g : genes.values()) {
			if (g.enabled) {
				fullydisabled = false;
				break;
			}
		}
		if (fullydisabled)
			return;

		// Select the gene to be split
		Gene mutated;
		do {
			mutated = getRandomGene();
		} while (!mutated.enabled);

		// Create the new structure
		NNode addition = new NNode(population.getNewNodeID(), NNode.HIDDEN);
		population.registerNode(addition);

		int earlystart = mutated.start;
		int earlyend = addition.ID;
		int earlyinnov = population.getInnovation(earlystart, earlyend);
		Gene early = new Gene(earlyinnov, earlystart, earlyend, 1, true);
		population.registerGene(early);

		int latestart = addition.ID;
		int lateend = mutated.end;
		int lateinnov = population.getInnovation(latestart, lateend);
		Gene late = new Gene(lateinnov, latestart, lateend, mutated.weight,
				true);
		population.registerGene(late);

		// Disable old gene
		mutated.enabled = false;

		// Submit new node
		submitNewNode(addition);

		// Submit new genes
		submitNewGene(early);
		submitNewGene(late);

		if (Braincraft.gatherStats)
			Braincraft.genetics.add("node mutation " + ID + " "
					+ mutated.innovation + " " + early.innovation + " "
					+ late.innovation + " " + addition.ID);
	}

	/**
	 * Mutate this DNA to add a link
	 */
	protected void mutateAddLink() {
		// Make sure network is not fully connected

		// Sum number of connections
		int totalconnections = numGenes();

		// Find number of each type of node
		int in = population.numInputs;
		int out = population.numOutputs;
		int hid = numNodes() - (in + out);

		// Find the number of possible connections.
		// Links cannot end with an input
		int fullyconnected = 0;
		fullyconnected = (in + out + hid) * (out + hid);

		if (totalconnections == fullyconnected)
			return;

		// Pick 2 nodes for a new connection and submit it
		NNode randomstart;
		NNode randomend;
		do {
			randomstart = getRandomNode();
			randomend = getRandomNode();
		} while (randomend.type == NNode.INPUT
				|| hasConnection(randomstart.ID, randomend.ID));

		int newgeneinno = population
				.getInnovation(randomstart.ID, randomend.ID);
		Gene newgene = new Gene(newgeneinno, randomstart.ID, randomend.ID,
				Braincraft.randomWeight(), true);
		population.registerGene(newgene);
		submitNewGene(newgene);

		if (Braincraft.gatherStats)
			Braincraft.genetics.add("link creation mutation " + ID + " "
					+ newgene.innovation + " " + randomstart.ID + " "
					+ randomend.ID);
	}

	/**
	 * Mutate the weights of this DNA
	 */
	protected void mutateWeights() {
		// TODO: Change the way weight mutation works
		if (Braincraft.gatherStats) {
			ArrayList<Integer> mutatedgenes = new ArrayList<Integer>();
			for (Gene g : genes.values()) {
				if (Braincraft.randomChance(population.perWeightMutationRate)) {
					g.weight = Braincraft.randomWeight();
					mutatedgenes.add(g.innovation);
				}
			}
			String output = "weight mutation " + ID;
			for (Integer i : mutatedgenes) {
				output += " " + i;
			}
			Braincraft.genetics.add(output);
		} else {
			for (Gene g : genes.values()) {
				if (Braincraft.randomChance(population.perWeightMutationRate))
					g.weight = Braincraft.randomWeight();
			}
		}
		// TODO: Report weight mutations to stats
	}

	/**
	 * Disables a random link in this genome. Primitive search pruning.
	 */
	protected void mutateDisableLink() {
		Gene g = getRandomGene();
		g.enabled = false;

		if (Braincraft.gatherStats)
			Braincraft.genetics.add("link disable mutation " + ID + " "
					+ g.innovation);
	}

	/**
	 * Genetic algorithm to cross two DNA objects and produce a child. This
	 * specific GA is part of Ken Stanley's NEAT algorithm and is included in
	 * the DNA class as a default method for crossing two DNA objects. New
	 * genetic algorithms should be added to new Population classes, not DNA.
	 * More accessor methods might need to be added to DNA to support new GA's.
	 * 
	 * @param other
	 *            the DNA to cross this one with
	 * @return a child DNA (how cute!)
	 */
	protected DNA NEATcross(DNA other) {
		DNA hifit;
		DNA lofit;

		if (this.fitness == null || other.fitness == null)
			return null;

		DNA ret = new DNA(population, false);

		// Choose the genome with the higher fitness
		if (this.fitness > other.fitness) {
			hifit = this;
			lofit = other;
		} else {
			hifit = this;
			lofit = other;
		}

		// Populate gene list of ret
		for (Integer i : hifit.getInnovations()) {
			Gene submission;
			if (!lofit.hasGene(i)) {
				submission = hifit.getGene(i);
			} else {
				Gene newgene;
				if (Braincraft
						.randomChance(population.inheritFromHigherFitRate))
					newgene = new Gene(hifit.getGene(i));
				else
					newgene = new Gene(lofit.getGene(i));
				if (!hifit.getGene(i).enabled || !lofit.getGene(i).enabled) {
					if (Braincraft.randomChance(population.disabledRate))
						newgene.enabled = false;
					else
						newgene.enabled = true;
				}
				submission = newgene;
			}
			if (!ret.hasNode(submission.start)) {
				ret.submitNewNode(hifit.getNode(submission.start));
			}
			if (!ret.hasNode(submission.end)) {
				ret.submitNewNode(hifit.getNode(submission.end));
			}
			ret.submitNewGene(submission);
		}

		if (Braincraft.randomChance(population.weightMutationRate))
			ret.mutateWeights();
		if (Braincraft.randomChance(population.linkMutationRate))
			ret.mutateAddLink();
		if (Braincraft.randomChance(population.nodeMutationRate))
			ret.mutateAddNode();
		if (Braincraft.randomChance(population.linkDisableRate))
			ret.mutateDisableLink();

		return ret;
	}

	// LIBRARY METHODS:
	/**
	 * Returns a Brain object constructed from this DNA
	 * 
	 * @return a new Brain object
	 */
	protected Brain DNAtoBrain() {
		return new Brain(this, this.ID, genes.values(), nodes,
				population.sigmoidCoefficient);
	}

	// INTERFACE HELPERS:
	public String toString() {
		// First line
		String output = "genomestart " + ID + "\n";

		// Node list
		ArrayList<NNode> nodelist = new ArrayList<NNode>();
		nodelist.addAll(nodes.values());
		Collections.sort(nodelist);
		output += Braincraft.listToString(nodelist);

		// Gene list
		ArrayList<Gene> allGenes = new ArrayList<Gene>();
		allGenes.addAll(genes.values());
		Collections.sort(allGenes);
		output += Braincraft.listToString(allGenes);

		// Last line
		output += "genomeend" + "\n";

		return output;
	}

	public int compareTo(DNA d) {
		if (fitness == null || d.fitness == null)
			return 0;
		if (fitness < d.fitness)
			return -1;
		else if (fitness == d.fitness)
			return 0;
		else
			return 1;
	}
}