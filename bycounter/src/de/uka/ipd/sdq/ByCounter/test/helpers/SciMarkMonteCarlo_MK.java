package de.uka.ipd.sdq.ByCounter.test.helpers;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Estimate Pi by approximating the area of a circle.
 * 
 * How: generate N random numbers in the unit square, (0,0) to (1,1) and see how
 * are within a radius of 1 or less, i.e.
 * 
 * <pre>
 * 
 * sqrt(x &circ; 2 + y &circ; 2) &lt; r
 * 
 * </pre>
 * 
 * since the radius is 1.0, we can square both sides and avoid a sqrt()
 * computation:
 * 
 * <pre>
 * 
 * x &circ; 2 + y &circ; 2 &lt;= 1.0
 * 
 * </pre>
 * 
 * this area under the curve is (Pi * r^2)/ 4.0, and the area of the unit of
 * square is 1.0, so Pi can be approximated by
 * 
 * <pre>
 *  # points with x&circ;2+y&circ;2 &lt; 1
 *  Pi =&tilde; 		--------------------------  * 4.0
 *  total # points
 * 
 * </pre>
 * 
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
public class SciMarkMonteCarlo_MK {
	final static int NUM_SAMPLES = 1000 * 1000;
	final static int SEED = 113;

	/**
	 * TODO
	 * @deprecated MK because superseeded by "_MK" method 
	 * @param Num_samples
	 * @return approximation of PI
	 */
	public static final double integrate(int Num_samples) {

		SciMarkRandom_MK R = new SciMarkRandom_MK(SEED);

		int under_curve = 0;
		for (int count = 0; count < Num_samples; count++) {
			double x = R.nextDouble();
			double y = R.nextDouble();

			if (x * x + y * y <= 1.0)
				under_curve++;

		}
		
		return ((double) under_curve / Num_samples) * 4.0;
	}

	/**
	 * TODO MK h
	 * 
	 * @return approximation of PI
	 */
	public static final double integrateMK() {
		int Num_samples = NUM_SAMPLES;
		SciMarkRandom_MK R = new SciMarkRandom_MK(SEED);

		int under_curve = 0;
		for (int count = 0; count < Num_samples; count++) {
			double x = R.nextDouble();
			double y = R.nextDouble();

			if (x * x + y * y <= 1.0)
				under_curve++;

		}
		return ((double) under_curve / Num_samples) * 4.0;
		//for prevention of dead code elimination: the caller must use (e.g. print) the return value 
	}

	/**
	 * TODO
	 * 
	 * @param args
	 */
	@SuppressWarnings({"unused", "boxing"})
	public static void main(String[] args) {// added by MK
//		long start = System.nanoTime();
		int rep = 40;
		System.out.println("MonteCarlo_MK (rep="+rep+") : ");
	    long start = 0L;//MK TODO
	    long stop = 0L;//MK TODO
		ArrayList<Long> results = new ArrayList<Long>();
		double res = 0D;
		for(int i=0; i<rep; i++){
			start = System.nanoTime();// MK TODO
			System.out.println(integrateMK()); //done to prevent dead code elimination
			stop = System.nanoTime();// MK TODO
			results.add(stop-start);
			System.out.println(i+": "+(stop-start)+",");
//			long ns = stop - start;// MK TODO
//			long us = Math.round((double) ns / 1000);// MK TODO
//			long ms = Math.round((double) us / 1000);// MK TODO
//			long s = Math.round((double) ms / 1000);// MK TODO
//			System.out.println("JGFCastBench.JGFrun() itself reports: "
//					+ "with INITSIZE=" + INITSIZE // MK TODO
//					+ " and MAXSIZE=" + MAXSIZE + " executed in "// MK TODO
//					+ ns + " ns = " + us + " us = " + ms + " ms = " + s + " s.");// MK
		}
		System.out.println(results);
		Collections.sort(results);
		System.out.println("\n Median: "+results.get(results.size()/2));
//		double result = integrateMK();
//		long stop = System.nanoTime();
//		long ns = stop - start;
//		long us = Math.round((double) ns / 1000);
//		long ms = Math.round((double) us / 1000);
//		long s = Math.round((double) ms / 1000);
//		System.out.println("MonteCarlo.integrate(" + NUM_SAMPLES + ") "
//				+ "executed in " + ns + " ns = " + us + " us = " + ms
//				+ " ms = " + s + " s, giving PI=" + result);// MK TODO
	}

	/**
	 * TODO
	 * 
	 * @param Num_samples
	 * @return number of flops
	 */
	public static final double num_flops(int Num_samples) {
		// 3 flops in x^2+y^2 and 1 flop in random routine

		return ((double) Num_samples) * 4.0;

	}

}
