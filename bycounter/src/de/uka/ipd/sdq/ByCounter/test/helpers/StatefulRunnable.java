package de.uka.ipd.sdq.ByCounter.test.helpers;


public interface StatefulRunnable {
	int getCurrentState();
	void nextState();
}
