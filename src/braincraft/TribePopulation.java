package braincraft;

import java.util.ArrayList;

/**
 * @author Chris Donahue
 * 
 *         A TribePopulation enforces NEAT-style population control and uses the
 *         NEAT genetic algorithm. This is not a real time version, all networks
 *         must be evaluated before any more can be produced.
 */
public class TribePopulation extends Population {
	// TRIBEPOPULATION PARAMETERS:
	/**
	 * NEAT parameter for calculating Tribe compatibility
	 */
	public double c1;
	/**
	 * NEAT parameter for calculating Tribe compatibility
	 */
	public double c2;
	/**
	 * NEAT parameter for calculating Tribe compatibility
	 */
	public double c3;
	/**
	 * NEAT parameter for Tribe placement
	 */
	public double tribeCompatibilityThreshold;
	/**
	 * Percentage of population killed before reproduction occurs.
	 */
	public double percentageOfTribeToKillBeforeReproduction;

	// FIELDS:
	/**
	 * Keeps track of the Tribes in this Population
	 */
	private ArrayList<Tribe> tribes;
	/**
	 * Keeps track of the Tribes created this generation
	 */
	private ArrayList<Tribe> newTribes;
	/**
	 * The best Tribe from the previous generation
	 */
	private Tribe champTribe;

	// CONSTRUCTORS:
	/**
	 * Construct a TribePopulation
	 * 
	 * @param popSize
	 * @param in
	 * @param out
	 */
	public TribePopulation(int popSize, int in, int out) {
		super(popSize, in, out);
	}

	/**
	 * Construct a TribePopulation with default population size
	 * 
	 * @param numInputs
	 * @param numOutputs
	 */
	public TribePopulation(int numInputs, int numOutputs) {
		this(Braincraft.populationSize, numInputs, numOutputs);
	}

	/**
	 * Construct a TribePopulation from a seed network
	 * 
	 * @param popSize
	 * @param b
	 */
	public TribePopulation(int popSize, Brain b) {
		super(popSize, b);
	}

	// LIBRARY METHODS:
	/**
	 * Gets an ID for a new Tribe
	 * 
	 * @return the new ID
	 */
	protected int getNewTribeID() {
		return tribes.size() + newTribes.size() + 1;
	}

	/**
	 * Registers a Tribe with the library
	 * 
	 * @param t the Tribe to register
	 */
	protected void registerTribe(Tribe t) {
		newTribes.add(t);
		Braincraft.report("POPULATION " + ID + ": Tribe " + t.ID
				+ " was just made.");
	}

	/**
	 * Takes a DNA object and finds a Tribe for it.
	 * 
	 * @param d
	 *            the DNA to find a Tribe for
	 */
	protected void registerDNA(DNA d) {
		super.registerDNA(d);

		for (Tribe tri : tribes) {
			if (tri.isCompatible(d)) {
				tri.add(d);
				return;
			}
		}

		for (Tribe tri : newTribes) {
			if (tri.isCompatible(d)) {
				tri.add(d);
				return;
			}
		}

		// Otherwise create a new Tribe with this Brain as the representative.
		registerTribe(new Tribe(this, d));
	}

	// NEAT EPOCH AND HELPER METHODS:
	protected void repopulate() {
		super.repopulate();

		double totalFitness = 0.0;
		double normalizeNeg = 0.0;

		// Tribe loop to determine adjusted fitnesses
		for (Tribe t : tribes) {
			double tribeFitness = 0.0;
			int tribeSize = t.size();

			// Calculate adjusted fitness for each Brain
			for (int i = 0; i < tribeSize; i++) {
				DNA d = t.get(i);
				if (champTribe == null || !tribes.contains(champTribe)
						|| champTribe.representative.fitness < d.fitness) {
					champTribe = t;
					t.representative = d;
				}

				// SHARE FITNESS
				d.fitness = d.fitness / tribeSize;
				tribeFitness += d.fitness;
				totalFitness += d.fitness;
			}
			
			// Find the most negative tribe fitness for normalization
			if (tribeFitness < normalizeNeg)
			{
				normalizeNeg = tribeFitness;
			}

			t.fitness = tribeFitness;
		}
		
		// Normalize the total fitness
		totalFitness = totalFitness + (tribes.size() * -1 * normalizeNeg);

		// Determine reproduction rights, remove poor-performing members
		int numBabiesDealt = 0;
		
		if (totalFitness == 0)
		{
			// All babies go to champion tribe if totalFitness is 0 (edge case)
			for (Tribe t : tribes) {
				t.sortByFitness();
				int tribeSize = t.size();
				t.numBabies = 0;
				int numUnfit = (int) (percentageOfTribeToKillBeforeReproduction * tribeSize);
				for (int i = 0; i < numUnfit; i++) {
					t.removeLowestIndex();
				}
			}
		}
		else
		{
			for (Tribe t : tribes) {
				t.sortByFitness();
				// Normalize the tribe fitness
				t.fitness = t.fitness + (-1 * normalizeNeg);
				int tribeSize = t.size();
				// Number of Babies a tribe gets is equal to its share of the total
				// fitness multiplied by the population size.
				int designatedBabies = (int) ((t.fitness / totalFitness) * populationSize);
				numBabiesDealt += designatedBabies;
				t.numBabies = designatedBabies;
				// Remove unfit part of the tribe
				int numUnfit = (int) (percentageOfTribeToKillBeforeReproduction * tribeSize);
				for (int i = 0; i < numUnfit; i++) {
					t.removeLowestIndex();
				}
			}
		}

		// Assign champTribe the rounded-off babies
		if (populationSize > numBabiesDealt) {
			champTribe.numBabies += (populationSize - numBabiesDealt);
		}

		int babycount = 0;
		for (Tribe t : tribes)
			babycount += t.numBabies;

		// Move last generation to proper array
		for (Tribe t : tribes) {
			t.backupLastGen();
		}

		// Reproduce designated number of babies
		for (Tribe t : tribes) {
			// Skip tribes that did not earn the right to reproduce
			if (t.numBabies == 0) {
				continue;
			}

			// Create offspring
			for (int j = 0; j < t.numBabies; j++) {
				DNA mother = t.getRandomParent();
				DNA father = t.getRandomParent();
				// System.out.println(mother.ID + "," + father.ID);
				DNA child = father.NEATcross(mother);
				registerDNA(child);
				if (Braincraft.gatherStats)
					Braincraft.genetics.add("reproduction " + mother.ID + " "
							+ father.ID + " " + child.ID);
			}
			t.numBabies = 0;
			t.fitness = 0;
		}
	}

	// HOOK METHODS:
	protected void sanityCheck() {
		System.out.println("SANITY CHECK");
		int popSizeCheck = 0;
		for (Tribe t : tribes) {
			popSizeCheck += t.size();
		}
		for (Tribe t : newTribes) {
			popSizeCheck += t.size();
		}
		if (popSizeCheck != populationSize) {
			System.out.println("Tribes insane! " + popSizeCheck);
			System.exit(-1);
		}

		if (unevaluated.size() + issued.size() + evaluated.size() != populationSize) {
			System.out.println("Population insane! " + popSizeCheck);
			System.exit(-1);
		}
	}

	protected void initialSetup() {
		super.initialSetup();
		tribes = new ArrayList<Tribe>();
		newTribes = new ArrayList<Tribe>();
		c1 = Braincraft.c1;
		c2 = Braincraft.c2;
		c3 = Braincraft.c3;
		tribeCompatibilityThreshold = Braincraft.tribeCompatibilityThreshold;
		percentageOfTribeToKillBeforeReproduction = Braincraft.percentageOfTribeToKillBeforeReproduction;
	}

	protected void postPopulate() {
		super.postPopulate();
		tribes.addAll(newTribes);
		newTribes.clear();

		// Find empty tribes
		ArrayList<Tribe> deadTribes = new ArrayList<Tribe>();
		for (Tribe t : tribes) {
			if (t.size() == 0)
				deadTribes.add(t);
		}
		// Kill off empty tribes
		for (Tribe t : deadTribes) {
			tribes.remove(t);
			Braincraft.report("POPULATION " + ID + ": Tribe " + t.ID
					+ " was eradicated.");
		}
		if (tribes.size() == 1) {
			Braincraft.report("POPULATION " + ID + ": There is "
					+ tribes.size() + " tribe currently active.");
		} else {
			Braincraft.report("POPULATION " + ID + ": There are "
					+ tribes.size() + " tribes currently active.");
		}
	}
}