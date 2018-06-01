package de.uka.ipd.sdq.ByCounter.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import de.uka.ipd.sdq.ByCounter.instrumentation.BlockCountingMode;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedCodeArea;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedRegion;
import de.uka.ipd.sdq.ByCounter.parsing.ArrayCreation;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.results.RequestResult;

/**
 * This class is used in {@link CountingResultCollector} in 
 * order to process results during result collection when no other strategy 
 * is specified. 
 * @author Martin Krogmann
 *
 */
public class CollectionStrategyDefault extends AbstractCollectionStrategy {
	
	/** {@link BlockResultCalculation} helper. */
	private BlockResultCalculation blockCalculation;

	/** Indexing infrastructure for counting results. */
	private CountingResultIndexing countingResultIndexing;

	/** Indexing infrastructure for section update results. */
	private CountingResultUpdateIndexing countingResultUpdateIndexing;

	/** Indexing infrastructure for counting regions. */
	private CountingResultRegionIndexing countingResultRegionIndexing;
	
	/** Indexing infrastructure for counting thread structures. */
	private CountingResultThreadIndexing countingResultThreadIndexing;
	
	/** For each method: Last length of execution sequence. For updates. */
	private Map<UUID, Integer> blockExecutionSequenceLengthByMethod;

	/** Region that is currently counted. Is null when no region is 
	 * active. */
	private InstrumentedRegion currentRegion;

	/**
	 * When the instrumentation region ends, the last block needs 
	 * to be added still.
	 */
	private InstrumentedRegion regionEnd;

	/**
	 * Map that maps requestIds to a {@link RequestResult} if there was such 
	 * a result before. Otherwise the entry will be null.
	 */
	private Map<UUID, RequestResult> requestMap;
	
	/**
	 * Construct the strategy object.
	 * @param parent {@link CountingResultCollector} using this strategy.
	 */
	public CollectionStrategyDefault(CountingResultCollector parent) {
		super(parent);
		this.blockCalculation = new BlockResultCalculation(parentResultCollector.instrumentationContext);
		this.countingResultIndexing = new CountingResultIndexing();
		this.countingResultUpdateIndexing = new CountingResultUpdateIndexing();
		this.countingResultRegionIndexing = new CountingResultRegionIndexing();
		this.countingResultThreadIndexing = new CountingResultThreadIndexing();
		this.blockExecutionSequenceLengthByMethod = new HashMap<UUID, Integer>();
		this.currentRegion = null;
		this.regionEnd = null;
		this.requestMap = new HashMap<UUID, RequestResult>();
	}

	/** {@inheritDoc} */
	@Override
	public void clearResults() {
		this.countingResultIndexing.clearResults();
		this.countingResultUpdateIndexing.clearResults();
		this.countingResultRegionIndexing.clearResults();
		this.countingResultThreadIndexing.clearResults();
		this.blockExecutionSequenceLengthByMethod.clear();
		this.requestMap.clear();
	}

	/** 
	 * @return Indexing infrastructure for counting results.
	 */
	public CountingResultIndexing getCountingResultIndexing() {
		return countingResultIndexing;
	}

	/** {@inheritDoc} */
	@Override
	public void prepareCountingResults() {
		MethodExecutionRecord lastMethodExecutionDetails = parentResultCollector.getLastMethodExecutionDetails();
		if(lastMethodExecutionDetails == null) {
			log.warning("No method execution details are available. Please make certain that instrumented code has been executed.");
		} else  if(lastMethodExecutionDetails.executionSettings.getAddUpResultsRecursively()) {
			currentResultCollection.getCountingResults().addAll(this.countingResultIndexing.retrieveRecursiveSum(lastMethodExecutionDetails));
		}
	}
	
	/**
	 * Allocate a result.
	 */
	@Override
	public void protocolFutureCount(final ProtocolFutureCountStructure futureCount) {
		CountingResult cr = new CountingResult();
		cr.setQualifiedMethodName(futureCount.canonicalMethodName);
		cr.setMethodExecutionID(futureCount.ownID);
		cr.setObservedElement(parentResultCollector.instrumentationContext.getEntitiesToInstrument().get(futureCount.observedEntityID));
		cr.setThreadId(Thread.currentThread().getId());
		cr.setFinal(false);
		cr = (CountingResult) this.countingResultThreadIndexing.apply(cr, futureCount.spawnedThreads);
	}

	/** Add to counting results. */
	@Override
	public boolean protocolCount(ProtocolCountStructure result) {
		// Is this an update?
		if(!(result instanceof ProtocolCountUpdateStructure)) {
			// This is not an update so all updates are done.
			this.countingResultUpdateIndexing.setMethodDone(result.ownID);
		} else if(result.blockExecutionSequence != null) {
			// This is an update. Replace the block execution sequence with 
			// the part of the sequence that is new since the last update.
			Integer lastExSeqLength = blockExecutionSequenceLengthByMethod.get(result.ownID);
			Integer newExSeqLength = result.blockExecutionSequence.size();
			if(lastExSeqLength != null && lastExSeqLength != newExSeqLength) {
				ArrayList<Integer> newSequence = new ArrayList<Integer>();
				for(int i = lastExSeqLength; i < newExSeqLength; i++) {
					newSequence.add(result.blockExecutionSequence.get(i));
				}
				result.blockExecutionSequence = newSequence;
				if(result.blockCountingMode == BlockCountingMode.RangeBlocks) {
					// consider only the last entered range block?
					int rangeBlockIndex = result.rangeBlockExecutionSequence.get(result.rangeBlockExecutionSequence.size()-1);
					result.rangeBlockExecutionSequence = new ArrayList<Integer>();
					result.rangeBlockExecutionSequence.add(rangeBlockIndex);
				}
			}
			// update length of execution sequence
			blockExecutionSequenceLengthByMethod.put(result.ownID, newExSeqLength);
		}

		MethodExecutionRecord lastMethodExecutionDetails = parentResultCollector.getLastMethodExecutionDetails();
		if(lastMethodExecutionDetails == null) {
			log.warning("No method execution details are available. Please make certain that instrumented code has been executed.");
		}
		
		CalculatedCounts[] ccounts = calculateResultCounts(result);

		for(int ccountsNum = 0; ccountsNum < ccounts.length; ccountsNum++) {
			Map<ArrayCreation, Long> arrayCreationCounts = null;
			if(result.newArrayCounts != null && 
					result.newArrayCounts.length != 0) {
				// create the map for array creation counts
				arrayCreationCounts = new HashMap<ArrayCreation, Long>();
				List<ArrayCreation> creations = this.parentResultCollector.instrumentationContext.getArrayCreations().get(result.qualifyingMethodName);
				for(int i = 0; i < result.newArrayCounts.length; i++) {
					long count = result.newArrayCounts[i];
					arrayCreationCounts.put(creations.get(i), count);
				}
			}
			CountingResult res = new CountingResult();
			res.setRequestID(result.requestID);
			res.setMethodExecutionID(result.ownID);
			res.setCallerID(result.callerID);
			res.setQualifiedMethodName(result.qualifyingMethodName);
			res.setMethodInvocationBeginning(result.executionStart+ccountsNum);
			res.setReportingTime(result.reportingStart);
			res.setOpcodeCounts(ccounts[ccountsNum].opcodeCounts);
			res.overwriteMethodCallCounts(ccounts[ccountsNum].methodCounts);
			res.setArrayCreationCounts(arrayCreationCounts);
			res.setThreadId(Thread.currentThread().getId());
			res.setObservedElement(this.parentResultCollector.instrumentationContext.getEntitiesToInstrument().get(result.observedEntityID));
			res.setFinal(true);
			if(result.blockCountingMode == BlockCountingMode.RangeBlocks) {
				// set the index of the range block, i.e. the number of the section as
				// defined by the user in the instrumentation settings. This 
				// enables the user to find the counts for specific sections.
				final int indexOfRangeBlock = ccounts[ccountsNum].indexOfRangeBlock;
				res.setIndexOfRangeBlock(indexOfRangeBlock);
				
				InstrumentedCodeArea observedRange = 
						this.parentResultCollector.instrumentationContext.getRangesByMethod().get(
								result.qualifyingMethodName)[indexOfRangeBlock];
				res.setObservedElement(observedRange);
			} else if(result.blockCountingMode == BlockCountingMode.LabelBlocks) {
				final int labelBlockIndex = result.blockExecutionSequence.get(result.blockExecutionSequence.size()-1);
				for(InstrumentedRegion ir : parentResultCollector.instrumentationContext.getInstrumentationRegions()) {
					if(ir != null) {
						if(ir.getStartLabelIds().contains(labelBlockIndex)
								&& result.qualifyingMethodName.equals(ir.getStartMethod().getQualifyingMethodName())) {
							// region started
							this.currentRegion = ir;
							log.info("Region started: " + ir);
							this.addResultToCollection(res);
						}
						// this is not the else case if the region is a single line
						if(ir.getStopLabelIds().contains(labelBlockIndex)
								&& result.qualifyingMethodName.equals(ir.getStopMethod().getQualifyingMethodName())) {
							// region ended
							this.regionEnd = this.currentRegion;
							this.currentRegion = null;
							log.info("Region ended: " + regionEnd);
						}
					}
				}
			}
			res.setResultCollection(this.currentResultCollection);
			// When this result is not an update, add it to the permanent results
			if(!(result instanceof ProtocolCountUpdateStructure)) {
				if(result.blockCountingMode != BlockCountingMode.LabelBlocks) {
					if(this.parentResultCollector.instrumentationContext.getCountingMode() == CountingMode.Regions) {
						if(this.currentRegion != null) {
							res = this.countingResultThreadIndexing.apply(res, result.spawnedThreads);
							this.countingResultRegionIndexing.add(res, this.currentRegion);
						} else {
							// no region active, skip result
						}
					} else {
						res = this.countingResultThreadIndexing.apply(res, result.spawnedThreads);
						this.countingResultIndexing.add(res, result.reportingStart);

						if(lastMethodExecutionDetails != null && lastMethodExecutionDetails.executionSettings.getAddUpResultsRecursively()) {
							// adding will be done on retrieveResults()
							return true;
						} else {
							addResultToCollection(res);
						}
					}
				} else {
					return false;
				}
			} else {
				// result is an instance of ProtocolCountUpdateStructure

				if(this.parentResultCollector.instrumentationContext.getCountingMode() == CountingMode.Regions) {
					// add up for the counting region if necessary
					if(this.currentRegion != null) {
						this.countingResultRegionIndexing.add(res, this.currentRegion);
					} else if(this.regionEnd != null) {
						this.countingResultRegionIndexing.add(res, this.regionEnd);
						this.regionEnd = null;
					}

					// make sure observers are updated
					CountingResultSectionExecutionUpdate update = 
							new CountingResultSectionExecutionUpdate(res);
					CountingResultCollector.getInstance().setChanged();
					CountingResultCollector.getInstance().notifyObservers(update);
				} else {
					res = this.countingResultThreadIndexing.apply(res, result.spawnedThreads);
					this.countingResultUpdateIndexing.add(res);
				}
			}

		}
		return true;
	}

	/**
	 * Adds the result to {@link #currentResultCollection}.
	 * @param res CountingResult.
	 */
	private void addResultToCollection(CountingResult res) {
		final UUID requestID = res.getRequestID();
		if(requestID != null) {
			RequestResult requestResult = requestMap.get(requestID);
			if(requestResult == null) {
				// add a new RequestResult
				requestResult = new RequestResult();
				requestResult.setRequestId(requestID);
				requestMap.put(requestID, requestResult);
				this.currentResultCollection.getRequestResults().add(requestResult);
			}
			// add the result to the RequestResult
			requestResult.getCountingResults().add(res);
			res.setRequestResult(requestResult);
		} else {
			if(res.getSpawnedThreadedCountingResults().isEmpty()) {
				if(res.getThreadedCountingResultSource() == null) {
					this.currentResultCollection.getCountingResults().add(res);
				} else {
					// the parent result is already added, do nothing.
				}
			} else {
				// not a threaded result
				this.currentResultCollection.getCountingResults().add(res);
			}
		}
	}

	/**
	 * Based on the kind of information available in the 
	 * {@link ProtocolCountStructure}, calculate result counts.
	 * @param result The recorded data.´
	 * @return The result counts.
	 * @see CalculatedCounts
	 */
	private CalculatedCounts[] calculateResultCounts(
			ProtocolCountStructure result) {
		CalculatedCounts[] ccounts;
		SortedMap<String, Long> methodCounts = new TreeMap<String, Long>();
		if(result.blockCountingMode == BlockCountingMode.BasicBlocks) {
			if(result.blockExecutionSequence != null) {
				ccounts = blockCalculation.calculateCountsFromBlockExecutionSequence(
						result);
			} else {
				ccounts = new CalculatedCounts[] {
						blockCalculation.calculateCountsFromBBCounts(
						result.qualifyingMethodName, 
						result.opcodeCounts,
						new long[CountingResultBase.MAX_OPCODE], // opcode counts
						methodCounts)
				};
			}
		} else if (result.blockCountingMode == BlockCountingMode.LabelBlocks) {//Label blocks!
			if(result.blockExecutionSequence != null) {
				ccounts = blockCalculation.calculateCountsFromBlockExecutionSequence(
						result);
			} else {
				throw new RuntimeException("Label blocks currently only support calculation from block execution sequences.");
			}
		} else if (result.blockCountingMode == BlockCountingMode.RangeBlocks) {//Ranges!
			if(result.blockExecutionSequence != null) {
				result.rangeBlockExecutionSequence = removeDuplicateSequencesFromList(result.rangeBlockExecutionSequence);
				ccounts = blockCalculation.calculateCountsFromBlockExecutionSequence(
						result);
			} else {
				ccounts = blockCalculation.calculateCountsFromRBCounts(
						result.qualifyingMethodName, 
						result.opcodeCounts,
						new long[CountingResultBase.MAX_OPCODE], // opcode counts
						methodCounts);
			}
		} else {
			// check proper length
			if(result.methodCallCounts.length != result.calledMethods.length) {
				throw new IllegalArgumentException("Reported method call count structures must match in length.");
			}
			// create a HashMap for the method signatures and their counts
			for(int i = 0; i < result.methodCallCounts.length; i++) {
				methodCounts.put(result.calledMethods[i], result.methodCallCounts[i]);//TODO too much effort...
			}
			ccounts = new CalculatedCounts[1];//again, too many conversions...
			ccounts[0] = new CalculatedCounts();
			ccounts[0].opcodeCounts = result.opcodeCounts;
			ccounts[0].methodCounts = methodCounts;
			// numResults is already set to 1 above
		}
		return ccounts;
	}

	/**
	 * Remove duplicate sequences from the list and return the filtered result.
	 * Duplicate sequences are identical values that appear multiple times in 
	 * the list with no other values in between. I.e. in the list [1,2,3,3,3,4],
	 * [3,3,3] are duplicate sequences and will be replaced by [3].
	 * @param list List to remove the duplicate sequences from.
	 * @return Filtered list.
	 */
	private <T> ArrayList<T> removeDuplicateSequencesFromList(
			ArrayList<T> list) {
		ArrayList<T> result = new ArrayList<T>();
		T lastEntry = null;
		for(T entry : list) {
			if(lastEntry == entry) {
				continue;
			}
			result.add(entry);
			lastEntry = entry;
		}
		return result;
	}
}
