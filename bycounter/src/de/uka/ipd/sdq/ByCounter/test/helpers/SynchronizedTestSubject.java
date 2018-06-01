package de.uka.ipd.sdq.ByCounter.test.helpers;

import de.uka.ipd.sdq.ByCounter.test.TestQueryUpdates;

/**
 * This class is instrumented by one or more test cases in 
 * {@link TestQueryUpdates}. It is designed so that the execution stays in 
 * specific sections of code until external code modifies the state counter
 * {@link SynchronizedTestSubject#nextState()}.
 * <p>
 * <b>NOTE: THIS CLASS IS SENSITIVE TO LINE NUMBER CHANGES!</b>
 * </p>
 * 
 * 
 * 
 * 
 * @author Martin Krogmann
 *
 */
public class SynchronizedTestSubject implements StatefulRunnable {
	/** This is used to control the state of {@link #run()}. */
	private volatile int stateCounter = 0;
	/** Currently active section. */
	private volatile int currentState = 0;
	
	/**
	 * This method loops terminates when {@link #nextState()} has been called
	 * twice. 
	 */
	public void run() {
		// 32: Start of section 1
		int i = 1;
		int j = 1;
		int k = 2;
		// 34: End of section 1
		// 37: Start of section 2
		while(stateCounter < 1) {
			currentState = 2;
			k += i + j;
			i = j;
			j = k;
			k--;
		}
		// 42: End of section 2
		// 46: Start of section 3
		while(stateCounter < 2) {
			currentState = 3;
			k -= i*j;
		}
		// 48: End of section 3
	}
	
	/**
	 * This method increments a state counter.
	 */
	@Override
	public synchronized void nextState() {
		stateCounter++;
	}

	@Override
	public int getCurrentState() {
		return currentState;
	}
}