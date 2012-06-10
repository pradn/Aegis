import braincraft.Brain;
import braincraft.Braincraft;
import braincraft.TribePopulation;

/**
 * @author Chris Donahue
 * 
 *         XOR is a simplified learning experiment example utilizing some of
 *         Braincraft's features. It evolves networks that approximate a 2-bit
 *         XOR gate (a more difficult task than an AND gate and will sometimes fail)
 */
public class XOR {

	public static void main(String[] args) {
		new XOR();
	}

	/**
	 * Constructs a new XOR() experiment.
	 */
	public XOR() {
		Braincraft.logToSystemOut = true;
		
		TribePopulation pop = new TribePopulation(20, 2, 1);

		// Loop to acquire and test Brains.
		double fitness = 0;
		int counter = 0;
		while (fitness < 4 && counter < 20000) {
			Brain b = pop.getBrain();
			fitness = evaluateBrain(b);
			pop.reportFitness(b, fitness);

			if (fitness == 4) {
				b.saveText("xornetwork.txt");
				pop.killPopulation("XOR network found and saved.");
			}
			counter++;
		}
		if (counter == 20000)
		{
			pop.killPopulation("XOR network not produced in 20,000 trials.");
		}

		Braincraft.writeLog("log.txt");
	}

	/**
	 * Evaluates a neural network for its use as an XOR gate. Output values less
	 * than 0.5 interpreted as 0, otherwise 1.
	 * 
	 * @param b
	 *            Brain to evaluate
	 * @return a fitness 0-4 based on how many correct answers the 2-bit XOR
	 *         network produced.
	 */
	public double evaluateBrain(Brain b) {
		boolean[] xarr = { false, true };
		boolean[] yarr = { false, true };
		
		int fitness = 0;
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				double[] arr = new double[2];
				arr[0] = x;
				arr[1] = y;

				double[] output = null;
				for (int i = 0; i < 4; i++) {
					output = b.pumpNet(arr);
				}

				boolean result = true;
				if (output[0] < 0.5) {
					result = false;
				}
				if (result == (xarr[x] ^ yarr[y])) {
					fitness++;
				}

				b.clearActivity();
			}
		}
		return fitness;
	}
}