package de.uka.ipd.sdq.ByCounter.utils.wide;
/**
 * This class provides a flexible timer to be used from WideVsNormal
 * TODO eventually, switch to TSC
*/
public class TimerHelper {
	public static long time(){
		return System.nanoTime();
	}
}
