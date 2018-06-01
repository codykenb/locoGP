package de.uka.ipd.sdq.ByCounter.test.nativeInstrumentation;

public class TimeNanoTime implements ITime {

	public long time() {
		return System.nanoTime()/(1000*1000);
	}

}
