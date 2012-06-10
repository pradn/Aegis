package braincraft;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Chris Donahue
 * 
 *         A Tribe is a support class used by TribePopulation to group similar
 *         networks. It is identical to the concept of "Species" from the
 *         original NEAT paper.
 */
public class Tribe implements Comparable<Tribe> {
	/**
	 * ID of this Tribe
	 */
	protected int ID;
	/**
	 * All the DNA in this Tribe
	 */
	private ArrayList<DNA> genomes;
	/**
	 * The DNA from last generation in this Tribe
	 */
	private ArrayList<DNA> lastGen;
	/**
	 * Representative for calculating compatibility
	 */
	protected DNA representative;
	/**
	 * Tribe fitness
	 */
	protected double fitness;
	/**
	 * Number of babies this Tribe has earned in the population
	 */
	protected int numBabies;
	/**
	 * Population this Tribe belongs to
	 */
	private TribePopulation population;

	/**
	 * Constructor for a new Tribe
	 * 
	 * @param spec
	 *            Population that this Tribe is a part of
	 * @param rep
	 *            Representative DNA for this tribe
	 */
	protected Tribe(TribePopulation spec, DNA rep) {
		population = spec;
		ID = population.getNewTribeID();
		genomes = new ArrayList<DNA>();
		lastGen = new ArrayList<DNA>();
		genomes.add(rep);
		representative = rep;
	}

	/**
	 * Adds a DNA to this Tribe
	 * 
	 * @param d
	 *            DNA to add
	 */
	protected void add(DNA d) {
		genomes.add(d);
	}

	/**
	 * Moves current generation to last generation
	 */
	protected void backupLastGen() {
		lastGen.clear();
		for (DNA d : genomes) {
			lastGen.add(d);
		}
		genomes.clear();
	}

	/**
	 * Gets a random DNA from this Tribe
	 * 
	 * @return a random DNA from the parental generation
	 */
	protected DNA getRandomParent() {
		return lastGen.get(Braincraft.randomInteger(lastGen.size()));
	}

	/**
	 * Gets the size of this Tribe
	 * 
	 * @return size of the Tribe as an int
	 */
	protected int size() {
		return genomes.size();
	}

	/**
	 * Gets a DNA by fitness ranking after sorted
	 * 
	 * @param i
	 *            the fitness rank (1 is highest)
	 * @return the DNA with that fitness rank
	 */
	protected DNA get(int i) {
		return genomes.get(i);
	}

	/**
	 * Sorts genomes by their fitness values
	 */
	protected void sortByFitness() {
		Collections.sort(genomes);
	}

	/**
	 * Removes a member of the Tribe
	 */
	protected void removeLowestIndex() {
		if (genomes.size() > 0)
			genomes.remove(0);
	}

	/**
	 * Returns the number of disjoint genes, the number of excess genes, and the
	 * average weight difference between input and champ in indexes 0, 1, 2
	 * respectively.
	 * 
	 * @param other
	 *            the DNA object for comparison to the representative
	 * @return a double array with difference information
	 */
	private double[] getDisjointExcessWeightCount(DNA other) {
		int highest = representative.getHighestInnovation();
		int highest2 = other.getHighestInnovation();
		int excess = 0;
		int disjoint = 0;
		double weightdif = 0;
		int matching = 0;
		DNA larger;
		int low = Math.min(highest, highest2);
		int high = Math.max(highest, highest2);

		// Calculate which genome has a higher innovation
		if (high == highest)
			larger = representative;
		else
			larger = other;

		// Calculate excess
		for (int i = low + 1; i <= highest; i++) {
			if (larger.hasGene(i)) {
				excess++;
			}
		}

		// Calculate disjoint
		for (int i = 0; i <= low; i++) {
			boolean d1has = representative.hasGene(i);
			boolean d2has = other.hasGene(i);
			if ((d1has || d2has) && !(d1has && d2has)) {
				disjoint++;
			}
			if (d1has && d2has) {
				weightdif += Math.abs(representative.getGene(i).weight
						- other.getGene(i).weight);
				matching++;
			}
		}
		double[] ret = new double[3];
		ret[0] = disjoint;
		ret[1] = excess;
		ret[2] = weightdif / matching;
		return ret;
	}

	/**
	 * Returns true if input DNA is compatible with this tribe
	 * 
	 * @param another
	 *            DNA object to test compatibility
	 * @return true if DNA passes compatibility test, otherwise false
	 */
	protected boolean isCompatible(DNA another) {
		double[] disex = getDisjointExcessWeightCount(another);
		int n = Math.max(representative.numGenes(), another.numGenes());
		if (n < 20) {
			n = 1;
		}
		double comindex = ((population.c1 * disex[1]) / n)
				+ ((population.c2 * disex[0]) / n) + population.c3 * disex[2];
		// If they pass the threshold test or have the same phenotype then
		// return true;
		if (comindex >= population.tribeCompatibilityThreshold
				|| (disex[0] == 0 && disex[1] == 0)) {
			return true;
		}
		return false;
	}

	// JAVA INTERFACE HELPERS:
	public int compareTo(Tribe t) {
		if (ID < t.ID)
			return -1;
		else if (ID == t.ID)
			return 0;
		else
			return 1;
	}
}