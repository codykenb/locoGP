package de.uka.ipd.sdq.ByCounter.test.helpers;

import java.util.ArrayList;
import java.util.Collections;


/**
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 *
 */
public class JGFCastBench_MK/* implements JGFSection1_MK */{//TODO document modifications

	private static final int INITSIZE = 10000;
	private static final int MAXSIZE = 100000 /* MK TODO Used to be *10000 */;

	// MK TODO private static final double TARGETTIME = 10.0;

	@SuppressWarnings("boxing")
	public static void main(String argv[]) {

		// MK TODO JGFInstrumentor.printHeader(1, 0);

		JGFCastBench_MK cb = new JGFCastBench_MK();
		int rep = 40;
		System.out.println("JGFCastBench_MK, "+rep+" times JGFrun() : ");
		ArrayList<Long> results = new ArrayList<Long>(); 
		for(int i=0; i<rep; i++){
			long start = System.nanoTime();// MK TODO
			cb.JGFrun_MK();
			long stop = System.nanoTime();// MK TODO
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

	}

	/**
	 * @deprecated because full of dead code
	 */
	public void JGFrun() { //TODO document modifications

		int i, size;
		@SuppressWarnings("unused")
		double time;

		int i1 = 0;
		long l1 = 0;
		float f1 = 0.0F;
		double d1 = 0.0D;

		 JGFInstrumentor_MK.addTimer("Section1:Cast:IntFloat", "casts");

		time = 0.0;
		size = INITSIZE;
		i1 = 6;

		while (/* MK TODO time < TARGETTIME && */size < MAXSIZE) {
			JGFInstrumentor_MK.resetTimer("Section1:Cast:IntFloat");
			JGFInstrumentor_MK.startTimer("Section1:Cast:IntFloat");
			for (i = 0; i < size; i++) {
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
				f1 = (float) i1;
				i1 = (int) f1;
			}
			JGFInstrumentor_MK.stopTimer("Section1:Cast:IntFloat");

			// try to defeat dead code elimination //TODO MK doesn't work
			if (f1 == -1.0F)
				System.out.println(f1);
			time = JGFInstrumentor_MK.readTimer("Section1:Cast:IntFloat");
			JGFInstrumentor_MK.addOpsToTimer("Section1:Cast:IntFloat",
					(double) 32 * size);
			size *= 2;
		}

		JGFInstrumentor_MK.printperfTimer("Section1:Cast:IntFloat");

		JGFInstrumentor_MK.addTimer("Section1:Cast:IntDouble", "casts");

		time = 0.0;
		size = INITSIZE;
		i1 = 6;

		while (/* MK TODO time < TARGETTIME && */size < MAXSIZE) {
			JGFInstrumentor_MK.resetTimer("Section1:Cast:IntDouble");
			JGFInstrumentor_MK.startTimer("Section1:Cast:IntDouble");
			for (i = 0; i < size; i++) {
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
				d1 = (double) i1;
				i1 = (int) d1;
			}
			JGFInstrumentor_MK.stopTimer("Section1:Cast:IntDouble");

			// try to defeat dead code elimination
			if (d1 == -1.0D)
				System.out.println(d1);
			time = JGFInstrumentor_MK.readTimer("Section1:Cast:IntDouble");
			JGFInstrumentor_MK.addOpsToTimer("Section1:Cast:IntDouble",
					(double) 32 * size);
			size *= 2;
		}

		JGFInstrumentor_MK.printperfTimer("Section1:Cast:IntDouble");

		JGFInstrumentor_MK.addTimer("Section1:Cast:LongFloat", "casts");

		time = 0.0;
		size = INITSIZE;
		l1 = 7;

		while (/* MK TODO time < TARGETTIME && */size < MAXSIZE) {
			JGFInstrumentor_MK.resetTimer("Section1:Cast:LongFloat");
			JGFInstrumentor_MK.startTimer("Section1:Cast:LongFloat");
			for (i = 0; i < size; i++) {
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
				f1 = (float) l1;
				l1 = (long) f1;
			}
			JGFInstrumentor_MK.stopTimer("Section1:Cast:LongFloat");

			// try to defeat dead code elimination
			if (f1 == -1.0F)
				System.out.println(f1);
			time = JGFInstrumentor_MK.readTimer("Section1:Cast:LongFloat");
			JGFInstrumentor_MK.addOpsToTimer("Section1:Cast:LongFloat",
					(double) 32 * size);
			size *= 2;
		}

		JGFInstrumentor_MK.printperfTimer("Section1:Cast:LongFloat");

		JGFInstrumentor_MK.addTimer("Section1:Cast:LongDouble", "casts");

		time = 0.0;
		size = INITSIZE;
		l1 = 7;

		while (/* MK TODO time < TARGETTIME && */size < MAXSIZE) {
			JGFInstrumentor_MK.resetTimer("Section1:Cast:LongDouble");
			JGFInstrumentor_MK.startTimer("Section1:Cast:LongDouble");
			for (i = 0; i < size; i++) {
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
				d1 = (double) l1;
				l1 = (long) d1;
			}
			JGFInstrumentor_MK.stopTimer("Section1:Cast:LongDouble");

			// try to defeat dead code elimination
			if (d1 == -1.0D)
				System.out.println(d1);
			time = JGFInstrumentor_MK.readTimer("Section1:Cast:LongDouble");
			JGFInstrumentor_MK.addOpsToTimer("Section1:Cast:LongDouble",
					(double) 32 * size);
			size *= 2;
		}

		JGFInstrumentor_MK.printperfTimer("Section1:Cast:LongDouble");
	}

	/** TODO
	 */
	public void JGFrun_MK() { //TODO document modifications

		int i, size;
		@SuppressWarnings("unused")
		double time;

		int i1 = 0;
		long l1 = 0;
		float f1 = 0.0F;
		double d1 = 0.0D;

		 JGFInstrumentor_MK.addTimer("Section1:Cast:IntFloat", "casts");//TODO understand!

		time = 0.0;
		size = INITSIZE;
		i1 = 6;

		while (/* MK TODO time < TARGETTIME && */size < MAXSIZE) {
			JGFInstrumentor_MK.resetTimer("Section1:Cast:IntFloat");
			JGFInstrumentor_MK.startTimer("Section1:Cast:IntFloat");
			for (i = 0; i < size; i++) {
				f1 = (float) i1++;
				i1 = (int) --f1;
				f1 = (float) i1+3;
				i1 = (int) f1-4;
				f1 = (float) i1+5;
				i1 = (int) f1-6;
				f1 = (float) i1+7;
				i1 = (int) f1-8;
				f1 = (float) i1*+9;
				i1 = (int) f1-0;
				f1 = (float) i1*i1;
				i1 = (int) (f1*f1);
				f1 = (float) i1/1;
				i1 = (int) f1/2;
				f1 = (float) i1/3;
				i1 = (int) f1/4;
				f1 = (float) i1/5;
				i1 = (int) f1/6;
				f1 = (float) i1/7;
				i1 = (int) f1/8;
				f1 = (float) i1/9;
				i1 = (int) f1*2;
				f1 = (float) i1*3;
				i1 = (int) f1*4;
				f1 = (float) i1*5;
				i1 = (int) f1*6;
				f1 = (float) i1*7;
				i1 = (int) f1*8;
				f1 = (float) i1*9;
				i1 = (int) f1*10;
				f1 = (float) i1*11;
				i1 = (int) f1*12;
			}
			JGFInstrumentor_MK.stopTimer("Section1:Cast:IntFloat");

			// try to defeat dead code elimination //TODO MK doesn't work
//MK TODO 			if (f1 == -1.0F)
				System.out.println("f1:"+f1+", i1: "+i1);
			time = JGFInstrumentor_MK.readTimer("Section1:Cast:IntFloat");
			JGFInstrumentor_MK.addOpsToTimer("Section1:Cast:IntFloat",
					(double) 32 * size);
			size *= 2;
		}

		JGFInstrumentor_MK.printperfTimer("Section1:Cast:IntFloat");

		JGFInstrumentor_MK.addTimer("Section1:Cast:IntDouble", "casts");

		time = 0.0;
		size = INITSIZE;
		i1 = 6;

		while (/* MK TODO time < TARGETTIME && */size < MAXSIZE) {
			JGFInstrumentor_MK.resetTimer("Section1:Cast:IntDouble");
			JGFInstrumentor_MK.startTimer("Section1:Cast:IntDouble");
			for (i = 0; i < size; i++) {
				d1 = (double) i1++;
				i1 = (int) d1--;
				d1 = (double) ++i1;
				i1 = (int) --d1;
				d1 = (double) i1+2;
				i1 = (int) d1-3;
				d1 = (double) i1+4;
				i1 = (int) d1-5;
				d1 = (double) i1+6;
				i1 = (int) d1+7;
				d1 = (double) i1+8;
				i1 = (int) d1-9;
				d1 = (double) i1*i1;
				i1 = (int) (d1*d1);
				d1 = (double) i1/1;
				i1 = (int) d1/2;
				d1 = (double) i1/3;
				i1 = (int) d1/4;
				d1 = (double) i1/5;
				i1 = (int) d1/6;
				d1 = (double) i1/7;
				i1 = (int) d1/8;
				d1 = (double) i1/9;
				i1 = (int) d1*9;
				d1 = (double) i1*8;
				i1 = (int) d1*7;
				d1 = (double) i1*6;
				i1 = (int) d1*5;
				d1 = (double) i1*4;
				i1 = (int) d1*3;
				d1 = (double) i1*2;
				i1 = (int) (d1*d1);
			}
			JGFInstrumentor_MK.stopTimer("Section1:Cast:IntDouble");

			// try to defeat dead code elimination
//MK TODO	if (d1 == -1.0D)
				System.out.println("d1: "+d1+", i1: "+i1);
			time = JGFInstrumentor_MK.readTimer("Section1:Cast:IntDouble");
			JGFInstrumentor_MK.addOpsToTimer("Section1:Cast:IntDouble",
					(double) 32 * size);
			size *= 2;
		}

		JGFInstrumentor_MK.printperfTimer("Section1:Cast:IntDouble");

		JGFInstrumentor_MK.addTimer("Section1:Cast:LongFloat", "casts");

		time = 0.0;
		size = INITSIZE;
		l1 = 7;

		while (/* MK TODO time < TARGETTIME && */size < MAXSIZE) {
			JGFInstrumentor_MK.resetTimer("Section1:Cast:LongFloat");
			JGFInstrumentor_MK.startTimer("Section1:Cast:LongFloat");
			for (i = 0; i < size; i++) {
				f1 = (float) l1*9;
				l1 = (long) f1/8;
				f1 = (float) l1*7;
				l1 = (long) f1/6;
				f1 = (float) l1*5;
				l1 = (long) f1/4;
				f1 = (float) l1*3;
				l1 = (long) f1/2;
				f1 = (float) l1*-3;
				l1 = (long) f1/-4;
				f1 = (float) l1*-5;
				l1 = (long) f1/-6;
				f1 = (float) l1*-7;
				l1 = (long) f1/-8;
				f1 = (float) l1*-9;
				l1 = (long) f1+9;
				f1 = (float) l1-8;
				l1 = (long) f1+7;
				f1 = (float) l1-6;
				l1 = (long) f1+5;
				f1 = (float) l1-4;
				l1 = (long) f1+3;
				f1 = (float) l1-2;
				l1 = (long) f1+1;
				f1 = (float) l1++;
				l1 = (long) --f1;
				f1 = (float) (l1+90.1);
				l1 = (long) (f1-80.9);
				f1 = (float) (l1+70.8);
				l1 = (long) (f1-60.7);
				f1 = (float) (l1+50.8);
				l1 = (long) (f1+40.5);
			}
			JGFInstrumentor_MK.stopTimer("Section1:Cast:LongFloat");

			// try to defeat dead code elimination
//MK TODO	if (f1 == -1.0F)
				System.out.println("f1: "+f1+", l1: "+l1);
			time = JGFInstrumentor_MK.readTimer("Section1:Cast:LongFloat");
			JGFInstrumentor_MK.addOpsToTimer("Section1:Cast:LongFloat",
					(double) 32 * size);
			size *= 2;
		}

		JGFInstrumentor_MK.printperfTimer("Section1:Cast:LongFloat");

		JGFInstrumentor_MK.addTimer("Section1:Cast:LongDouble", "casts");

		time = 0.0;
		size = INITSIZE;
		l1 = 7;

		while (/* MK TODO time < TARGETTIME && */size < MAXSIZE) {
			JGFInstrumentor_MK.resetTimer("Section1:Cast:LongDouble");
			JGFInstrumentor_MK.startTimer("Section1:Cast:LongDouble");
			for (i = 0; i < size; i++) {
				d1 = (double) (l1+19.9);
				l1 = (long) (d1-20.1);
				d1 = (double) (l1*2);
				l1 = (long) (3*d1);
				d1 = (double) l1*4;
				l1 = (long) (d1*5.5);
				d1 = (double) (6.6*l1);
				l1 = (long) (d1/7.7);
				d1 = (double) (l1/8.8);
				l1 = (long) (d1/9.9);
				d1 = (double) l1*l1*l1;
				l1 = (long) (d1*d1*d1*d1);
				d1 = (double) 5*l1;
				l1 = (long) (d1+5.050505);
				d1 = (double) l1*l1;
				l1 = (long) d1+5;
				d1 = (double) l1-6;
				l1 = (long) (100000000-d1);
				d1 = (double) -1*(l1-99999999);
				l1 = (long) d1++;
				d1 = (double) --l1;
				l1 = (long) d1+5;
				d1 = (double) l1-6.6;
				l1 = (long) (d1+7.7);
				d1 = (double) (9+l1);
				l1 = (long) (d1*d1);
				d1 = (double) (l1/2);
				l1 = (long) (d1*d1);
				d1 = (double) (l1/3);
				l1 = (long) (d1+4.4);
				d1 = (double) (l1-5.9);
				l1 = (long) (d1*l1);
			}
			JGFInstrumentor_MK.stopTimer("Section1:Cast:LongDouble");

			// try to defeat dead code elimination
//MK TODO if(d1 == -1.0D)
				System.out.println("d1: "+d1+", l1: "+l1);
			time = JGFInstrumentor_MK.readTimer("Section1:Cast:LongDouble");
			JGFInstrumentor_MK.addOpsToTimer("Section1:Cast:LongDouble",
					(double) 32 * size);
			size *= 2;
		}

		JGFInstrumentor_MK.printperfTimer("Section1:Cast:LongDouble");
	}
}
