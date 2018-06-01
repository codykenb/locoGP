package de.uka.ipd.sdq.ByCounter.instrumentation;

/**
 * Data structure to keep track of a basic/range block during instrumentation.
 * @author Martin Krogmann
 */
public class BlockCounterData {
	
	/** variable/register index */
	public int variableIndex;

	/** Index of the block as it was defined. */
	public int blockIndex;
}
