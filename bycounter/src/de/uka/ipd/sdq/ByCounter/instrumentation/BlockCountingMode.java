/**
 * 
 */
package de.uka.ipd.sdq.ByCounter.instrumentation;

/**
 * Modes of grouping instructions when instrumenting.
 * @author Martin Krogmann
 */
public enum BlockCountingMode {
	/**
	 * Instructions are counted individually.
	 */
	NoBlocks,
	/**
	 * Instructions are grouped as basic blocks.
	 */
	BasicBlocks,
	/**
	 * Instructions are grouped into line number ranges.
	 */
	RangeBlocks,
	/**
	 * Instructions are grouped as label blocks, i.e. for each label there 
	 * is an instruction block.
	 */
	LabelBlocks;
	
	/**
	 * Statically set version of {@link #values()}
	 */
	public static BlockCountingMode[] values = values();
}
