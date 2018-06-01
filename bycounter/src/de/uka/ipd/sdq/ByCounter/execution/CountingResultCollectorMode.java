package de.uka.ipd.sdq.ByCounter.execution;

/**
 * This enum lists modes in which the {@link CountingResultCollector} can 
 * handle the counting of results.
 */
public enum CountingResultCollectorMode {
	/** Do not save any counting results at all. */
	DiscardAllIncomingCountingResults,
	
	/** Inline counting results (instruction and method counts) in all cases. */
	ForceInlineDisregardingInstrumentMethodWishes_InstructionAndMethodCounts,
	
	/** Inline counting results (instruction and method counts) in all cases, 
	 * but save the method signatures of individuall reporting methods. */
	ForceInlineDisregardingInstrumentMethodWishes_InstructionAndMethodCounts_ButCountReportsPerSignature,
		
//		/**
//		 * Setting this to true just appends method counts to forcedInlining_CollectedMethodCallCounts 
//		 * as well as appends method signatures to forcedInlining_CollectedMethodCallSignatures
//		 * TODO this is not evaluated properly so far
//		 */
//		ForceInlineDisregardingInstrumentMethodWishes_InstructionCountsOnly, //TODO appending method counts may still blow up memory
//		
//		ForceInlineDisregardingInstrumentMethodWishes_InstructionCountsOnly_ButCountReportsPerSignature,
	
	/** default */
	UseReportingMethodChoiceByInstrumentedMethods,
	/**
	 * Force inlining of a methods results once a threshold of results reported 
	 * from that method is reached. When the threshold of total reported methods 
	 * is reached, force inlining on all methods.
	 * unlimited threshold allowed --> define constant
	 */
	UseThresholdPerReportingMethod_UntilTotalThresholdReachedThenForceInline,
	/**
	 * Force inlining of a methods results once a total threshold of results 
	 * reported from any method is reached.  
	 * unlimited threshold allowed --> define constant */
	UseTotalThreshold_RegardlessOfIndividualMethodCountsThenForceInline;
	
	/**
	 * @return True if this mode specified reporting per signature.
	 */
	public boolean getCountReportsPerSignature() {
		if(this == ForceInlineDisregardingInstrumentMethodWishes_InstructionAndMethodCounts_ButCountReportsPerSignature
				 || this == UseThresholdPerReportingMethod_UntilTotalThresholdReachedThenForceInline
				 || this == UseTotalThreshold_RegardlessOfIndividualMethodCountsThenForceInline) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @return True if this mode specified forced inlining in all cases.
	 */
	public boolean getForceInliningAlways() {
		if(this == ForceInlineDisregardingInstrumentMethodWishes_InstructionAndMethodCounts
				|| this == ForceInlineDisregardingInstrumentMethodWishes_InstructionAndMethodCounts_ButCountReportsPerSignature) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @return True, if inlining is forced in some or all cases, ignoring 
	 * the inlining settings specified for the method.
	 */
	public boolean getForceInliningPossible() {
		return (getForceInliningAlways() 
			|| this == CountingResultCollectorMode.UseThresholdPerReportingMethod_UntilTotalThresholdReachedThenForceInline
			|| this == CountingResultCollectorMode.UseTotalThreshold_RegardlessOfIndividualMethodCountsThenForceInline);
	}
}
