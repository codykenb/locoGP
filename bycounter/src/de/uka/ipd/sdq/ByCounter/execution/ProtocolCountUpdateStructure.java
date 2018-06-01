/**
 * 
 */
package de.uka.ipd.sdq.ByCounter.execution;

import java.util.UUID;

/**
 * This class is the same as {@link ProtocolCountStructure}, but has a different 
 * semantic. While {@link ProtocolCountStructure} is used to report complete 
 * results for a method, this class is used for updates to the counts while the 
 * instrumented method is still being executed. This is used when user 
 * specified sections of the method have been visited to provide live 
 * information on their execution.
 * 
 * @author Martin Krogmann
 *
 */
public class ProtocolCountUpdateStructure extends ProtocolCountStructure {

	/**
	 * Same constructor as superclass.
	 * Constructor for integer typed counters.
	 * @param executionStart {@link #executionStart}
	 * @param qualifyingMethodName {@link #qualifyingMethodName}
	 * @param opcodeCounts {@link #opcodeCountsInt}
	 * @param methodCallCounts {@link #methodCallCountsInt}
	 * @param calledMethods {@link #calledMethods}
	 * @param newArrayCounts {@link #newArrayCountsInt}
	 * @param requestID {@link #requestID}
	 * @param ownID {@link #ownID}
	 * @param callerID {@link #callerID}
	 * @param inliningSpecified {@link #inliningSpecified}
	 * @param blockCountingMode {@link #blockCountingMode}
	 */
	public ProtocolCountUpdateStructure(
			final long executionStart,
			final String qualifyingMethodName,
			final int[] opcodeCounts,
			final int[] methodCallCounts,
			final String[] calledMethods,
			final int[] newArrayCounts,
			final UUID requestID,
			final UUID ownID,
			final UUID callerID,
			final boolean inliningSpecified,
			final int blockCountingMode) {
		super(executionStart, qualifyingMethodName, opcodeCounts, 
				methodCallCounts, calledMethods, newArrayCounts, 
				requestID, ownID, callerID, inliningSpecified, blockCountingMode);
	}
	
	/**
	 * Constructor for long typed counters. Same as super class.
	 * @param executionStart {@link #executionStart}
	 * @param qualifyingMethodName {@link #qualifyingMethodName}
	 * @param opcodeCounts {@link #opcodeCountsInt}
	 * @param methodCallCounts {@link #methodCallCountsInt}
	 * @param calledMethods {@link #calledMethods}
	 * @param newArrayCounts {@link #newArrayCountsInt}
	 * @param requestID {@link #requestID}
	 * @param ownID {@link #ownID}
	 * @param callerID {@link #callerID}
	 * @param inliningSpecified {@link #inliningSpecified}
	 * @param blockCountingMode {@link #blockCountingMode} 
	 */
	public ProtocolCountUpdateStructure(
			final long executionStart,
			final String qualifyingMethodName,
			final long[] opcodeCounts,
			final long[] methodCallCounts,
			final String[] calledMethods,
			final long[] newArrayCounts,
			final UUID requestID,
			final UUID ownID,
			final UUID callerID, 
			final boolean inliningSpecified,
			final int blockCountingMode) {
		super(executionStart, qualifyingMethodName, opcodeCounts, 
				methodCallCounts, calledMethods, newArrayCounts, 
				requestID, ownID, callerID, inliningSpecified, blockCountingMode);
	}
}
