package de.uka.ipd.sdq.ByCounter.test.helpers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * This test subject spawns multiple threads.
 * @author Martin Krogmann
 *
 */
public class ThreadedTestSubject {
	
	private static Logger log = Logger.getLogger(ThreadedTestSubject.class.getCanonicalName());
	
		public static void main(String[] args) {
		ThreadedTestSubject tts = new ThreadedTestSubject();
		try {
			tts.runThreads();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Spawn multiple threads with {@link RunnableForThreading} instances and wait for 
	 * them to finish.
	 * @throws InterruptedException Thrown if a spawned thread is interrupted.
	 */
	public void runThreads() throws InterruptedException {
		log.info("Run started.");
		Thread[] threads = new Thread[] {
				new Thread(new RunnableForThreading(), "t1"),
				new Thread(new RunnableForThreading(), "t2"),
				new Thread(new RunnableForThreading()),
				new Thread(new RunnableForThreading())
		};
		
		// start all threads
		for(Thread t : threads) {
			t.start();
		}
		log.info("Spawned " + threads.length + " threads.");
		
		// wait for all threads to finish
		for(Thread t : threads) {
			t.join();
		}
		log.info("Joined " + threads.length + " threads.");
		log.info("Run finished.");
		
		// now execute two threads sequentially
		Thread tIinc = new Thread(new RunnableIinc());
		Thread tRand = new Thread(new RunnableForThreading());
		tIinc.start();
		tIinc.join();
		tRand.start();
		tRand.join();
	};
	
	public void runThreadsExecutor() {
		ExecutorService es = Executors.newSingleThreadExecutor();
		es.execute(new RunnableForThreading());
		// TODO: extend me
	}
	
	/**
	 * Spawns a thread but does not join.
	 * @param sleepBase Minimum time the thread sleeps before returning.
	 */
	public void runThreadsNoJoin(long sleepBase) {
		RunnableForThreading runnableForThreading = new RunnableForThreading();
		runnableForThreading.baseSleepTime = sleepBase;
		Thread tRand = new Thread(runnableForThreading);
		tRand.start();
	}
}
