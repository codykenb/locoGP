package de.uka.ipd.sdq.ByCounter.test.helpers;

import java.util.Random;
import java.util.logging.Logger;

/**
 * A {@link Runnable} implementation that prints some output about it's 
 * state and runs mostly <= 1000 ms.
 * @author Martin Krogmann
 */
public class RunnableForThreading implements Runnable {
	/** Logging. */
	private static Logger log = Logger.getLogger(RunnableForThreading.class.getCanonicalName());
	/**
	 * Random number generator initialised with {@link System#currentTimeMillis()}.
	 * This is used to randomize thread execution durations.
	 */
	static Random random = new Random(System.currentTimeMillis());
	
	/**
	 * Minimum sleep time for {@link #run()} in milliseconds.
	 */
	public long baseSleepTime = 0;
	

	@Override
	public void run() {
		Thread currentThread = Thread.currentThread();
		final String id = "Thread id " + currentThread.getId()
				+ "; Name: " + currentThread.getName();
		log.info("Runnable called: " + id);
		try {
			Thread.sleep(baseSleepTime + Math.abs(random.nextLong()) % 1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		log.info("Runnable returning: " + id);
	}
}