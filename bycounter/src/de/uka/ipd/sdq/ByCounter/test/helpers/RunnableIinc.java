package de.uka.ipd.sdq.ByCounter.test.helpers;


/**
 * A {@link Runnable} implementation that increments an integer and prints it
 * to {@link System#out}.
 * @author Martin Krogmann
 */
public class RunnableIinc implements Runnable {
	
	@Override
	public void run() {
		int i = 0;
		i++;
		System.out.println(i);
	}
}