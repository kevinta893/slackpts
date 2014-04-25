package command;

import java.util.Random;

public class Rand {

	private static Random rand = new MersenneTwister();
	
	/**
	 * Gets a random integer from 0 inclusive, max exclusive.
	 * @param max
	 * @return
	 */
	public static int randInt(int max){
		return (rand.nextInt(max));
	}
	
	/**
	 * Returns a random integer from min and max both inclusive.
	 * @param min
	 * @param max
	 * @return
	 */
	public static int randInt(int min, int max){
		return (rand.nextInt((max-min)+1)+min);
	}
	
	
	
	private static final int ACCURACY = 10000000;
	/**
	 * Randomly picks an index according to the array of probabilities given.
	 * Eg: A = {0.25, 0.50, 0.25}
	 * The expected returns should be:
	 * 0 => ~25%
	 * 1 => ~50%
	 * 2 => ~25%
	 * 
	 * @param probArray An array in which the sum of all elements add up to 1.
	 * @return An index of the array.
	 */
	public static int randArray(double[] probArray){

		int threshold = 0;
		int randNum = rand.nextInt(ACCURACY-1);

		for (int i =0; i < probArray.length ;i++){
			threshold += probArray[i] * ACCURACY;

			if(randNum < threshold){
				return i;
			}

		}

		//probabilites likely did not add to 1.
		System.err.println("Warning! Probability array did not add to 1.");
		return -1;
	}
}
