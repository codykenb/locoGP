package de.uka.ipd.sdq.ByCounter.instrumentation;

public enum InstrumentationScopeModeEnum {
	/**
	 * Disregard what is otherwise specified to be instrumented and instrument
	 * nothing.
	 */
	InstrumentNothing,
	/**
	 * Instrument everything as specified.
	 */
	InstrumentAsSpecified,
	/**
	 * Disregard what is otherwise specified to be instrumented and instrument
	 * everything.
	 */
	InstrumentEverything

}
