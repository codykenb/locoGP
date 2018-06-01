package de.uka.ipd.sdq.ByCounter.test.nativeInstrumentation;

public class TimeLinuxTSC implements ITime {

	public long time() {
		return uka.perfcount.Perfcount.readcounter();
	}

}
