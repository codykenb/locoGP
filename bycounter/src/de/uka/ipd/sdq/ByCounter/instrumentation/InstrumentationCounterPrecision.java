package de.uka.ipd.sdq.ByCounter.instrumentation;

/**
 * This enum lists counter precision modes available in ByCounter.
 * @author Martin Krogmann
 *
 */
public enum InstrumentationCounterPrecision {
	/**
	 * Counters are int values, i.e. counts up to {@link Integer#MAX_VALUE} can
	 * be counted.
	 */
	Integer,
	/**
	 * Counters are long values, i.e. counts up to {@link Long#MAX_VALUE} can
	 * be counted.
	 */
	Long
}
