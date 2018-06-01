package de.uka.ipd.sdq.ByCounter.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import de.uka.ipd.sdq.ByCounter.instrumentation.BlockCountingMode;
import de.uka.ipd.sdq.ByCounter.parsing.ArrayCreation;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;

/**
 * This class can be used in addition to {@link CountingResultCollector} in 
 * order to achieve result inlining during result collection. Inlining means 
 * that instead of holding every individual result, results are added up into 
 * shared counters.
 * <p>
 * The goal of this strategy is to provide a collection strategy with a strong 
 * limit in used memory.
 * </p>
 * @author Martin Krogmann
 * @author Michael Kuperberg
 *
 */
public class CollectionStrategyForceInlining extends AbstractCollectionStrategy {


	private static final boolean USE_DEFERRED_BB_CALCULATIONS_DEFAULT = true;

	private static final int DEFAULT_THRESHOLD_PER_REPORTING_METHOD = 100;

	private static final int DEFAULT_TOTAL_THRESHOLD = 10000;

	/**
	 * Should be re-appended carefully to the lists returned by *get methods
	 */
	private CountingResult countingResult;

	/**
	 * Complements forcedInlining_Opcode counts.
	 * TODO there should be a SortedSet of such elements, similarily to forcedInlining_CountingResultSet 
	 * since method counts should also reflect the possibility to switch forced inlining on and off. 
	 * TODO work as a pointer to the field of forcedInlining_CountingResult instead?
	 */
	private SortedMap<String, Integer> occurenceCountsReportingMethods;

	private Integer thresholdPerReportingMethod;

	private Integer thresholdTotalMaximum;

	private Integer totalOfUninlinedMethodsDespiteForcedInlining;//don't forget to reset!

	/**
	 * Maps a method to the execution counts of the basic blocks 
	 * in that method.
	 */
	private long[][] uncalculatedBBCounts;

	/**
	 * Maps the method signature to the index in {@link #uncalculatedBBCounts}.
	 * This indirection is used to avoid the costs of boxing/unboxing 
	 * values in a Long[][] array by using the long[][] array.
	 */
	private Map<String, Integer> uncalculatedBBCounts_Index;

	/**
	 * When true, do not calculate opcode counts from the basic block execution
	 * counts instantly, but only when the results are requested. This is 
	 * only relevant to inlining.
	 */
	private static final boolean useDeferredBBcalculations = USE_DEFERRED_BB_CALCULATIONS_DEFAULT;

	/** {@link BlockResultCalculation} helper. */
	private BlockResultCalculation blockCalculation;
	
	/** When true, at least one result has been inlined since the last 
	 * {@link #clearResults()}*/
	private boolean hasInliningResult;

	/**
	 * Create and initialise the strategy.
	 */
	public CollectionStrategyForceInlining(final CountingResultCollector parent) {
		super(parent);
		this.clearResults();
		this.thresholdPerReportingMethod = DEFAULT_THRESHOLD_PER_REPORTING_METHOD;
		this.thresholdTotalMaximum = DEFAULT_TOTAL_THRESHOLD;
		this.totalOfUninlinedMethodsDespiteForcedInlining = 0;//even augment
		this.uncalculatedBBCounts_Index = new HashMap<String, Integer>();
		this.blockCalculation = new BlockResultCalculation(parentResultCollector.instrumentationContext);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.CollectionStrategy#clearResults()
	 */
	@Override
	public synchronized void clearResults() {
		this.countingResult = createNewForcedInlinedCountingResult();
		this.occurenceCountsReportingMethods = new TreeMap<String, Integer>();//TODO or clear?
		this.uncalculatedBBCounts = null;	// is initialised on demand
		this.hasInliningResult = false;
	}
	
	private synchronized CountingResult createNewForcedInlinedCountingResult() {
		CountingResult res = new CountingResult();
		res.setRequestID(UUID.randomUUID());
		res.setMethodExecutionID(UUID.randomUUID());
		res.setCallerID(UUID.randomUUID());
		res.setQualifiedMethodName("______forcedInlined______");
		res.setMethodInvocationBeginning(System.nanoTime/*currentTimeMillis*/());
		res.setReportingTime(0L); //reporting time TODO use a Date-like class for this...
		res.setOpcodeCounts(new long[CountingResultBase.MAX_OPCODE]);//opcode counts
		res.overwriteMethodCallCounts(new TreeMap<String, Long>());
		res.setArrayCreationCounts(new TreeMap<ArrayCreation, Long>());
		return res;
	}

	@Override
	public synchronized boolean protocolCount (
			ProtocolCountStructure result) {
		//TODO ignores newArray* as well as *IDs
		
		boolean forceInlining = parentResultCollector.getMode().getForceInliningAlways();
		
		if(parentResultCollector.getMode().getCountReportsPerSignature()) {
			Integer count=null;
			if(result.qualifyingMethodName==null){
				log.severe("Qualifying method name is null");
			}else{
				count = this.occurenceCountsReportingMethods.get(result.qualifyingMethodName);
				if(count==null){
					this.occurenceCountsReportingMethods.put(result.qualifyingMethodName, 1);
				}else{
					this.occurenceCountsReportingMethods.put(result.qualifyingMethodName, count+1);
				}
			}
			if(parentResultCollector.getMode() == CountingResultCollectorMode.UseThresholdPerReportingMethod_UntilTotalThresholdReachedThenForceInline
					&& count!=null && count>=thresholdPerReportingMethod){
				log.fine("Inlining counting result because "+result.qualifyingMethodName+
						" already has "+thresholdPerReportingMethod+" counting results");
				forceInlining = true;
			}else if(this.totalOfUninlinedMethodsDespiteForcedInlining>=this.thresholdTotalMaximum){
				log.fine("Inlining counting result because total number of stored counting results " +
						" already reached "+thresholdTotalMaximum);
				forceInlining = true;
			}
		}
		
		if(forceInlining) {
			forceInline(result, false);
			return true;
		} else {
			this.totalOfUninlinedMethodsDespiteForcedInlining++;
			return false;
		}
	}

	/**
	 * TODO for forced inlining, efficiency can be significantly increased by counting only those methods which have not been instrumented. This has to be a modification of the instrumenting mechanism...
	 */
	private synchronized void forceInline(
			ProtocolCountStructure result,
			boolean countReportsPerSignature) {
		
		// look for basic block counting
		boolean convertCountsFromOpcodeCounts = false;
		if(result.blockCountingMode != BlockCountingMode.NoBlocks) {
			convertCountsFromOpcodeCounts = true;
			// separate range block results make no sense for inlining because 
			// the results are all added up anyways, so use basic block routines
		}
		if(convertCountsFromOpcodeCounts) {
			if(useDeferredBBcalculations) {
				// save the values
				saveUncalculatedBBCounts(result.qualifyingMethodName, result.opcodeCounts);
			} else {
				// calculate immediatly
				CalculatedCounts ccounts = this.blockCalculation.calculateCountsFromBBCounts(
						result.qualifyingMethodName,
						result.opcodeCounts,
						countingResult.getOpcodeCounts(), 
						countingResult.getMethodCallCounts());
				// the forcedInlining_ values are included in the new counts
				// so apply the results
				countingResult.overwriteOpcodeCounts(ccounts.opcodeCounts);
				countingResult.overwriteMethodCallCounts(ccounts.methodCounts);
			}
		} else {
			
			for(int opcode = 0; opcode < result.opcodeCounts.length; opcode++) {
				countingResult.getOpcodeCounts()[opcode] += result.opcodeCounts[opcode];
			}
			Long currMethodCount;
			assert result.methodCallCounts.length == result.calledMethods.length;//escalate warning - RuntimeException?
			// create a HashMap for the method signatures and their counts
			for(int i = 0; i < result.methodCallCounts.length; i++) {
				currMethodCount = countingResult.getMethodCallCounts().get(result.calledMethods[i]);
				if(currMethodCount == null){
					countingResult.getMethodCallCounts().put(result.calledMethods[i], result.methodCallCounts[i]);
				}else{
					countingResult.getMethodCallCounts().put(result.calledMethods[i], currMethodCount+result.methodCallCounts[i]);//superfluous additions of repeated zeros?
				}
			}
		}
		
		if(result.executionStart<countingResult.forcedInlining_earliestStartOfInlinedMethod){
			countingResult.forcedInlining_earliestStartOfInlinedMethod = result.executionStart;
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void prepareCountingResults() {
		if(useDeferredBBcalculations) {
			processDeferredResults();
		}
		if(this.hasInliningResult) {
			countingResult.setResultCollection(this.currentResultCollection);
			this.currentResultCollection.getCountingResults().add(countingResult);
		}
	}

	public SortedMap<String, Integer> getForcedInlining_OccurenceCountsReportingMethods() {
		return this.occurenceCountsReportingMethods;
	}
	

	public CountingResultBase getForcedInlining_CountingResult() {
		return this.countingResult;
	}

	public Integer getForcedInlining_thresholdPerReportingMethod() {
		return this.thresholdPerReportingMethod;
	}

	public Integer getForcedInlining_thresholdTotalMaximum() {
		return this.thresholdTotalMaximum;
	}

	public Integer getForcedInlining_totalOfUninlinedMethodsDespiteForcedInlining() {
		return this.totalOfUninlinedMethodsDespiteForcedInlining;
	}

	/**
	 * If {@link #useDeferredBBcalculations} is enabled, this method processes 
	 * the uncalculated results.
	 */
	private void processDeferredResults() {
		for(Entry<String, Integer> e : this.uncalculatedBBCounts_Index.entrySet()) {
			int index = e.getValue();
			String canonicalMethodName = e.getKey();
			
			// then calculate the opcode counts
			CalculatedCounts ccounts = blockCalculation.calculateCountsFromBBCounts(
					canonicalMethodName,
					this.uncalculatedBBCounts[index],
					countingResult.getOpcodeCounts(), 
					countingResult.getMethodCallCounts());

			// add up the counts
			// the forcedInlining_ values are included in the new counts
			// so apply the results
			countingResult.overwriteOpcodeCounts(ccounts.opcodeCounts);
			countingResult.overwriteMethodCallCounts(ccounts.methodCounts);
		}
		this.uncalculatedBBCounts = null;
	}

	/**
	 * Add up the basic block execution counts for later opcode calculations.
	 * @param qualifyingMethodName Method from which the results come.
	 * @param bbCounts Basic block execution counts.
	 */
	private void saveUncalculatedBBCounts(String qualifyingMethodName,
			long[] bbCounts) {
		Integer index = uncalculatedBBCounts_Index.get(qualifyingMethodName);
		// initialise the array if necessary
		if(index == null) {
			index = uncalculatedBBCounts_Index.size();
			uncalculatedBBCounts_Index.put(qualifyingMethodName, index);
			if(this.uncalculatedBBCounts == null) {
				this.uncalculatedBBCounts = new long[bbCounts.length][];
			}
			uncalculatedBBCounts[index] = new long[bbCounts.length];
		}

		long[] basicBlockCounts = uncalculatedBBCounts[index];
		// add up the execution counts
		for(int i = 0; i < bbCounts.length; i++) {
			basicBlockCounts[i] += bbCounts[i];
		}
	}

}
