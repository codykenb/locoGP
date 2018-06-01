package de.uka.ipd.sdq.ByCounter.execution;

/**
 * Globally used counting modes for instrumentation that mutually are exclusive.
 * @author Martin Krogmann
 *
 */
public enum CountingMode {
	/**
	 * Method instrumentation, sections etc.
	 */
	Default,
	/**
	 * Start/Stop-Regions.
	 */
	Regions

}
