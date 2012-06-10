package braincraft;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//import java.util.Random;

/**
 * @author Chris Donahue
 * 
 *         Braincraft is a platform for the evolution of Neural Networks and the
 *         testing of genetic algorithms/population control algorithms. Heavily
 *         inspired by NEAT: http://www.cs.ucf.edu/~kstanley/neat.html
 * 
 *         This class is a singleton to provide storage and convenience across
 *         the library. Stores default values for all parameters.
 */
public class Braincraft {
	// ALL POPULATION PARAMETER DEFAULTS:
	protected static int populationSize = 100;
	protected static double sigmoidCoefficient = -4.9;
	protected static double perWeightMutationRate = 0.9;
	protected static double weightMutationRate = 0.8;
	protected static double linkMutationRate = 0.1;
	protected static double linkDisableRate = 0.1;
	protected static double nodeMutationRate = 0.05;
	protected static double disabledRate = 0.75;
	protected static double inheritFromHigherFitRate = 0.8;

	// POPULATION SUBCLASS PARAMETER DEFAULTS:
	protected static double c1 = 1.0;
	protected static double c2 = 1.0;
	protected static double c3 = 0.4;
	protected static double tribeCompatibilityThreshold = 3.0;
	protected static double percentageOfTribeToKillBeforeReproduction = 0.5;
	protected static double survivalRatePerGeneration = 0.2;

	// STATISTICS FIELDS:
	public static boolean gatherStats = false;
	protected static ArrayList<DNA> allDNA = new ArrayList<DNA>();
	protected static ArrayList<String> genetics = new ArrayList<String>();
	protected static ArrayList<Double> generationAverages = new ArrayList<Double>();

	// FIELDS:
	public static boolean logToSystemOut = false;
	// private static Random rng = new Random();
	private static ArrayList<Population> society = new ArrayList<Population>();
	private static ArrayList<String> log = new ArrayList<String>();
	private static ArrayList<String> errorLog = new ArrayList<String>();

	// CONSTRUCTORS (to avoid public construction)
	/**
	 * Unused constructor
	 */
	private Braincraft() {
	}

	// PUBLIC METHODS:
	/**
	 * Writes the log messages to a specified file
	 * 
	 * @param file
	 *            the file to write to
	 * @return 1 if successful, -1 if unsuccessful
	 */
	public static int writeLog(String file) {
		return writeStringToFile(listToString(log), file);
	}

	/**
	 * Writes the visualizer statistics to a specified file
	 * 
	 * @param file
	 *            the file to write to
	 * @return 1 if successful, -1 if unsuccessful
	 */
	public static int writeStats(String file) {
		return writeStringToFile(listToString(allDNA) + listToString(genetics)
				+ listToString(generationAverages), file);
	}

	/**
	 * Writes the error log messages to a specified file
	 * 
	 * @param file
	 *            the file to write to
	 * @return 1 if successful, -1 if unsuccessful
	 */
	public static int writeErrorLog(String file) {
		return writeStringToFile(listToString(errorLog), file);
	}

	// LIBRARY METHODS:
	/**
	 * Bernoulli trial with percentage chance
	 * 
	 * @param chance
	 *            the chance of success for this Bernoulli trial
	 * @return whether or not the trial was a success
	 */
	protected static boolean randomChance(double chance) {
		if (Math.random() < chance)
			return true;
		return false;
	}

	/**
	 * Get a random weight value
	 * 
	 * @return double a weight value between -1 and 1
	 */
	protected static double randomWeight() {
		int sign = (int) (Math.random() * 2);
		double value = Math.random();
		if (sign == 0) {
			return value * -1;
		}
		return value;
	}

	/**
	 * Gets a random integer between 0 (inclusive) and the specified range
	 * (exclusive)
	 * 
	 * @param range
	 *            get a random number greater than or equal to 0 but less than
	 *            range
	 * @return a random integer
	 */
	protected static int randomInteger(int range) {
		// TODO: MAKE THIS MORE RANDOM
		return (int) (Math.random() * range);
		// return rng.nextInt(range);
	}

	/**
	 * Adds a string to the library log
	 * 
	 * @param message
	 *            message to add to the log
	 */
	protected static void report(String message) {
		if (logToSystemOut)
			System.out.println(message);
		log.add(message);
	}

	/**
	 * Adds a string to the library error log
	 * 
	 * @param message
	 *            error to report
	 */
	protected static void reportError(String message) {
		if (logToSystemOut)
			System.out.println(message);
		errorLog.add(message);
	}

	/**
	 * Returns an integer representing the new Population ID
	 * 
	 * @param p
	 *            Population to get an ID for
	 * @return new Population ID
	 */
	protected static int getNewPopulationID(Population p) {
		int ret = society.size() + 1;
		society.add(p);
		return ret;
	}

	/**
	 * Takes each element of an ArrayList and calls toString() on it, appending
	 * newlines.
	 * 
	 * @param list
	 *            the list to convert to a String
	 * @return a String made up of all of the elements of the list
	 */
	protected static String listToString(ArrayList<?> list) {
		String ret = "";
		for (Object o : list)
			ret += o.toString() + "\n";
		return ret;
	}

	/**
	 * Attempts to write a given string to a given file
	 * 
	 * @param output
	 *            the output to write to file
	 * @param file
	 *            the location of the file writing to
	 * @return 1 if successful, -1 if unsuccessful
	 */
	protected static int writeStringToFile(String output, String file) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write(output);
			out.close();
		} catch (IOException e) {
			Braincraft.reportError("Could not write to location " + file + ".");
			return -1;
		}
		return 1;
	}
}