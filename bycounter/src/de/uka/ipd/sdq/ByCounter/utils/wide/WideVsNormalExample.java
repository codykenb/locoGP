package de.uka.ipd.sdq.ByCounter.utils.wide;
public class WideVsNormalExample {
	public long testWide_ISTORE_3Vars_4Iters_falseArrayExplicit_5ArraySize() {
		long start = TimerHelper.time();
		long stop;
		int var0 = 0;
		int var1 = 0;
		int var2 = 0;
		var2 = -1390993650;
		var0 = 1255477124;
		var0 = 1255477124;
		var2 = -2000435098;
		System.out.println(var0);
		System.out.println(var1);
		System.out.println(var2);
		stop = TimerHelper.time();
		System.out
				.println("Method testWide_ISTORE_3Vars_4Iters_falseArrayExplicit_5ArraySize() : "
						+ (stop - start)
						+ " ns = "
						+ (stop - start)
						/ (1000L * 1000L) + " ms");
		return stop - start;
	}
}
